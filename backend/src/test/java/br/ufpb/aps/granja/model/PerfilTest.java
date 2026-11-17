package br.ufpb.aps.granja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** RN25: checagem por perfil. Na Parcial 02 o AutorizacaoService usa essas mesmas regras. */
class PerfilTest {

    private Usuario com(Perfil perfil) {
<<<<<<< HEAD
        return new Usuario(1L, "usuario", "usuario@granja.local", "Usuario de teste", "hash-bcrypt", perfil);
=======
        return new Usuario(1L, "usuario", "hash-bcrypt", perfil);
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Test
    void funcionarioNaoEncerraLoteNemCadastraInsumo() {
        Usuario funcionario = com(Perfil.FUNCIONARIO);

        assertFalse(funcionario.podeExecutar(Acao.ENCERRAR_LOTE));
        assertFalse(funcionario.podeExecutar(Acao.CADASTRAR_INSUMO));
        assertTrue(funcionario.podeExecutar(Acao.REGISTRAR_MORTALIDADE));
    }

    @Test
    void tecnicoCadastraEEncerraLoteMasNaoRegistraMortalidade() {
        Usuario tecnico = com(Perfil.TECNICO);

        assertTrue(tecnico.podeExecutar(Acao.CADASTRAR_LOTE));
        assertTrue(tecnico.podeExecutar(Acao.ENCERRAR_LOTE));
        assertFalse(tecnico.podeExecutar(Acao.REGISTRAR_MORTALIDADE));
    }

    @Test
    void gestorTemAcessoIrrestrito() {
        Usuario gestor = com(Perfil.GESTOR);

        for (Acao acao : Acao.values()) {
            assertTrue(gestor.podeExecutar(acao), "gestor deveria poder " + acao);
        }
    }

    @Test
    void usuarioInativoNaoExecutaNada() {
        Usuario gestor = com(Perfil.GESTOR);
        gestor.inativar();

        for (Acao acao : Acao.values()) {
            assertFalse(gestor.podeExecutar(acao));
        }
    }
}
