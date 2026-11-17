package br.ufpb.aps.granja.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CriadorDePoliticaAlertaTest {

    @Test
    void criadorDiarioProduzPoliticaDiaria() {
        PoliticaAlertaMortalidade politica = new CriadorAlertaDiario().configurar(5, 10);
        assertInstanceOf(AlertaMortalidadeDiaria.class, politica);
        // 1 morte no dia em 10 animais = 10% (critico), mesmo com 30% acumulado
        assertEquals(NivelAlerta.CRITICO, politica.avaliar(new ResumoMortalidade(10, 1, 3)));
    }

    @Test
    void criadorAcumuladoProduzPoliticaAcumulada() {
        PoliticaAlertaMortalidade politica = new CriadorAlertaAcumulado().configurar(5, 10);
        assertInstanceOf(AlertaMortalidadeAcumulada.class, politica);
        // nenhuma morte no dia mas 6% acumulado: atencao
        assertEquals(NivelAlerta.ATENCAO, politica.avaliar(new ResumoMortalidade(100, 0, 6)));
    }

    @Test
    void criadorRejeitaLimitesInvertidos() {
        assertThrows(IllegalArgumentException.class, () -> new CriadorAlertaDiario().configurar(10, 5));
        assertThrows(IllegalArgumentException.class, () -> new CriadorAlertaAcumulado().configurar(10, 5));
    }
}
