package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.AutenticacaoException;
import br.ufpb.aps.granja.model.excecao.ContaBloqueadaException;
import br.ufpb.aps.granja.model.excecao.EmailJaExisteException;
import br.ufpb.aps.granja.model.excecao.LoginJaExisteException;
import br.ufpb.aps.granja.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Login, sessao e cadastro de usuario. RN02 (inativo nao entra), RN03 (5 erros travam 15 min),
 * RNF09 (senha so como hash bcrypt) e RN25 (so o gestor escolhe perfil e status).
 * A sessao e um token aleatorio guardado em memoria, com validade. As tentativas erradas tambem ficam
 * em memoria: reiniciar o servidor zera as duas coisas.
 */
public final class AutenticacaoService {

    /** Custo do bcrypt. A RNF09 pede 10 ou mais. */
    private static final int CUSTO_BCRYPT = 10;
    private static final int MIN_CARACTERES_SENHA = 6;
    /** O bcrypt so le os primeiros 72 bytes. Passar disso cortaria a senha sem avisar. */
    private static final int MAX_BYTES_SENHA = 72;
    private static final int MAX_CARACTERES_NOME = 100;
    private static final int MAX_CARACTERES_EMAIL = 254;
    private static final Pattern LOGIN_VALIDO = Pattern.compile("[A-Za-z0-9._-]{3,30}");
    private static final Pattern EMAIL_VALIDO = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final Duration VALIDADE_SESSAO = Duration.ofHours(8);
    /** RN03. */
    private static final int MAX_TENTATIVAS = 5;
    private static final Duration TEMPO_BLOQUEIO = Duration.ofMinutes(15);
    /** Quem se cadastra sozinho entra como funcionario, que e o perfil de menos poder. */
    private static final Perfil PERFIL_PUBLICO = Perfil.FUNCIONARIO;

    /** O que o login devolve: o token pras proximas chamadas e o usuario logado. */
    public record SessaoAberta(String token, Usuario usuario) {
    }

    private record Sessao(String login, Instant expiraEm) {
    }

    /** Erros seguidos de um login e, se travou, ate quando. */
    private record Tentativas(int erros, Instant bloqueadoAte) {
    }

    private final UsuarioRepository usuarios;
    private final Clock relogio;
    private final BarramentoEventos eventos;
    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private final Map<String, Tentativas> tentativas = new ConcurrentHashMap<>();
    private final SecureRandom aleatorio = new SecureRandom();

    public AutenticacaoService(UsuarioRepository usuarios) {
        this(usuarios, new BarramentoEventos(), Clock.systemUTC());
    }

    public AutenticacaoService(UsuarioRepository usuarios, BarramentoEventos eventos) {
        this(usuarios, eventos, Clock.systemUTC());
    }

    AutenticacaoService(UsuarioRepository usuarios, Clock relogio) {
        this(usuarios, new BarramentoEventos(), relogio);
    }

    AutenticacaoService(UsuarioRepository usuarios, BarramentoEventos eventos, Clock relogio) {
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    /**
     * Confere login e senha. Login inexistente, senha errada e usuario inativo dao a mesma resposta.
     * RN03: no quinto erro seguido o login trava por 15 minutos, e enquanto estiver travado nem a senha
     * certa entra. Acertar zera a contagem.
     */
    public Usuario autenticar(String login, String senha) {
        String chave = chaveDe(login);
        Instant agora = Instant.now(relogio);
        exigirNaoBloqueado(chave, agora);
        Usuario usuario = usuarios.buscarPorLogin(login).orElse(null);
        if (usuario == null || !usuario.ativo() || senha == null || !BCrypt.checkpw(senha, usuario.senhaHash())) {
            registrarErro(chave, agora);
            throw new AutenticacaoException();
        }
        tentativas.remove(chave);
        return usuario;
    }

    /** Login: confere a credencial e abre uma sessao com token. */
    public SessaoAberta entrar(String login, String senha) {
        Usuario usuario = autenticar(login, senha);
        String token = novoToken();
        sessoes.put(token, new Sessao(usuario.login(), Instant.now(relogio).plus(VALIDADE_SESSAO)));
        return new SessaoAberta(token, usuario);
    }

    /** Devolve o dono do token. Token desconhecido, vencido ou de usuario inativo vira 401. */
    public Usuario usuarioDoToken(String token) {
        if (token == null) {
            throw new AutenticacaoException();
        }
        Sessao sessao = sessoes.get(token);
        if (sessao == null) {
            throw new AutenticacaoException();
        }
        if (!Instant.now(relogio).isBefore(sessao.expiraEm())) {
            sessoes.remove(token);
            throw new AutenticacaoException();
        }
        Usuario usuario = usuarios.buscarPorLogin(sessao.login()).orElseThrow(AutenticacaoException::new);
        if (!usuario.ativo()) {
            // RN02: quem foi inativado no meio da sessao perde o acesso na hora
            sessoes.remove(token);
            throw new AutenticacaoException();
        }
        return usuario;
    }

    /** Logout: o token deixa de valer. */
    public void sair(String token) {
        if (token != null) {
            sessoes.remove(token);
        }
    }

    /** Cadastro publico: a pessoa cria a propria conta. Perfil e status ficam fixos, nao vem do pedido. */
    public Usuario cadastrarPublico(String login, String email, String nome, String senha) {
        Usuario novo = criar(login, email, nome, senha, PERFIL_PUBLICO, true);
        auditar("Usuário " + novo.login() + " criou a própria conta como " + novo.perfil());
        return novo;
    }

    /** Cadastro feito pelo gestor, com perfil e status escolhidos por ele. Sem gestor logado, nao passa. */
    public Usuario cadastrarUsuario(Usuario autor, String login, String email, String nome, String senha,
                                    Perfil perfil, boolean ativo) {
        Autorizacao.exigir(autor, Acao.CADASTRAR_USUARIO);
        Usuario novo = criar(login, email, nome, senha, perfil, ativo);
        auditar("Usuário " + novo.login() + " cadastrado como " + novo.perfil() + (ativo ? "" : ", inativo")
                + " por " + autor.login());
        return novo;
    }

    /** Cria usuario na subida do sistema, sem autor. Usado pelo Main e pelos testes. */
    public Usuario cadastrar(String login, String email, String nome, String senha, Perfil perfil) {
        return criar(login, email, nome, senha, perfil, true);
    }

    private Usuario criar(String login, String email, String nome, String senha, Perfil perfil, boolean ativo) {
        String loginLimpo = validarLogin(login);
        String emailLimpo = validarEmail(email);
        String nomeLimpo = validarNome(nome);
        validarSenha(senha);
        if (perfil == null) {
            throw new IllegalArgumentException("O perfil é obrigatório.");
        }
        if (usuarios.buscarPorLogin(loginLimpo).isPresent()) {
            throw new LoginJaExisteException(loginLimpo);
        }
        if (usuarios.buscarPorEmail(emailLimpo).isPresent()) {
            throw new EmailJaExisteException(emailLimpo);
        }
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt(CUSTO_BCRYPT));
        return usuarios.inserir(loginLimpo, emailLimpo, nomeLimpo, hash, perfil, ativo);
    }

    private void exigirNaoBloqueado(String chave, Instant agora) {
        Tentativas atual = tentativas.get(chave);
        if (atual == null || atual.bloqueadoAte() == null) {
            return;
        }
        if (agora.isBefore(atual.bloqueadoAte())) {
            throw new ContaBloqueadaException(minutosAte(agora, atual.bloqueadoAte()));
        }
        // passou o tempo: comeca a contar do zero
        tentativas.remove(chave);
    }

    private void registrarErro(String chave, Instant agora) {
        Tentativas atual = tentativas.merge(chave, new Tentativas(1, null),
                (velha, nova) -> new Tentativas(velha.erros() + 1, null));
        if (atual.erros() >= MAX_TENTATIVAS) {
            Instant ate = agora.plus(TEMPO_BLOQUEIO);
            tentativas.put(chave, new Tentativas(atual.erros(), ate));
            auditar("Login " + chave + " bloqueado por " + TEMPO_BLOQUEIO.toMinutes() + " minutos após "
                    + MAX_TENTATIVAS + " tentativas erradas");
            throw new ContaBloqueadaException(TEMPO_BLOQUEIO.toMinutes());
        }
    }

    private static long minutosAte(Instant agora, Instant ate) {
        long segundos = Duration.between(agora, ate).toSeconds();
        return Math.max(1, (segundos + 59) / 60);
    }

    /** Ana e ANA sao a mesma chave aqui, pra trocar maiuscula nao driblar o bloqueio. */
    private static String chaveDe(String login) {
        return login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
    }

    private void auditar(String detalhe) {
        // o relogio e UTC; o log usa a hora local, igual as outras operacoes
        eventos.publicar(new OperacaoRealizada("USUARIO", detalhe,
                LocalDateTime.ofInstant(Instant.now(relogio), ZoneId.systemDefault())));
    }

    private String novoToken() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String validarLogin(String login) {
        if (login == null || !LOGIN_VALIDO.matcher(login.trim()).matches()) {
            throw new IllegalArgumentException(
                    "O login deve ter de 3 a 30 caracteres: letras, números, ponto, traço ou sublinhado.");
        }
        return login.trim();
    }

    /** E-mail guardado em minusculo, pra que Ana@x e ana@x nao virem duas contas. */
    private static String validarEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }
        String limpo = email.trim().toLowerCase(Locale.ROOT);
        if (limpo.length() > MAX_CARACTERES_EMAIL || !EMAIL_VALIDO.matcher(limpo).matches()) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        return limpo;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        String limpo = nome.trim();
        if (limpo.length() > MAX_CARACTERES_NOME) {
            throw new IllegalArgumentException("O nome passou de 100 caracteres.");
        }
        return limpo;
    }

    private static void validarSenha(String senha) {
        if (senha == null || senha.length() < MIN_CARACTERES_SENHA) {
            throw new IllegalArgumentException("A senha precisa ter pelo menos 6 caracteres.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_SENHA) {
            throw new IllegalArgumentException("A senha é longa demais: o limite é de 72 bytes.");
        }
    }
}
