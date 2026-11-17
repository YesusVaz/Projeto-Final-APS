package br.ufpb.aps.granja.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** RN24: duas politicas trocaveis (Strategy) pra classificar a mortalidade por limite. */
class PoliticasDeAlertaMortalidadeTest {

    @Test
    void diariaClassificaExatamenteNosLimites() {
        PoliticaAlertaMortalidade politica = new AlertaMortalidadeDiaria(1.0, 2.0);

        assertEquals(NivelAlerta.NORMAL, politica.avaliar(new ResumoMortalidade(1000, 9, 9)));
        assertEquals(NivelAlerta.ATENCAO, politica.avaliar(new ResumoMortalidade(1000, 10, 10)), "1,0% == limite de atenção");
        assertEquals(NivelAlerta.ATENCAO, politica.avaliar(new ResumoMortalidade(1000, 19, 19)));
        assertEquals(NivelAlerta.CRITICO, politica.avaliar(new ResumoMortalidade(1000, 20, 20)), "2,0% == limite crítico");
    }

    @Test
    void acumuladaIgnoraOQueAconteceuSoNoDia() {
        PoliticaAlertaMortalidade politica = new AlertaMortalidadeAcumulada(3.0, 5.0);

        assertEquals(NivelAlerta.NORMAL, politica.avaliar(new ResumoMortalidade(1000, 20, 29)));
        assertEquals(NivelAlerta.ATENCAO, politica.avaliar(new ResumoMortalidade(1000, 0, 30)));
        assertEquals(NivelAlerta.CRITICO, politica.avaliar(new ResumoMortalidade(1000, 0, 50)));
    }

    @Test
    void mesmoResumoPodeTerNiveisDiferentesConformeAPoliticaEscolhida() {
        ResumoMortalidade resumo = new ResumoMortalidade(1000, 15, 20);

        PoliticaAlertaMortalidade diaria = new AlertaMortalidadeDiaria(1.0, 2.0);
        PoliticaAlertaMortalidade acumulada = new AlertaMortalidadeAcumulada(3.0, 5.0);

        assertEquals(NivelAlerta.ATENCAO, diaria.avaliar(resumo));
        assertEquals(NivelAlerta.NORMAL, acumulada.avaliar(resumo));
    }

    @Test
    void limitesInvertidosSaoRejeitados() {
        assertThrows(IllegalArgumentException.class, () -> new AlertaMortalidadeDiaria(2.0, 1.0));
    }
}
