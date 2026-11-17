package br.ufpb.aps.granja.model;

import br.ufpb.aps.granja.model.excecao.InsumoInativoException;
import br.ufpb.aps.granja.model.excecao.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Regras de estoque: RN07, RN19 e alerta de minimo (UC09). */
class InsumoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 12, 8, 0);

    private Insumo racao;

    @BeforeEach
    void criarInsumo() {
        racao = new Insumo(1L, "Ração inicial", CategoriaInsumo.RACAO, "kg", Quantidade.de(100));
    }

    private MovimentoEstoque mov(TipoMovimentoEstoque tipo, int qtd, String codigo) {
        return new MovimentoEstoque(null, 1L, tipo, Quantidade.de(qtd), new CodigoLancamento(codigo),
                OrigemMovimento.USO_GERAL, null, AGORA);
    }

    @Test
    void saidaMaiorQueSaldoEBloqueadaComMensagemClara() {
        racao.aplicar(mov(TipoMovimentoEstoque.ENTRADA, 50, "C-1"));

        SaldoInsuficienteException erro = assertThrows(SaldoInsuficienteException.class,
                () -> racao.aplicar(mov(TipoMovimentoEstoque.SAIDA, 51, "S-1")));

        assertEquals("Saldo insuficiente. Disponível: 50", erro.getMessage());
        assertEquals(50, racao.saldo().valor());
    }

    @Test
    void insumoInativoNaoRecebeEntradaNemSaida() {
        racao.aplicar(mov(TipoMovimentoEstoque.ENTRADA, 50, "C-1"));
        racao.inativar();

        assertThrows(InsumoInativoException.class, () -> racao.aplicar(mov(TipoMovimentoEstoque.ENTRADA, 10, "C-2")));
        assertThrows(InsumoInativoException.class, () -> racao.aplicar(mov(TipoMovimentoEstoque.SAIDA, 10, "S-1")));
        assertEquals(50, racao.saldo().valor());
    }

    @Test
    void alertaDeEstoqueMinimoDisparaExatamenteNoLimite() {
        racao.aplicar(mov(TipoMovimentoEstoque.ENTRADA, 101, "C-1"));
        assertFalse(racao.abaixoDoMinimo(), "101 > 100: ainda acima do mínimo");

        racao.aplicar(mov(TipoMovimentoEstoque.SAIDA, 1, "S-1"));
        assertTrue(racao.abaixoDoMinimo(), "100 == mínimo: atingiu o nível, deve alertar");
    }
}
