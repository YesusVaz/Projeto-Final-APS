package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.ProdutividadeLote;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoHigienizacao;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.model.excecao.LoteEncerradoException;
import br.ufpb.aps.granja.service.HistoricoLoteService.ItemHistorico;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** UC14 (higienizacao), UC15 (iluminacao) e UC18 (produtividade, RN20). */
class ManejoEProdutividadeTest {

    private static final LocalDate ALOJAMENTO = LocalDate.of(2026, 10, 1);
    private static final LocalDate HOJE = AGORA.toLocalDate();

    private Cenario c;
    private Lote frangos;
    private Lote poedeiras;
    private Insumo racao;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        frangos = c.app.lotes().cadastrar(c.tecnico, cod("L-F"), Especie.FRANGO_CORTE, Quantidade.de(1000), ALOJAMENTO, 1, AGORA);
        poedeiras = c.app.lotes().cadastrar(c.tecnico, cod("L-P"), Especie.POEDEIRA, Quantidade.de(500), ALOJAMENTO, 120, AGORA);
        racao = c.app.estoque().cadastrarInsumo(c.gestor, "Ração", CategoriaInsumo.RACAO, "kg", Quantidade.de(10));
        c.app.estoque().movimentar(c.gestor, racao.id(), TipoMovimentoEstoque.ENTRADA, 10_000, OrigemMovimento.COMPRA, cod("E-1"), AGORA);
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    private static CodigoLancamento cod(String valor) {
        return new CodigoLancamento(valor);
    }

    // ---------- UC14 ----------

    @Test
    void funcionarioRegistraHigienizacaoEElaApareceNoHistorico() {
        c.app.manejo().registrarHigienizacao(c.funcionario, frangos.id(), TipoHigienizacao.DESINFECCAO,
                LocalDate.of(2026, 10, 5), "Cal virgem", cod("H-1"), AGORA);

        assertEquals(1, c.app.manejo().listarHigienizacoes(c.tecnico, frangos.id()).size());
        ItemHistorico item = c.app.historico().historico(c.gestor, frangos.id()).itens().stream()
                .filter(i -> i.categoria().equals("HIGIENIZACAO")).findFirst().orElseThrow();
        assertTrue(item.descricao().contains("Desinfecção"));
        assertTrue(item.descricao().contains("Cal virgem"));
    }

    @Test
    void higienizacaoRecusaLoteEncerradoDataFuturaOuAnteriorAoAlojamento() {
        assertThrows(IllegalArgumentException.class, () -> c.app.manejo().registrarHigienizacao(c.funcionario,
                frangos.id(), TipoHigienizacao.LIMPEZA_GALPAO, HOJE.plusDays(1), null, cod("H-2"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> c.app.manejo().registrarHigienizacao(c.funcionario,
                frangos.id(), TipoHigienizacao.LIMPEZA_GALPAO, ALOJAMENTO.minusDays(1), null, cod("H-3"), AGORA));
        c.app.lotes().encerrar(c.tecnico, frangos.id(), MotivoEncerramento.VENDA, cod("ENC-1"), AGORA);
        assertThrows(LoteEncerradoException.class, () -> c.app.manejo().registrarHigienizacao(c.funcionario,
                frangos.id(), TipoHigienizacao.LIMPEZA_GALPAO, HOJE, null, cod("H-4"), AGORA));
    }

    @Test
    void higienizacaoRepetidaPeloMesmoCodigoNaoDuplica() {
        c.app.manejo().registrarHigienizacao(c.funcionario, frangos.id(), TipoHigienizacao.TROCA_DE_CAMA, HOJE, null, cod("H-5"), AGORA);

        assertThrows(LancamentoDuplicadoException.class, () -> c.app.manejo().registrarHigienizacao(c.funcionario,
                frangos.id(), TipoHigienizacao.TROCA_DE_CAMA, HOJE, null, cod("H-5"), AGORA));
        assertEquals(1, c.app.manejo().listarHigienizacoes(c.gestor, frangos.id()).size());
    }

    @Test
    void soFuncionarioEGestorRegistramHigienizacao() {
        assertThrows(AcessoNegadoException.class, () -> c.app.manejo().registrarHigienizacao(c.tecnico, frangos.id(),
                TipoHigienizacao.DESINFECCAO, HOJE, null, cod("H-6"), AGORA));
    }

    // ---------- UC15 ----------

    @Test
    void tecnicoRegistraProgramaEOUltimoFicaEmVigor() {
        c.app.manejo().registrarPrograma(c.tecnico, poedeiras.id(), 16, 20, LocalDate.of(2026, 10, 10), null, cod("I-1"), AGORA);
        c.app.manejo().registrarPrograma(c.tecnico, poedeiras.id(), 14, null, LocalDate.of(2026, 11, 1), "Redução gradual", cod("I-2"), AGORA);

        var programas = c.app.manejo().listarProgramas(c.funcionario, poedeiras.id());
        assertEquals(2, programas.size());
        assertEquals(14, programas.get(programas.size() - 1).horasDeLuz());
        assertNull(programas.get(1).intensidadeLux());
    }

    @Test
    void programaRecusaHorasForaDeUmADezesseisEIntensidadeInvalida() {
        assertThrows(IllegalArgumentException.class, () -> c.app.manejo().registrarPrograma(c.tecnico, poedeiras.id(),
                0, null, HOJE, null, cod("I-3"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> c.app.manejo().registrarPrograma(c.tecnico, poedeiras.id(),
                25, null, HOJE, null, cod("I-4"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> c.app.manejo().registrarPrograma(c.tecnico, poedeiras.id(),
                16, 0, HOJE, null, cod("I-5"), AGORA));
    }

    @Test
    void funcionarioNaoRegistraPrograma() {
        assertThrows(AcessoNegadoException.class, () -> c.app.manejo().registrarPrograma(c.funcionario, poedeiras.id(),
                16, null, HOJE, null, cod("I-6"), AGORA));
    }

    // ---------- UC18 ----------

    @Test
    void conversaoAlimentarDoFrangoUsaGanhoDePesoEARacaoDoPeriodo() {
        c.app.lotes().registrarMortalidade(c.funcionario, frangos.id(), 100, null, cod("M-1"), AGORA);
        c.app.producao().registrarPesagem(c.tecnico, frangos.id(), 200, LocalDate.of(2026, 10, 10), cod("W-1"), AGORA);
        // fora do periodo: no dia da primeira pesagem nao conta
        c.app.alimentacao().registrar(c.funcionario, frangos.id(), racao.id(), 50, LocalDate.of(2026, 10, 10), cod("A-0"), AGORA);
        c.app.alimentacao().registrar(c.funcionario, frangos.id(), racao.id(), 600, LocalDate.of(2026, 10, 20), cod("A-1"), AGORA);
        c.app.alimentacao().registrar(c.funcionario, frangos.id(), racao.id(), 600, LocalDate.of(2026, 11, 1), cod("A-2"), AGORA);
        c.app.producao().registrarPesagem(c.tecnico, frangos.id(), 1_200, LocalDate.of(2026, 11, 1), cod("W-2"), AGORA);

        ProdutividadeLote p = c.app.produtividade().consultar(c.gestor, frangos.id());

        assertEquals(1000, p.totalEntrado());
        assertEquals(900, p.saldo());
        assertEquals(100, p.mortes());
        assertEquals(10.0, p.taxaDeMortalidade());
        assertEquals(1250, p.racaoConsumidaKg());
        // (1200 - 200) g x 900 aves = 900 kg ganhos; racao do periodo = 1200 kg
        assertEquals(900.0, p.ganhoDePesoKg());
        assertEquals(1.33, p.conversaoAlimentar());
        assertNull(p.ovosTotal());
    }

    @Test
    void semDuasPesagensNaoHaConversaoNemGanho() {
        c.app.producao().registrarPesagem(c.tecnico, frangos.id(), 200, LocalDate.of(2026, 10, 10), cod("W-3"), AGORA);

        ProdutividadeLote p = c.app.produtividade().consultar(c.gestor, frangos.id());

        assertNull(p.ganhoDePesoKg());
        assertNull(p.conversaoAlimentar());
        assertEquals(0, p.racaoConsumidaKg());
    }

    @Test
    void poedeiraTemOvosTaxaMediaERacaoPorDuzia() {
        c.app.producao().registrarPostura(c.funcionario, poedeiras.id(), 400, LocalDate.of(2026, 11, 1), cod("P-1"), AGORA);
        c.app.producao().registrarPostura(c.funcionario, poedeiras.id(), 300, LocalDate.of(2026, 11, 2), cod("P-2"), AGORA);
        c.app.alimentacao().registrar(c.funcionario, poedeiras.id(), racao.id(), 40, LocalDate.of(2026, 11, 1), cod("A-3"), AGORA);
        c.app.alimentacao().registrar(c.funcionario, poedeiras.id(), racao.id(), 40, LocalDate.of(2026, 11, 2), cod("A-4"), AGORA);
        // antes da primeira postura: nao entra na conta por duzia
        c.app.alimentacao().registrar(c.funcionario, poedeiras.id(), racao.id(), 500, LocalDate.of(2026, 10, 20), cod("A-5"), AGORA);

        ProdutividadeLote p = c.app.produtividade().consultar(c.gestor, poedeiras.id());

        assertEquals(700, p.ovosTotal());
        assertEquals(70.0, p.taxaMediaDePostura());
        // 80 kg / (700 / 12 duzias) = 1,37
        assertEquals(1.37, p.racaoPorDuzia());
        assertEquals(580, p.racaoConsumidaKg());
        assertNull(p.conversaoAlimentar());
    }

    @Test
    void soOGestorVeAProdutividade() {
        assertThrows(AcessoNegadoException.class, () -> c.app.produtividade().consultar(c.tecnico, frangos.id()));
        assertThrows(AcessoNegadoException.class, () -> c.app.produtividade().consultar(c.funcionario, frangos.id()));
    }
}
