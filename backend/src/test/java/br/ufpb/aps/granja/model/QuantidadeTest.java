package br.ufpb.aps.granja.model;

import br.ufpb.aps.granja.model.excecao.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuantidadeTest {

    @Test
    void quantidadeNegativaERejeitadaNaCriacao() {
        assertThrows(IllegalArgumentException.class, () -> Quantidade.de(-1));
    }

    @Test
    void subtrairAteZeroPodeMasAlemDeZeroNao() {
        assertEquals(Quantidade.ZERO, Quantidade.de(5).menos(Quantidade.de(5)));
        assertThrows(SaldoInsuficienteException.class, () -> Quantidade.de(5).menos(Quantidade.de(6)));
    }

    @Test
    void codigoDeLancamentoEmBrancoERejeitado() {
        assertThrows(IllegalArgumentException.class, () -> new CodigoLancamento("  "));
    }
}
