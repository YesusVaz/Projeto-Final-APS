package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.AlertaSanitario;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** UC24 e RN17: alertas sanitarios calculados por dia, com os prazos do UC27. */
class AlertaSanitarioServiceTest {

    private Cenario c;
    private AlertaSanitarioService alertas;
    private Lote lote;
    private Insumo vacina;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        alertas = c.app.alertasSanitarios();
        lote = c.app.lotes().cadastrar(c.tecnico, codigo("L-1"), Especie.FRANGO_CORTE, Quantidade.de(100),
                LocalDate.of(2026, 11, 1), 1, AGORA);
        vacina = c.app.estoque().cadastrarInsumo(c.gestor, "Gumboro", CategoriaInsumo.VACINA, "dose", Quantidade.de(1));
        c.app.estoque().movimentar(c.gestor, vacina.id(), TipoMovimentoEstoque.ENTRADA, 500, OrigemMovimento.COMPRA,
                codigo("E-1"), AGORA);
        // vacina dia 10 com reforco dia 20
        vacinar(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), "V-1", AGORA);
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void reforcoDentroDoPrazoDeAvisoViraVacinaProxima() {
        List<AlertaSanitario> dia13 = alertas.verificar(c.tecnico, LocalDate.of(2026, 11, 13));
        assertEquals(1, dia13.size());
        assertEquals(AlertaSanitario.Tipo.VACINA_PROXIMA, dia13.get(0).tipo());
        assertEquals("LOT-2026-001", dia13.get(0).codigoLote());

        // dia 12 ainda falta mais de 7 dias
        assertTrue(alertas.verificar(c.tecnico, LocalDate.of(2026, 11, 12)).isEmpty());
        // no proprio dia ainda e aviso, nao atraso
        assertEquals(AlertaSanitario.Tipo.VACINA_PROXIMA,
                alertas.verificar(c.tecnico, LocalDate.of(2026, 11, 20)).get(0).tipo());
    }

    @Test
    void reforcoVencidoSemNovaDoseViraVacinaAtrasada() {
        assertEquals(AlertaSanitario.Tipo.VACINA_ATRASADA,
                alertas.verificar(c.gestor, LocalDate.of(2026, 11, 21)).get(0).tipo());
    }

    @Test
    void reforcoAplicadoTiraOAlerta() {
        vacinar(LocalDate.of(2026, 11, 21), null, "V-2", LocalDateTime.of(2026, 11, 21, 8, 0));

        assertTrue(alertas.verificar(c.gestor, LocalDate.of(2026, 11, 25)).isEmpty());
    }

    @Test
    void ocorrenciaAbertaNoPrazoDoParametroViraOcorrenciaProlongada() {
        OcorrenciaSanitaria o = c.app.sanidade().abrirOcorrencia(c.tecnico, lote.id(), "Coccidiose",
                LocalDate.of(2026, 11, 2), AGORA);
        c.app.parametros().configurar(c.gestor, 0, 5, AGORA);

        assertTrue(alertas.verificar(c.gestor, LocalDate.of(2026, 11, 6)).isEmpty());
        List<AlertaSanitario> dia7 = alertas.verificar(c.gestor, LocalDate.of(2026, 11, 7));
        assertEquals(1, dia7.size());
        assertEquals(AlertaSanitario.Tipo.OCORRENCIA_PROLONGADA, dia7.get(0).tipo());

        c.app.sanidade().encerrarOcorrencia(c.tecnico, o.id(), LocalDate.of(2026, 11, 8), AGORA);
        assertTrue(alertas.verificar(c.gestor, LocalDate.of(2026, 11, 9)).isEmpty());
        // olhando pro passado, no dia 7 ela ainda estava aberta
        assertEquals(1, alertas.verificar(c.gestor, LocalDate.of(2026, 11, 7)).size());
    }

    @Test
    void atrasadaVemAntesDoAvisoELoteEncerradoNaoEntra() {
        Lote outro = c.app.lotes().cadastrar(c.tecnico, codigo("L-2"), Especie.FRANGO_CORTE, Quantidade.de(50),
                LocalDate.of(2026, 11, 1), 1, AGORA);
        c.app.sanidade().registrarVacinacao(c.tecnico, outro.id(), vacina.id(), 50, LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 25), null, codigo("V-3"), AGORA);

        List<AlertaSanitario> dia21 = alertas.verificar(c.gestor, LocalDate.of(2026, 11, 21));
        assertEquals(AlertaSanitario.Tipo.VACINA_ATRASADA, dia21.get(0).tipo());
        assertEquals(AlertaSanitario.Tipo.VACINA_PROXIMA, dia21.get(1).tipo());

        c.app.lotes().encerrar(c.tecnico, lote.id(), MotivoEncerramento.VENDA, codigo("F-1"), AGORA);
        assertEquals(1, alertas.verificar(c.gestor, LocalDate.of(2026, 11, 21)).size());
    }

    @Test
    void funcionarioNaoConsultaAlertasSanitarios() {
        assertThrows(AcessoNegadoException.class, () -> alertas.verificar(c.funcionario, LocalDate.of(2026, 11, 13)));
    }

    private void vacinar(LocalDate data, LocalDate proximaDose, String codigo, LocalDateTime agora) {
        c.app.sanidade().registrarVacinacao(c.tecnico, lote.id(), vacina.id(), 100, data, proximaDose, null,
                codigo(codigo), agora);
    }

    private static CodigoLancamento codigo(String valor) {
        return new CodigoLancamento(valor);
    }
}
