package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.Fornecedor;
import br.ufpb.aps.granja.model.ParametrosSistema;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.FornecedorJaExisteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** UC10 (fornecedor, RN14) e UC27 (parametros, RN18). */
class FornecedorEParametrosTest {

    private Cenario c;

    @BeforeEach
    void preparar() {
        c = new Cenario();
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void gestorCadastraFornecedorComDocumentoNormalizado() {
        Fornecedor f = c.app.fornecedores().cadastrar(c.gestor, "Ração Boa Ltda", "11.222.333/0001-81",
                "(83) 99999-0000", "", AGORA);

        assertEquals("11222333000181", f.documento().numero());
        assertNull(f.email());
        assertEquals(1, c.app.fornecedores().listar(c.funcionario).size());
    }

    @Test
    void mesmoDocumentoComOutraMascaraEhRecusado() {
        c.app.fornecedores().cadastrar(c.gestor, "Granja Vizinha", "529.982.247-25", null, null, AGORA);

        assertThrows(FornecedorJaExisteException.class, () ->
                c.app.fornecedores().cadastrar(c.gestor, "Outro nome", "52998224725", null, null, AGORA));
    }

    @Test
    void soOGestorCadastraFornecedorEDocumentoInvalidoNaoPassa() {
        assertThrows(AcessoNegadoException.class, () ->
                c.app.fornecedores().cadastrar(c.tecnico, "X", "529.982.247-25", null, null, AGORA));
        assertThrows(IllegalArgumentException.class, () ->
                c.app.fornecedores().cadastrar(c.gestor, "X", "529.982.247-26", null, null, AGORA));
        assertThrows(IllegalArgumentException.class, () ->
                c.app.fornecedores().cadastrar(c.gestor, "X", "529.982.247-25", null, "sem-arroba", AGORA));
    }

    @Test
    void parametrosComecamNoPadraoESoOGestorMuda() {
        assertEquals(ParametrosSistema.PADRAO, c.app.parametros().consultar(c.funcionario));

        assertThrows(AcessoNegadoException.class, () -> c.app.parametros().configurar(c.tecnico, 3, 5, AGORA));
        c.app.parametros().configurar(c.gestor, 3, 5, AGORA);

        assertEquals(new ParametrosSistema(3, 5), c.app.parametros().consultar(c.tecnico));
    }

    @Test
    void parametrosForaDaFaixaSaoRecusados() {
        assertThrows(IllegalArgumentException.class, () -> c.app.parametros().configurar(c.gestor, 91, 5, AGORA));
        assertThrows(IllegalArgumentException.class, () -> c.app.parametros().configurar(c.gestor, 3, 0, AGORA));
    }
}
