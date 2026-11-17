package br.ufpb.aps.granja.model;

import br.ufpb.aps.granja.model.excecao.LoteEncerradoException;
import br.ufpb.aps.granja.model.excecao.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Regras de lote: RN05, RN06 (revisada) e RN21. */
class LoteTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 12, 8, 0);

    private Lote lote;

    @BeforeEach
    void criarLote() {
        lote = new Lote(1L, "LOT-2026-001", Especie.FRANGO_CORTE, Quantidade.de(300));
    }

    private MovimentoLote saida(TipoMovimentoLote tipo, int qtd, String codigo) {
        return new MovimentoLote(null, 1L, tipo, Quantidade.de(qtd), new CodigoLancamento(codigo), AGORA,
                tipo == TipoMovimentoLote.MORTALIDADE ? "doença" : null);
    }

    @Test
    void saidaMaiorQueSaldoELancaSaldoInsuficienteESemAlterarSaldo() {
        SaldoInsuficienteException erro = assertThrows(SaldoInsuficienteException.class,
                () -> lote.aplicar(saida(TipoMovimentoLote.MORTALIDADE, 301, "M-1")));

        assertEquals(300, erro.disponivel());
        assertEquals(300, lote.saldo().valor(), "o saldo não pode mudar quando a saída é rejeitada");
    }

    @Test
    void saidaIgualAoSaldoZeraSemFicarNegativo() {
        lote.aplicar(saida(TipoMovimentoLote.MORTALIDADE, 300, "M-1"));

        assertTrue(lote.saldo().ehZero());
        assertThrows(SaldoInsuficienteException.class,
                () -> lote.aplicar(saida(TipoMovimentoLote.MORTALIDADE, 1, "M-2")));
    }

    @Test
    void loteEncerradoRejeitaNovosMovimentos() {
        lote.encerrar(MotivoEncerramento.VENDA, new CodigoLancamento("E-1"), AGORA);

        assertThrows(LoteEncerradoException.class,
                () -> lote.aplicar(saida(TipoMovimentoLote.MORTALIDADE, 1, "M-1")));
    }

    @Test
    void encerrarGeraBaixaDoSaldoRestanteEFechaOLote() {
        lote.aplicar(saida(TipoMovimentoLote.MORTALIDADE, 20, "M-1"));

        Optional<MovimentoLote> baixa = lote.encerrar(MotivoEncerramento.VENDA, new CodigoLancamento("E-1"), AGORA);

        assertTrue(baixa.isPresent());
        assertEquals(TipoMovimentoLote.BAIXA_VENDA, baixa.get().tipo());
        assertEquals(280, baixa.get().quantidade().valor());
        assertEquals(StatusLote.ENCERRADO, lote.status());
        assertTrue(lote.saldo().ehZero());
    }

    @Test
    void encerrarLoteSemSaldoNaoGeraBaixa() {
        lote.aplicar(saida(TipoMovimentoLote.MORTALIDADE, 300, "M-1"));

        assertTrue(lote.encerrar(MotivoEncerramento.DESCARTE, new CodigoLancamento("E-1"), AGORA).isEmpty());
        assertFalse(lote.estaAtivo());
    }

    @Test
    void encerrarDuasVezesERejeitado() {
        lote.encerrar(MotivoEncerramento.TRANSFERENCIA, new CodigoLancamento("E-1"), AGORA);

        assertThrows(LoteEncerradoException.class,
                () -> lote.encerrar(MotivoEncerramento.VENDA, new CodigoLancamento("E-2"), AGORA));
    }

    @Test
    void saldoSempreIgualASomaAlgebricaDoHistorico() {
        List<MovimentoLote> historico = new ArrayList<>();
        historico.add(lote.movimentoInicial(new CodigoLancamento("I-1"), AGORA));

        for (MovimentoLote m : List.of(saida(TipoMovimentoLote.MORTALIDADE, 7, "M-1"),
                                       saida(TipoMovimentoLote.MORTALIDADE, 3, "M-2"),
                                       saida(TipoMovimentoLote.BAIXA_DESCARTE, 10, "B-1"))) {
            lote.aplicar(m);
            historico.add(m);
        }
        assertEquals(280, lote.saldo().valor());
        assertTrue(lote.consistenteCom(historico));

        lote.encerrar(MotivoEncerramento.VENDA, new CodigoLancamento("E-1"), AGORA).ifPresent(historico::add);
        assertTrue(lote.consistenteCom(historico), "após o encerramento o histórico também deve fechar em zero");
    }

    @Test
    void historicoIncompletoEDetectadoComoInconsistente() {
        lote.aplicar(saida(TipoMovimentoLote.MORTALIDADE, 5, "M-1"));

        List<MovimentoLote> semAMortalidade = List.of(lote.movimentoInicial(new CodigoLancamento("I-1"), AGORA));

        assertFalse(lote.consistenteCom(semAMortalidade));
    }
}
