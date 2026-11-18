package br.ufpb.aps.granja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RN20: conversao alimentar = racao consumida / peso ganho. */
class ConversaoAlimentarTest {

    @Test
    void divideARacaoPeloPesoGanhoComDuasCasas() {
        assertEquals(1.67, IndicadoresProducao.conversaoAlimentar(2500, 1500));
        assertEquals(2.0, IndicadoresProducao.conversaoAlimentar(1000, 500));
    }

    @Test
    void semGanhoDePesoNaoHaConversao() {
        assertThrows(IllegalArgumentException.class, () -> IndicadoresProducao.conversaoAlimentar(100, 0));
        assertThrows(IllegalArgumentException.class, () -> IndicadoresProducao.conversaoAlimentar(100, -5));
        assertThrows(IllegalArgumentException.class, () -> IndicadoresProducao.conversaoAlimentar(-1, 10));
    }

    @Test
    void racaoPorDuziaUsaOsOvosDivididosPorDoze() {
        assertEquals(2.0, IndicadoresProducao.racaoPorDuzia(100, 600));
        assertThrows(IllegalArgumentException.class, () -> IndicadoresProducao.racaoPorDuzia(100, 0));
    }
}
