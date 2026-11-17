package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.policy.ConfiguracaoAlerta;
import br.ufpb.aps.granja.policy.CriterioAlerta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguracaoAlertaServiceTest {

    private Cenario c;
    private ConfiguracaoAlertaService configuracao;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        configuracao = c.app.configuracao();
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void semConfiguracaoValeOPadraoDiario() {
        ConfiguracaoAlerta atual = configuracao.consultar(c.tecnico);

        assertEquals(CriterioAlerta.DIARIO, atual.criterio());
        assertEquals(2.0, atual.atencao());
        assertEquals(5.0, atual.critico());
    }

    @Test
    void gestorAlteraOsLimites() {
        configuracao.configurar(c.gestor, CriterioAlerta.ACUMULADO, 3.0, 8.0);

        ConfiguracaoAlerta atual = configuracao.consultar(c.tecnico);

        assertEquals(CriterioAlerta.ACUMULADO, atual.criterio());
        assertEquals(3.0, atual.atencao());
        assertEquals(8.0, atual.critico());
    }

    @Test
    void tecnicoNaoAlteraOsLimites() {
        assertThrows(AcessoNegadoException.class, () ->
                configuracao.configurar(c.tecnico, CriterioAlerta.ACUMULADO, 3.0, 8.0));
    }

    @Test
    void limitesInvalidosSaoRecusadosSemGravar() {
        assertThrows(IllegalArgumentException.class, () ->
                configuracao.configurar(c.gestor, CriterioAlerta.DIARIO, 5.0, 2.0));

        assertEquals(CriterioAlerta.DIARIO, configuracao.consultar(c.gestor).criterio());
        assertEquals(2.0, configuracao.consultar(c.gestor).atencao());
    }
}
