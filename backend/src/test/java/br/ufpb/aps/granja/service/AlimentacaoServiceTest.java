package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.MovimentoEstoque;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.RegistroAlimentacao;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.LoteEncerradoException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlimentacaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 17);

    private Cenario c;
    private AlimentacaoService alimentacao;
    private Lote lote;
    private Insumo racao;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        alimentacao = c.app.alimentacao();
        lote = c.app.lotes().cadastrar(c.tecnico, codigo("L-1"), Especie.FRANGO_CORTE, Quantidade.de(100), AGORA);
        racao = c.app.estoque().cadastrarInsumo(c.gestor, "Ração", CategoriaInsumo.RACAO, "kg", Quantidade.de(10));
        c.app.estoque().movimentar(c.gestor, racao.id(), TipoMovimentoEstoque.ENTRADA, 100,
                OrigemMovimento.COMPRA, codigo("E-1"), AGORA);
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void registroDeAlimentacaoBaixaARacaoNoEstoque() {
        RegistroAlimentacao registro = registrar(30, "A-1");

        assertNotNull(registro.id());
        assertEquals(30, registro.quantidadeKg().valor());
        assertEquals(70, saldoRacao());
    }

    @Test
    void saidaDaAlimentacaoFicaNoExtratoComOrigemELote() {
        registrar(30, "A-1");

        List<MovimentoEstoque> extrato = c.app.insumosRepositorio().extrato(racao.id());
        MovimentoEstoque saida = extrato.get(extrato.size() - 1);

        assertEquals(TipoMovimentoEstoque.SAIDA, saida.tipo());
        assertEquals(OrigemMovimento.ALIMENTACAO, saida.origem());
        assertEquals(lote.id(), saida.loteId());
    }

    @Test
    void faltaDeRacaoNaoGravaNemOLancamentoNemORegistro() {
        assertThrows(RegraDeNegocioException.class, () -> registrar(150, "A-1"));

        assertEquals(100, saldoRacao());
        // o codigo A-1 nao ficou gravado, entao a mesma operacao passa com quantidade valida.
        assertNotNull(registrar(10, "A-1").id());
    }

    @Test
    void insumoQueNaoEhRacaoNaoServeParaAlimentar() {
        Insumo vacina = c.app.estoque().cadastrarInsumo(c.gestor, "Vacina", CategoriaInsumo.VACINA, "dose", Quantidade.de(5));
        c.app.estoque().movimentar(c.gestor, vacina.id(), TipoMovimentoEstoque.ENTRADA, 20,
                OrigemMovimento.COMPRA, codigo("E-2"), AGORA);

        assertThrows(RegraDeNegocioException.class, () ->
                alimentacao.registrar(c.funcionario, lote.id(), vacina.id(), 5, HOJE, codigo("A-1"), AGORA));
    }

    @Test
    void loteEncerradoNaoRecebeAlimentacaoEORestauraOEstoque() {
        c.app.lotes().encerrar(c.tecnico, lote.id(), MotivoEncerramento.VENDA, codigo("E-3"), AGORA);

        assertThrows(LoteEncerradoException.class, () -> registrar(30, "A-1"));

        assertEquals(100, saldoRacao());
    }

    @Test
    void tecnicoNaoRegistraAlimentacao() {
        assertThrows(AcessoNegadoException.class, () ->
                alimentacao.registrar(c.tecnico, lote.id(), racao.id(), 5, HOJE, codigo("A-1"), AGORA));
    }

    @Test
    void funcionarioRegistraAlimentacao() {
        RegistroAlimentacao registro = alimentacao.registrar(c.funcionario, lote.id(), racao.id(), 5, HOJE,
                codigo("A-1"), AGORA);

        assertEquals(HOJE, registro.data());
        assertTrue(saldoRacao() == 95);
    }

    private RegistroAlimentacao registrar(int kg, String codigoLancamento) {
        return alimentacao.registrar(c.funcionario, lote.id(), racao.id(), kg, HOJE, codigo(codigoLancamento), AGORA);
    }

    private int saldoRacao() {
        return c.app.estoque().listar(c.gestor).stream()
                .filter(i -> i.id().equals(racao.id()))
                .findFirst().orElseThrow().saldo().valor();
    }

    private static CodigoLancamento codigo(String valor) {
        return new CodigoLancamento(valor);
    }
}
