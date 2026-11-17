package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.AutenticacaoException;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** UC01: o gestor lista, inativa e reativa usuarios (RN02). */
class UsuarioServiceTest {

    private Cenario c;
    private UsuarioService usuarios;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        usuarios = c.app.usuarios();
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void gestorListaTodosOsUsuarios() {
        assertEquals(3, usuarios.listar(c.gestor).size());
    }

    @Test
    void soOGestorGerenciaUsuarios() {
        assertThrows(AcessoNegadoException.class, () -> usuarios.listar(c.tecnico));
        assertThrows(AcessoNegadoException.class,
                () -> usuarios.alterarStatus(c.funcionario, c.tecnico.id(), false, AGORA));
    }

    @Test
    void inativadoPerdeASessaoNaHoraEReativadoVoltaAEntrar() {
        String token = c.app.autenticacao().entrar("tecnico", "senha-tecnico").token();

        Usuario inativo = usuarios.alterarStatus(c.gestor, c.tecnico.id(), false, AGORA);
        assertFalse(inativo.ativo());
        assertThrows(AutenticacaoException.class, () -> c.app.autenticacao().usuarioDoToken(token));
        assertThrows(AutenticacaoException.class, () -> c.app.autenticacao().autenticar("tecnico", "senha-tecnico"));

        assertTrue(usuarios.alterarStatus(c.gestor, c.tecnico.id(), true, AGORA).ativo());
        assertEquals("tecnico", c.app.autenticacao().autenticar("tecnico", "senha-tecnico").login());
    }

    @Test
    void gestorNaoPodeSeInativar() {
        assertThrows(RegraDeNegocioException.class, () -> usuarios.alterarStatus(c.gestor, c.gestor.id(), false, AGORA));
    }

    @Test
    void usuarioInexistenteDa404() {
        assertThrows(NaoEncontradoException.class, () -> usuarios.alterarStatus(c.gestor, 999L, false, AGORA));
    }

    @Test
    void mudancaDeStatusEntraNoLogEOLogSoOGestorLe() {
        usuarios.alterarStatus(c.gestor, c.funcionario.id(), false, AGORA);

        assertTrue(c.app.consultaAuditoria().listar(c.gestor).stream()
                .anyMatch(e -> e.categoria().equals("USUARIO") && e.detalhe().contains("funcionario inativado")));
        assertThrows(AcessoNegadoException.class, () -> c.app.consultaAuditoria().listar(c.tecnico));
    }
}
