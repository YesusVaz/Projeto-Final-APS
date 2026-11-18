package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.EmailJaExisteException;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import br.ufpb.aps.granja.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UC01: gestao dos usuarios pelo gestor. Lista e ativa ou inativa (RN02).
 * Nao existe operacao que troque o login: ele fica igual pra sempre (RN01).
 */
public final class UsuarioService {

    private final UsuarioRepository usuarios;
    private final BarramentoEventos eventos;

    public UsuarioService(UsuarioRepository usuarios, BarramentoEventos eventos) {
        this.usuarios = usuarios;
        this.eventos = eventos;
    }

    public List<Usuario> listar(Usuario autor) {
        Autorizacao.exigir(autor, Acao.GERENCIAR_USUARIOS);
        return usuarios.listar();
    }

    /**
     * Edita nome, e-mail e perfil (o login nao muda, RN01). O e-mail continua unico. O gestor nao troca o
     * proprio perfil, senao podia tirar de si o acesso de administrar e ninguem mais gerenciaria usuarios.
     */
    public Usuario editar(Usuario autor, long usuarioId, String nome, String email, Perfil perfil, LocalDateTime agora) {
        Autorizacao.exigir(autor, Acao.GERENCIAR_USUARIOS);
        if (perfil == null) {
            throw new IllegalArgumentException("O perfil é obrigatório.");
        }
        String nomeLimpo = AutenticacaoService.validarNome(nome);
        String emailLimpo = AutenticacaoService.validarEmail(email);
        Usuario alvo = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new NaoEncontradoException("Usuário " + usuarioId + " não encontrado."));
        if (alvo.id().equals(autor.id()) && perfil != alvo.perfil()) {
            throw new RegraDeNegocioException("O gestor não pode trocar o próprio perfil.");
        }
        usuarios.buscarPorEmail(emailLimpo)
                .filter(outro -> !outro.id().equals(alvo.id()))
                .ifPresent(outro -> {
                    throw new EmailJaExisteException(emailLimpo);
                });
        alvo.atualizarDados(nomeLimpo, emailLimpo, perfil);
        usuarios.atualizar(alvo);
        eventos.publicar(new OperacaoRealizada("USUARIO", "Usuário " + alvo.login() + " editado por " + autor.login()
                + " (perfil " + alvo.perfil() + ")", agora));
        return alvo;
    }

    /**
     * Ativa ou inativa. Inativado perde a sessao na proxima chamada, porque o token e conferido
     * contra o banco toda vez. O gestor nao pode se inativar, senao ninguem mais administra.
     */
    public Usuario alterarStatus(Usuario autor, long usuarioId, boolean ativo, LocalDateTime agora) {
        Autorizacao.exigir(autor, Acao.GERENCIAR_USUARIOS);
        Usuario alvo = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new NaoEncontradoException("Usuário " + usuarioId + " não encontrado."));
        if (!ativo && alvo.id().equals(autor.id())) {
            throw new RegraDeNegocioException("O gestor não pode inativar a própria conta.");
        }
        if (ativo) {
            alvo.ativar();
        } else {
            alvo.inativar();
        }
        usuarios.atualizar(alvo);
        eventos.publicar(new OperacaoRealizada("USUARIO", "Usuário " + alvo.login() + (ativo ? " ativado" : " inativado")
                + " por " + autor.login(), agora));
        return alvo;
    }
}
