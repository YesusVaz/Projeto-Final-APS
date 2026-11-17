package br.ufpb.aps.granja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RN11 (taxa de postura) e RN12 (GMD). */
class IndicadoresProducaoTest {

    @Test
    void taxaDePosturaEhOvosSobreVivosVezesCem() {
        assertEquals(85.0, IndicadoresProducao.taxaDePostura(850, 1000));
        assertEquals(66.67, IndicadoresProducao.taxaDePostura(2, 3));
        assertEquals(0.0, IndicadoresProducao.taxaDePostura(0, 10));
    }

    @Test
    void maisOvosQueAvesOuLoteSemAvesSaoRecusados() {
        assertThrows(IllegalArgumentException.class, () -> IndicadoresProducao.taxaDePostura(11, 10));
        assertThrows(IllegalArgumentException.class, () -> IndicadoresProducao.taxaDePostura(1, 0));
    }

    @Test
    void gmdEhADiferencaDePesoPelosDias() {
        assertEquals(50.0, IndicadoresProducao.ganhoMedioDiario(1200, 700, 10));
        assertEquals(-5.0, IndicadoresProducao.ganhoMedioDiario(990, 1000, 2));
    }

    @Test
    void gmdSemDiasEntreAsPesagensEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> IndicadoresProducao.ganhoMedioDiario(1200, 700, 0));
    }

    @Test
    void idadeDoLoteSomaAIdadeNaChegadaComOsDiasDesdeOAlojamento() {
        Lote lote = new Lote(1L, "LOT-2026-001", Especie.FRANGO_CORTE, Quantidade.de(100),
                java.time.LocalDate.of(2026, 11, 1), 1);

        assertEquals(1, lote.idadeEmDias(java.time.LocalDate.of(2026, 11, 1)));
        assertEquals(17, lote.idadeEmDias(java.time.LocalDate.of(2026, 11, 17)));
        assertThrows(IllegalArgumentException.class, () -> lote.idadeEmDias(java.time.LocalDate.of(2026, 10, 31)));
    }
}
