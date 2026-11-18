package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.CategoriaFinanceira;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Fornecedor;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.LancamentoFinanceiro;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.RelatorioFinanceiro;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** UC19 a UC22: compra gera entrada de estoque e despesa (RN13), despesa, receita e relatorio. */
class FinanceiroServiceTest {

    private static final LocalDate HOJE = AGORA.toLocalDate();

    private Cenario c;
    private Insumo racao;
    private Fornecedor fornecedor;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        racao = c.app.estoque().cadastrarInsumo(c.gestor, "Ração inicial", CategoriaInsumo.RACAO, "kg", Quantidade.de(10));
        fornecedor = c.app.fornecedores().cadastrar(c.gestor, "Ração Boa Ltda", "11.222.333/0001-81", null, null, AGORA);
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    private CodigoLancamento cod(String valor) {
        return new CodigoLancamento(valor);
    }

    @Test
    void compraEntraNoEstoqueEViraDespesaNaMesmaOperacao() {
        LancamentoFinanceiro l = c.app.financeiro().registrarCompra(c.gestor, racao.id(), fornecedor.id(), 500,
                125_000, HOJE, cod("C-1"), AGORA);

        assertEquals(CategoriaFinanceira.COMPRA_INSUMO, l.categoria());
        assertEquals(fornecedor.id(), l.fornecedorId());
        assertTrue(l.descricao().contains("Ração Boa Ltda"));
        assertEquals(500, c.app.insumosRepositorio().buscarPorId(racao.id()).orElseThrow().saldo().valor());
        assertEquals(125_000, c.app.financeiro().relatorio(c.gestor, HOJE, HOJE, null).despesasCentavos());
    }

    @Test
    void compraComFalhaNaoDeixaMeiaOperacao() {
        assertThrows(NaoEncontradoException.class, () -> c.app.financeiro().registrarCompra(c.gestor, racao.id(), 999,
                100, 5_000, HOJE, cod("C-2"), AGORA));

        assertEquals(0, c.app.insumosRepositorio().buscarPorId(racao.id()).orElseThrow().saldo().valor());
        assertEquals(0, c.app.financeiro().relatorio(c.gestor, HOJE, HOJE, null).lancamentos().size());
        // a falha desfez o codigo tambem: o mesmo codigo ainda pode ser usado
        c.app.financeiro().registrarCompra(c.gestor, racao.id(), fornecedor.id(), 100, 5_000, HOJE, cod("C-2"), AGORA);
    }

    @Test
    void reenvioDaMesmaCompraNaoDuplica() {
        c.app.financeiro().registrarCompra(c.gestor, racao.id(), fornecedor.id(), 100, 5_000, HOJE, cod("C-3"), AGORA);

        assertThrows(LancamentoDuplicadoException.class, () -> c.app.financeiro().registrarCompra(c.gestor,
                racao.id(), fornecedor.id(), 100, 5_000, HOJE, cod("C-3"), AGORA));
        assertEquals(100, c.app.insumosRepositorio().buscarPorId(racao.id()).orElseThrow().saldo().valor());
    }

    @Test
    void compraRecusaValorQuantidadeEDataInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> c.app.financeiro().registrarCompra(c.gestor, racao.id(),
                fornecedor.id(), 0, 5_000, HOJE, cod("C-4"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> c.app.financeiro().registrarCompra(c.gestor, racao.id(),
                fornecedor.id(), 10, 0, HOJE, cod("C-5"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> c.app.financeiro().registrarCompra(c.gestor, racao.id(),
                fornecedor.id(), 10, 5_000, HOJE.plusDays(1), cod("C-6"), AGORA));
    }

    @Test
    void despesaERecebeitaSoComCategoriaDoTipoCerto() {
        c.app.financeiro().registrarDespesa(c.gestor, CategoriaFinanceira.ENERGIA_AGUA, "Conta de luz", 30_000, HOJE,
                null, cod("D-1"), AGORA);
        c.app.financeiro().registrarReceita(c.gestor, CategoriaFinanceira.VENDA_OVOS, "Venda de 30 dúzias", 90_000,
                HOJE, null, cod("R-1"), AGORA);

        assertThrows(IllegalArgumentException.class, () -> c.app.financeiro().registrarDespesa(c.gestor,
                CategoriaFinanceira.VENDA_OVOS, "x", 100, HOJE, null, cod("D-2"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> c.app.financeiro().registrarDespesa(c.gestor,
                CategoriaFinanceira.COMPRA_INSUMO, "x", 100, HOJE, null, cod("D-3"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> c.app.financeiro().registrarReceita(c.gestor,
                CategoriaFinanceira.MAO_DE_OBRA, "x", 100, HOJE, null, cod("R-2"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> c.app.financeiro().registrarDespesa(c.gestor,
                CategoriaFinanceira.MANUTENCAO, "  ", 100, HOJE, null, cod("D-4"), AGORA));
    }

    @Test
    void lancamentoDeLoteInexistenteRecebeNaoEncontrado() {
        assertThrows(NaoEncontradoException.class, () -> c.app.financeiro().registrarReceita(c.gestor,
                CategoriaFinanceira.VENDA_AVES, "Venda", 1_000, HOJE, 999L, cod("R-3"), AGORA));
    }

    @Test
    void relatorioSomaPorTipoECategoriaEFiltraPorPeriodoELote() {
        Lote lote = c.app.lotes().cadastrar(c.gestor, cod("L-1"), Especie.FRANGO_CORTE, Quantidade.de(100), AGORA);
        c.app.financeiro().registrarDespesa(c.gestor, CategoriaFinanceira.MAO_DE_OBRA, "Diarista", 20_000, HOJE,
                lote.id(), cod("D-5"), AGORA);
        c.app.financeiro().registrarDespesa(c.gestor, CategoriaFinanceira.MAO_DE_OBRA, "Diarista da outra granja",
                5_000, HOJE, null, cod("D-6"), AGORA);
        c.app.financeiro().registrarReceita(c.gestor, CategoriaFinanceira.VENDA_AVES, "Venda de 40 aves", 120_000,
                HOJE, lote.id(), cod("R-4"), AGORA);
        c.app.financeiro().registrarReceita(c.gestor, CategoriaFinanceira.VENDA_OVOS, "Venda antiga", 7_000,
                HOJE.minusDays(40), null, cod("R-5"), AGORA);

        RelatorioFinanceiro mes = c.app.financeiro().relatorio(c.gestor, HOJE.minusDays(10), HOJE, null);
        assertEquals(120_000, mes.receitasCentavos());
        assertEquals(25_000, mes.despesasCentavos());
        assertEquals(95_000, mes.resultadoCentavos());
        assertEquals(3, mes.lancamentos().size());
        assertEquals(CategoriaFinanceira.MAO_DE_OBRA, mes.porCategoria().get(0).categoria());
        assertEquals(25_000, mes.porCategoria().get(0).totalCentavos());

        RelatorioFinanceiro doLote = c.app.financeiro().relatorio(c.gestor, HOJE.minusDays(10), HOJE, lote.id());
        assertEquals(120_000, doLote.receitasCentavos());
        assertEquals(20_000, doLote.despesasCentavos());

        assertEquals(7_000, c.app.financeiro().relatorio(c.gestor, HOJE.minusDays(60), HOJE.minusDays(30), null)
                .receitasCentavos());
        assertThrows(IllegalArgumentException.class, () -> c.app.financeiro().relatorio(c.gestor, HOJE, HOJE.minusDays(1), null));
    }

    @Test
    void soOGestorMexeComFinanceiro() {
        assertThrows(AcessoNegadoException.class, () -> c.app.financeiro().registrarCompra(c.funcionario, racao.id(),
                fornecedor.id(), 10, 5_000, HOJE, cod("C-7"), AGORA));
        assertThrows(AcessoNegadoException.class, () -> c.app.financeiro().registrarDespesa(c.tecnico,
                CategoriaFinanceira.MANUTENCAO, "x", 100, HOJE, null, cod("D-7"), AGORA));
        assertThrows(AcessoNegadoException.class, () -> c.app.financeiro().registrarReceita(c.funcionario,
                CategoriaFinanceira.VENDA_OVOS, "x", 100, HOJE, null, cod("R-6"), AGORA));
        assertThrows(AcessoNegadoException.class, () -> c.app.financeiro().relatorio(c.tecnico, HOJE, HOJE, null));
    }
}
