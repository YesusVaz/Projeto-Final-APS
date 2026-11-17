package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.MovimentoEstoque;
import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.StatusOcorrencia;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.Vacinacao;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.LoteEncerradoException;
import br.ufpb.aps.granja.model.excecao.OcorrenciaEncerradaException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import br.ufpb.aps.granja.model.excecao.SaldoInsuficienteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** UC11 (vacinacao, RN08) e UC12 (ocorrencia e tratamento, RN09), tudo ou nada (RN23). */
class SanidadeServiceTest {

    private static final LocalDate HOJE = AGORA.toLocalDate();

    private Cenario c;
    private SanidadeService sanidade;
    private Lote lote;
    private Insumo vacina;
    private Insumo remedio;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        sanidade = c.app.sanidade();
        lote = c.app.lotes().cadastrar(c.tecnico, codigo("L-1"), Especie.FRANGO_CORTE, Quantidade.de(100),
                LocalDate.of(2026, 11, 1), 1, AGORA);
        vacina = insumoComEstoque("Newcastle", CategoriaInsumo.VACINA, 100, "E-1");
        remedio = insumoComEstoque("Antibiótico", CategoriaInsumo.MEDICAMENTO, 20, "E-2");
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void vacinacaoBaixaAsDosesDoEstoqueComOrigemELote() {
        Vacinacao v = sanidade.registrarVacinacao(c.tecnico, lote.id(), vacina.id(), 100, HOJE,
                HOJE.plusDays(14), null, codigo("V-1"), AGORA);

        assertEquals(HOJE.plusDays(14), v.proximaDose());
        assertEquals(0, saldo(vacina));
        MovimentoEstoque saida = ultimoMovimento(vacina);
        assertEquals(TipoMovimentoEstoque.SAIDA, saida.tipo());
        assertEquals(OrigemMovimento.VACINACAO, saida.origem());
        assertEquals(lote.id(), saida.loteId());
        assertEquals(1, sanidade.vacinacoesDoLote(c.funcionario, lote.id()).size());
    }

    @Test
    void faltaDeVacinaNaoGravaNadaNemOCodigo() {
        assertThrows(SaldoInsuficienteException.class, () -> sanidade.registrarVacinacao(c.tecnico, lote.id(),
                vacina.id(), 101, HOJE, null, null, codigo("V-1"), AGORA));

        assertEquals(100, saldo(vacina));
        assertTrue(sanidade.vacinacoesDoLote(c.gestor, lote.id()).isEmpty());
        sanidade.registrarVacinacao(c.tecnico, lote.id(), vacina.id(), 10, HOJE, null, null, codigo("V-1"), AGORA);
    }

    @Test
    void vacinacaoComInsumoQueNaoEhVacinaEhRecusada() {
        assertThrows(RegraDeNegocioException.class, () -> sanidade.registrarVacinacao(c.tecnico, lote.id(),
                remedio.id(), 1, HOJE, null, null, codigo("V-1"), AGORA));
        assertEquals(20, saldo(remedio));
    }

    @Test
    void reforcoAntesDaVacinaDataFuturaOuAntesDoAlojamentoSaoRecusados() {
        assertThrows(IllegalArgumentException.class, () -> sanidade.registrarVacinacao(c.tecnico, lote.id(),
                vacina.id(), 1, HOJE, HOJE, null, codigo("V-1"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> sanidade.registrarVacinacao(c.tecnico, lote.id(),
                vacina.id(), 1, HOJE.plusDays(1), null, null, codigo("V-2"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> sanidade.registrarVacinacao(c.tecnico, lote.id(),
                vacina.id(), 1, LocalDate.of(2026, 10, 31), null, null, codigo("V-3"), AGORA));
    }

    @Test
    void funcionarioNaoVacinaELoteEncerradoNaoRecebe() {
        assertThrows(AcessoNegadoException.class, () -> sanidade.registrarVacinacao(c.funcionario, lote.id(),
                vacina.id(), 1, HOJE, null, null, codigo("V-1"), AGORA));

        c.app.lotes().encerrar(c.tecnico, lote.id(), MotivoEncerramento.VENDA, codigo("F-1"), AGORA);
        assertThrows(LoteEncerradoException.class, () -> sanidade.registrarVacinacao(c.tecnico, lote.id(),
                vacina.id(), 1, HOJE, null, null, codigo("V-1"), AGORA));
        assertEquals(100, saldo(vacina));
    }

    @Test
    void tratamentoBaixaOMedicamentoEFicaNaOcorrencia() {
        OcorrenciaSanitaria o = sanidade.abrirOcorrencia(c.tecnico, lote.id(), "Tosse e espirros", HOJE, AGORA);

        sanidade.registrarTratamento(c.tecnico, o.id(), remedio.id(), 5, HOJE, "na água, 5 dias", codigo("T-1"), AGORA);

        assertEquals(15, saldo(remedio));
        assertEquals(OrigemMovimento.TRATAMENTO, ultimoMovimento(remedio).origem());
        SanidadeService.ConsultaOcorrencia consulta = sanidade.consultarOcorrencia(c.funcionario, o.id());
        assertEquals(1, consulta.tratamentos().size());
        assertEquals(StatusOcorrencia.ABERTA, consulta.ocorrencia().status());
    }

    @Test
    void tratamentoComVacinaOuEmOcorrenciaEncerradaEhRecusado() {
        OcorrenciaSanitaria o = sanidade.abrirOcorrencia(c.tecnico, lote.id(), "Diarreia", HOJE, AGORA);
        assertThrows(RegraDeNegocioException.class, () -> sanidade.registrarTratamento(c.tecnico, o.id(),
                vacina.id(), 1, HOJE, null, codigo("T-1"), AGORA));

        sanidade.encerrarOcorrencia(c.tecnico, o.id(), HOJE, AGORA);
        assertThrows(OcorrenciaEncerradaException.class, () -> sanidade.registrarTratamento(c.tecnico, o.id(),
                remedio.id(), 1, HOJE, null, codigo("T-2"), AGORA));
        assertThrows(OcorrenciaEncerradaException.class, () -> sanidade.encerrarOcorrencia(c.tecnico, o.id(),
                HOJE, AGORA));
        assertEquals(20, saldo(remedio));
    }

    @Test
    void ocorrenciaNaoAbreEmLoteEncerradoNemComDescricaoVazia() {
        assertThrows(IllegalArgumentException.class, () -> sanidade.abrirOcorrencia(c.tecnico, lote.id(), " ",
                HOJE, AGORA));
        c.app.lotes().encerrar(c.tecnico, lote.id(), MotivoEncerramento.DESCARTE, codigo("F-1"), AGORA);
        assertThrows(LoteEncerradoException.class, () -> sanidade.abrirOcorrencia(c.tecnico, lote.id(), "Tosse",
                HOJE, AGORA));
        assertTrue(sanidade.listarOcorrencias(c.gestor).isEmpty());
    }

    private Insumo insumoComEstoque(String nome, CategoriaInsumo categoria, int quantidade, String codigoEntrada) {
        Insumo insumo = c.app.estoque().cadastrarInsumo(c.gestor, nome, categoria, "un", Quantidade.de(1));
        c.app.estoque().movimentar(c.gestor, insumo.id(), TipoMovimentoEstoque.ENTRADA, quantidade,
                OrigemMovimento.COMPRA, codigo(codigoEntrada), AGORA);
        return insumo;
    }

    private int saldo(Insumo insumo) {
        return c.app.insumosRepositorio().buscarPorId(insumo.id()).orElseThrow().saldo().valor();
    }

    private MovimentoEstoque ultimoMovimento(Insumo insumo) {
        List<MovimentoEstoque> extrato = c.app.insumosRepositorio().extrato(insumo.id());
        return extrato.get(extrato.size() - 1);
    }

    private static CodigoLancamento codigo(String valor) {
        return new CodigoLancamento(valor);
    }
}
