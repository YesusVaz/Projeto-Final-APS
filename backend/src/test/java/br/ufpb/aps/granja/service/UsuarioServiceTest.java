package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.EmailJaExisteException;
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
    void gestorEditaNomeEmailEPerfilSemMudarOLogin() {
        Usuario editado = usuarios.editar(c.gestor, c.tecnico.id(), "  Novo Nome ", "NOVO@x.com", Perfil.FUNCIONARIO, AGORA);

        assertEquals("Novo Nome", editado.nome());
        assertEquals("novo@x.com", editado.email());
        assertEquals(Perfil.FUNCIONARIO, editado.perfil());
        assertEquals("tecnico", editado.login());
        assertEquals(Perfil.FUNCIONARIO, usuarios.listar(c.gestor).stream()
                .filter(u -> u.id().equals(c.tecnico.id())).findFirst().orElseThrow().perfil());
    }

    @Test
    void edicaoRecusaEmailDeOutraPessoaMasAceitaOProprio() {
        String emailDoGestor = c.gestor.email();
        assertThrows(EmailJaExisteException.class,
                () -> usuarios.editar(c.gestor, c.tecnico.id(), "Técnico", emailDoGestor, Perfil.TECNICO, AGORA));
        assertEquals(c.tecnico.email(),
                usuarios.editar(c.gestor, c.tecnico.id(), "Outro nome", c.tecnico.email(), Perfil.TECNICO, AGORA).email());
    }

    @Test
    void edicaoValidaOsDadosEQuemPodeEditar() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarios.editar(c.gestor, c.tecnico.id(), " ", "a@x.com", Perfil.TECNICO, AGORA));
        assertThrows(IllegalArgumentException.class,
                () -> usuarios.editar(c.gestor, c.tecnico.id(), "Ok", "sem-arroba", Perfil.TECNICO, AGORA));
        assertThrows(AcessoNegadoException.class,
                () -> usuarios.editar(c.tecnico, c.funcionario.id(), "Ok", "a@x.com", Perfil.TECNICO, AGORA));
        assertThrows(NaoEncontradoException.class,
                () -> usuarios.editar(c.gestor, 999L, "Ok", "a@x.com", Perfil.TECNICO, AGORA));
    }

    @Test
    void gestorNaoTrocaOProprioPerfilMasEditaOResto() {
        assertThrows(RegraDeNegocioException.class,
                () -> usuarios.editar(c.gestor, c.gestor.id(), "Gestor", c.gestor.email(), Perfil.TECNICO, AGORA));
        assertEquals("Chefe", usuarios.editar(c.gestor, c.gestor.id(), "Chefe", c.gestor.email(), Perfil.GESTOR, AGORA).nome());
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
