package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.AutenticacaoException;
import br.ufpb.aps.granja.model.excecao.EmailJaExisteException;
import br.ufpb.aps.granja.model.excecao.LoginJaExisteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutenticacaoServiceTest {

    private Cenario c;
    private AutenticacaoService auth;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        auth = c.app.autenticacao();
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void entraComSenhaCorreta() {
        Usuario usuario = auth.autenticar("gestor", "senha-gestor");

        assertEquals(Perfil.GESTOR, usuario.perfil());
    }

    @Test
    void senhaErradaEhRecusada() {
        assertThrows(AutenticacaoException.class, () -> auth.autenticar("gestor", "errada"));
    }

    @Test
    void loginInexistenteEhRecusadoComAMesmaMensagem() {
        AutenticacaoException inexistente = assertThrows(AutenticacaoException.class,
                () -> auth.autenticar("ninguem", "qualquer"));
        AutenticacaoException senhaErrada = assertThrows(AutenticacaoException.class,
                () -> auth.autenticar("gestor", "errada"));

        assertEquals(senhaErrada.getMessage(), inexistente.getMessage());
    }

    @Test
    void usuarioInativoNaoEntra() {
        var repositorio = c.app.usuariosRepositorio();
        Usuario usuario = repositorio.buscarPorLogin("tecnico").orElseThrow();
        usuario.inativar();
        repositorio.atualizar(usuario);

        assertThrows(AutenticacaoException.class, () -> auth.autenticar("tecnico", "senha-tecnico"));
    }

    @Test
    void senhaFicaGuardadaComoHashBcrypt() {
        String hash = c.app.usuariosRepositorio().buscarPorLogin("gestor").orElseThrow().senhaHash();

        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertFalse(hash.contains("senha-gestor"));
    }

    @Test
    void senhaCurtaEhRecusadaNoCadastro() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.cadastrar("novo", "novo@granja.local", "Novo", "123", Perfil.FUNCIONARIO));
    }

    @Test
    void loginEntregaTokenQueAbreASessao() {
        AutenticacaoService.SessaoAberta sessao = auth.entrar("gestor", "senha-gestor");

        assertEquals("gestor", auth.usuarioDoToken(sessao.token()).login());
        assertFalse(sessao.token().contains("senha"));
    }

    @Test
    void tokenInventadoEhRecusado() {
        assertThrows(AutenticacaoException.class, () -> auth.usuarioDoToken("token-que-nao-existe"));
        assertThrows(AutenticacaoException.class, () -> auth.usuarioDoToken(null));
    }

    @Test
    void logoutInvalidaOToken() {
        String token = auth.entrar("gestor", "senha-gestor").token();

        auth.sair(token);

        assertThrows(AutenticacaoException.class, () -> auth.usuarioDoToken(token));
    }

    @Test
    void tokenVencidoEhRecusado() {
        RelogioManual relogio = new RelogioManual(Instant.parse("2026-11-17T12:00:00Z"));
        AutenticacaoService servico = new AutenticacaoService(c.app.usuariosRepositorio(), relogio);
        String token = servico.entrar("gestor", "senha-gestor").token();

        relogio.avancar(Duration.ofHours(7));
        assertEquals("gestor", servico.usuarioDoToken(token).login());

        relogio.avancar(Duration.ofHours(2));
        assertThrows(AutenticacaoException.class, () -> servico.usuarioDoToken(token));
    }

    @Test
    void inativadoNoMeioDaSessaoPerdeOAcessoNaHora() {
        String token = auth.entrar("tecnico", "senha-tecnico").token();
        var repositorio = c.app.usuariosRepositorio();
        Usuario tecnico = repositorio.buscarPorLogin("tecnico").orElseThrow();
        tecnico.inativar();
        repositorio.atualizar(tecnico);

        assertThrows(AutenticacaoException.class, () -> auth.usuarioDoToken(token));
    }

    // cadastro publico: sem perfil nem status na mao de quem se cadastra

    @Test
    void cadastroPublicoCriaFuncionarioAtivoComEmailEmMinusculas() {
        Usuario novo = auth.cadastrarPublico("ana.lima", "  Ana.Lima@Granja.LOCAL ", "Ana Lima", "senha-ana");

        assertEquals(Perfil.FUNCIONARIO, novo.perfil());
        assertTrue(novo.ativo());
        assertEquals("ana.lima@granja.local", novo.email());
        assertEquals("ana.lima", auth.autenticar("ana.lima", "senha-ana").login());
    }

    @Test
    void emailRepetidoEhRecusadoIgnorandoMaiusculas() {
        assertThrows(EmailJaExisteException.class,
                () -> auth.cadastrarPublico("outra", "GESTOR@granja.local", "Outra", "senha-outra"));
    }

    @Test
    void loginRepetidoEhRecusadoNoCadastroPublico() {
        assertThrows(LoginJaExisteException.class,
                () -> auth.cadastrarPublico("tecnico", "outro@granja.local", "Outro", "senha-outra"));
    }

    @Test
    void emailInvalidoEhRecusado() {
        for (String invalido : new String[] {"sem-arroba", "ana@", "ana@dominio", "ana @granja.local"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> auth.cadastrarPublico("ana", invalido, "Ana Lima", "senha-ana"), invalido);
        }
    }

    // cadastro pelo gestor: perfil e status escolhidos por ele

    @Test
    void gestorCadastraUsuarioComPerfilEEscolhidos() {
        Usuario novo = auth.cadastrarUsuario(c.gestor, "joana", "joana@granja.local", "Joana Lima",
                "senha-joana", Perfil.FUNCIONARIO, true);

        assertEquals("joana", novo.login());
        assertEquals("joana@granja.local", novo.email());
        assertEquals("Joana Lima", novo.nome());
        assertEquals(Perfil.FUNCIONARIO, novo.perfil());
        assertTrue(novo.ativo());
        assertEquals("joana", auth.autenticar("joana", "senha-joana").login());
    }

    @Test
    void gestorCadastraUsuarioInativo() {
        Usuario novo = auth.cadastrarUsuario(c.gestor, "marcos", "marcos@granja.local", "Marcos Reis",
                "senha-marcos", Perfil.TECNICO, false);

        assertFalse(novo.ativo());
        assertThrows(AutenticacaoException.class, () -> auth.autenticar("marcos", "senha-marcos"));
    }

    @Test
    void soOGestorCadastraUsuario() {
        assertThrows(AcessoNegadoException.class, () -> auth.cadastrarUsuario(c.tecnico, "joana",
                "joana@granja.local", "Joana Lima", "senha-joana", Perfil.FUNCIONARIO, true));
        assertThrows(AcessoNegadoException.class, () -> auth.cadastrarUsuario(c.funcionario, "joana",
                "joana@granja.local", "Joana Lima", "senha-joana", Perfil.FUNCIONARIO, true));
    }

    @Test
    void loginRepetidoEhRecusado() {
        assertThrows(LoginJaExisteException.class, () -> auth.cadastrarUsuario(c.gestor, "tecnico",
                "outro@granja.local", "Outro", "senha-outra", Perfil.FUNCIONARIO, true));
    }

    @Test
    void loginComCaracterInvalidoEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> auth.cadastrarUsuario(c.gestor, "ana lima",
                "ana@granja.local", "Ana Lima", "senha-ana", Perfil.FUNCIONARIO, true));
        assertThrows(IllegalArgumentException.class, () -> auth.cadastrarUsuario(c.gestor, "ab",
                "ana@granja.local", "Ana Lima", "senha-ana", Perfil.FUNCIONARIO, true));
    }

    @Test
    void senhaAcimaDe72BytesEhRecusada() {
        String longa = "a".repeat(73);

        assertThrows(IllegalArgumentException.class, () -> auth.cadastrarUsuario(c.gestor, "ana",
                "ana@granja.local", "Ana Lima", longa, Perfil.FUNCIONARIO, true));
    }

    /** Relogio que o teste controla, pra fazer a sessao vencer sem esperar 8 horas. */
    private static final class RelogioManual extends Clock {

        private Instant agora;

        RelogioManual(Instant agora) {
            this.agora = agora;
        }

        void avancar(Duration tempo) {
            agora = agora.plus(tempo);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
