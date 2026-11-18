package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.CategoriaFinanceira;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.GraficosDesempenho;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.PainelGerencial;
import br.ufpb.aps.granja.model.PontoDia;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.RelatorioGerencial;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** UC23 (painel), UC25 (relatorio gerencial) e UC26 (graficos). */
class GestaoServiceTest {

    private static final LocalDate HOJE = AGORA.toLocalDate();

    private Cenario c;
    private Lote poedeiras;
    private Lote frangos;
    private Insumo racao;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        poedeiras = c.app.lotes().cadastrar(c.tecnico, cod("L-P"), Especie.POEDEIRA, Quantidade.de(1000), HOJE.minusDays(20), 100, AGORA);
        frangos = c.app.lotes().cadastrar(c.tecnico, cod("L-F"), Especie.FRANGO_CORTE, Quantidade.de(500), HOJE.minusDays(10), 1, AGORA);
        racao = c.app.estoque().cadastrarInsumo(c.gestor, "Ração", CategoriaInsumo.RACAO, "kg", Quantidade.de(200));
        c.app.estoque().movimentar(c.gestor, racao.id(), TipoMovimentoEstoque.ENTRADA, 1_000, OrigemMovimento.COMPRA, cod("E-1"), AGORA);

        c.app.lotes().registrarMortalidade(c.funcionario, poedeiras.id(), 30, null, cod("M-1"), AGORA);
        c.app.alimentacao().registrar(c.funcionario, poedeiras.id(), racao.id(), 700, HOJE, cod("A-1"), AGORA);
        c.app.producao().registrarPostura(c.funcionario, poedeiras.id(), 800, HOJE, cod("P-1"), AGORA);
        c.app.financeiro().registrarReceita(c.gestor, CategoriaFinanceira.VENDA_OVOS, "Ovos", 90_000, HOJE, poedeiras.id(), cod("R-1"), AGORA);
        c.app.financeiro().registrarDespesa(c.gestor, CategoriaFinanceira.ENERGIA_AGUA, "Luz", 20_000, HOJE, null, cod("D-1"), AGORA);
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    private static CodigoLancamento cod(String valor) {
        return new CodigoLancamento(valor);
    }

    @Test
    void painelSomaOQueAconteceuNoMes() {
        PainelGerencial p = c.app.gestao().painel(c.gestor, HOJE);

        assertEquals(2, p.lotesAtivos());
        assertEquals(970 + 500, p.avesVivas());
        assertEquals(30, p.mortesNoMes());
        assertEquals(700, p.racaoNoMesKg());
        assertEquals(800, p.ovosNoMes());
        assertEquals(90_000, p.receitasDoMesCentavos());
        assertEquals(20_000, p.despesasDoMesCentavos());
        assertEquals(70_000, p.resultadoDoMesCentavos());
        // 1000 - 700 = 300 acima do minimo de 200
        assertEquals(0, p.insumosAbaixoDoMinimo());
    }

    @Test
    void painelContaInsumoAbaixoDoMinimoELoteEncerradoSaiDosAtivos() {
        c.app.alimentacao().registrar(c.funcionario, frangos.id(), racao.id(), 150, HOJE, cod("A-2"), AGORA);
        c.app.lotes().encerrar(c.tecnico, frangos.id(), br.ufpb.aps.granja.model.MotivoEncerramento.VENDA, cod("ENC-1"), AGORA);

        PainelGerencial p = c.app.gestao().painel(c.gestor, HOJE);

        assertEquals(1, p.insumosAbaixoDoMinimo());
        assertEquals(1, p.lotesAtivos());
        assertEquals(970, p.avesVivas());
    }

    @Test
    void graficosTrazSeisMesesETrintaDiasComZeroNosDiasSemRegistro() {
        GraficosDesempenho g = c.app.gestao().graficos(c.gestor, HOJE);

        assertEquals(6, g.financeiroPorMes().size());
        assertEquals(YearMonth.from(HOJE), g.financeiroPorMes().get(5).mes());
        assertEquals(90_000, g.financeiroPorMes().get(5).receitasCentavos());
        assertEquals(20_000, g.financeiroPorMes().get(5).despesasCentavos());
        assertEquals(0, g.financeiroPorMes().get(0).receitasCentavos());

        assertEquals(30, g.mortesPorDia().size());
        assertEquals(HOJE.minusDays(29), g.mortesPorDia().get(0).data());
        PontoDia ultimo = g.mortesPorDia().get(29);
        assertEquals(HOJE, ultimo.data());
        assertEquals(30, ultimo.valor());
        assertEquals(0, g.mortesPorDia().get(28).valor());
        assertEquals(700, g.racaoPorDia().get(29).valor());
        assertEquals(800, g.ovosPorDia().get(29).valor());
    }

    @Test
    void relatorioTrazUmaLinhaPorLoteComFinanceiroDoPeriodo() {
        RelatorioGerencial r = c.app.gestao().relatorio(c.gestor, HOJE.minusDays(5), HOJE);

        assertEquals(90_000, r.receitasCentavos());
        assertEquals(20_000, r.despesasCentavos());
        assertEquals(2, r.lotes().size());
        var linhaOvos = r.lotes().stream().filter(l -> l.produtividade().codigoLote().equals(poedeiras.codigo())).findFirst().orElseThrow();
        assertEquals(90_000, linhaOvos.receitasCentavos());
        assertEquals(0, linhaOvos.despesasCentavos());
        assertEquals(800, linhaOvos.produtividade().ovosTotal());
        var linhaFrango = r.lotes().stream().filter(l -> l.produtividade().codigoLote().equals(frangos.codigo())).findFirst().orElseThrow();
        assertEquals(0, linhaFrango.receitasCentavos());
        assertTrue(r.insumosAbaixoDoMinimo().isEmpty());
    }

    @Test
    void relatorioRecusaPeriodoInvertido() {
        assertThrows(IllegalArgumentException.class, () -> c.app.gestao().relatorio(c.gestor, HOJE, HOJE.minusDays(1)));
    }

    @Test
    void soOGestorAcessaPainelGraficosERelatorio() {
        assertThrows(AcessoNegadoException.class, () -> c.app.gestao().painel(c.tecnico, HOJE));
        assertThrows(AcessoNegadoException.class, () -> c.app.gestao().graficos(c.funcionario, HOJE));
        assertThrows(AcessoNegadoException.class, () -> c.app.gestao().relatorio(c.tecnico, HOJE, HOJE));
    }
}
