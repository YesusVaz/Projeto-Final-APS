package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.AutenticacaoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertThrows(IllegalArgumentException.class, () -> auth.cadastrar("novo", "123", Perfil.FUNCIONARIO));
    }
}
