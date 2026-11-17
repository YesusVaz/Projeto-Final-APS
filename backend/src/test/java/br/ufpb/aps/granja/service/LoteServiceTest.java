package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.Composicao;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.StatusLote;
import br.ufpb.aps.granja.model.TipoMovimentoLote;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.model.excecao.LoteEncerradoException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import br.ufpb.aps.granja.policy.AlertaMortalidade;
import br.ufpb.aps.granja.policy.CriterioAlerta;
import br.ufpb.aps.granja.policy.NivelAlerta;
import br.ufpb.aps.granja.repository.AuditoriaRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoteServiceTest {

    private Cenario c;
    private LoteService lotes;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        lotes = c.app.lotes();
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void cadastraLotesComCodigoSequencialESaldoIgualAoInicial() {
        Lote primeiro = cadastrar("L-1", 100);
        Lote segundo = cadastrar("L-2", 50);

        assertEquals("LOT-2026-001", primeiro.codigo());
        assertEquals("LOT-2026-002", segundo.codigo());
        assertEquals(100, primeiro.saldo().valor());
    }

    @Test
    void cadastroGravaOMovimentoInicialNoHistorico() {
        Lote lote = cadastrar("L-1", 100);

        LoteService.ConsultaLote consulta = lotes.consultar(c.gestor, lote.id());

        assertEquals(1, consulta.historico().size());
        assertEquals(TipoMovimentoLote.ENTRADA_INICIAL, consulta.historico().get(0).tipo());
        assertTrue(consulta.consistente());
    }

    @Test
    void funcionarioNaoCadastraLote() {
        assertThrows(AcessoNegadoException.class, () ->
                lotes.cadastrar(c.funcionario, codigo("L-1"), Especie.FRANGO_CORTE, Quantidade.de(100), AGORA));
    }

    @Test
    void quantidadeInicialZeroEhRecusada() {
        assertThrows(IllegalArgumentException.class, () -> cadastrar("L-1", 0));
    }

    @Test
    void codigoDeLancamentoRepetidoNaoCriaSegundoLote() {
        cadastrar("L-1", 100);

        assertThrows(LancamentoDuplicadoException.class, () -> cadastrar("L-1", 20));
        assertEquals(1, lotes.listar(c.gestor).size());
    }

    @Test
    void falhaNoMeioDoCadastroDesfazOLoteELOLancamento() {
        Composicao.Aplicacao comFalha = Composicao.criar(c.banco, LoteServiceTest::falharEmRegistrarMovimento);

        assertThrows(IllegalStateException.class, () ->
                comFalha.lotes().cadastrar(c.tecnico, codigo("L-9"), Especie.FRANGO_CORTE, Quantidade.de(100), AGORA));

        assertTrue(lotes.listar(c.gestor).isEmpty());
        // o codigo nao ficou gravado, entao a mesma operacao passa sem dar duplicado.
        assertEquals("LOT-2026-001", cadastrar("L-9", 100).codigo());
    }

    @Test
    void mortalidadeReduzSaldoEGravaNoHistorico() {
        Lote lote = cadastrar("L-1", 100);

        LoteService.RegistroMortalidade registro =
                lotes.registrarMortalidade(c.funcionario, lote.id(), 3, "doença", codigo("M-1"), AGORA);

        assertEquals(97, registro.lote().saldo().valor());
        LoteService.ConsultaLote consulta = lotes.consultar(c.gestor, lote.id());
        assertEquals(2, consulta.historico().size());
        assertEquals("doença", consulta.historico().get(1).observacao());
        assertTrue(consulta.consistente());
    }

    @Test
    void mortalidadeAcimaDoSaldoEhRecusadaSemAlterarNada() {
        Lote lote = cadastrar("L-1", 10);

        assertThrows(RegraDeNegocioException.class, () ->
                lotes.registrarMortalidade(c.funcionario, lote.id(), 11, null, codigo("M-1"), AGORA));

        assertEquals(10, lotes.consultar(c.gestor, lote.id()).lote().saldo().valor());
        assertEquals(1, lotes.consultar(c.gestor, lote.id()).historico().size());
    }

    @Test
    void mortalidadeComQuantidadeZeroEhRecusada() {
        Lote lote = cadastrar("L-1", 10);

        assertThrows(IllegalArgumentException.class, () ->
                lotes.registrarMortalidade(c.funcionario, lote.id(), 0, null, codigo("M-1"), AGORA));
    }

    @Test
    void mortalidadeAbaixoDoLimiteNaoGeraAlerta() {
        Lote lote = cadastrar("L-1", 100);

        LoteService.RegistroMortalidade registro =
                lotes.registrarMortalidade(c.funcionario, lote.id(), 1, null, codigo("M-1"), AGORA);

        assertEquals(NivelAlerta.NORMAL, registro.nivel());
        assertTrue(lotes.alertas(c.gestor).isEmpty());
    }

    @Test
    void mortalidadeAcimaDoAtencaoGeraAlertaDeAtencao() {
        Lote lote = cadastrar("L-1", 100);

        LoteService.RegistroMortalidade registro =
                lotes.registrarMortalidade(c.funcionario, lote.id(), 3, null, codigo("M-1"), AGORA);

        assertEquals(NivelAlerta.ATENCAO, registro.nivel());
        List<AlertaMortalidade> alertas = lotes.alertas(c.gestor);
        assertEquals(1, alertas.size());
        assertEquals(NivelAlerta.ATENCAO, alertas.get(0).nivel());
        assertEquals("LOT-2026-001", alertas.get(0).codigoLote());
    }

    @Test
    void mortalidadeAcimaDoCriticoGeraAlertaCritico() {
        Lote lote = cadastrar("L-1", 100);

        LoteService.RegistroMortalidade registro =
                lotes.registrarMortalidade(c.funcionario, lote.id(), 6, null, codigo("M-1"), AGORA);

        assertEquals(NivelAlerta.CRITICO, registro.nivel());
    }

    @Test
    void mortalidadeRegistradaEntraNoLogDeAuditoria() {
        Lote lote = cadastrar("L-1", 100);

        lotes.registrarMortalidade(c.funcionario, lote.id(), 3, "doença", codigo("M-1"), AGORA);

        List<AuditoriaRepository.Entrada> entradas = c.app.auditoria().listar();
        assertEquals(1, entradas.size());
        assertEquals("MORTALIDADE", entradas.get(0).categoria());
        assertTrue(entradas.get(0).detalhe().contains("LOT-2026-001"));
    }

    @Test
    void criterioAcumuladoSomaAsMortesDeDiasDiferentes() {
        c.app.configuracao().configurar(c.gestor, CriterioAlerta.ACUMULADO, 2.0, 5.0);
        Lote lote = cadastrar("L-1", 100);
        lotes.registrarMortalidade(c.funcionario, lote.id(), 1, null, codigo("M-1"), AGORA);
        lotes.registrarMortalidade(c.funcionario, lote.id(), 1, null, codigo("M-2"), AGORA.plusDays(1));

        LoteService.RegistroMortalidade terceiro =
                lotes.registrarMortalidade(c.funcionario, lote.id(), 1, null, codigo("M-3"), AGORA.plusDays(2));

        // cada dia tem 1%, mas o acumulado chega a 3%: pelo criterio acumulado da atencao.
        assertEquals(NivelAlerta.ATENCAO, terceiro.nivel());
    }

    @Test
    void encerrarGravaABaixaDoSaldoRestanteERecusaNovasOperacoes() {
        Lote lote = cadastrar("L-1", 100);
        lotes.registrarMortalidade(c.funcionario, lote.id(), 4, "doença", codigo("M-1"), AGORA);

        Lote encerrado = lotes.encerrar(c.tecnico, lote.id(), MotivoEncerramento.VENDA, codigo("E-1"), AGORA);

        assertEquals(StatusLote.ENCERRADO, encerrado.status());
        assertEquals(0, encerrado.saldo().valor());
        LoteService.ConsultaLote consulta = lotes.consultar(c.gestor, lote.id());
        assertTrue(consulta.consistente());
        assertEquals(TipoMovimentoLote.BAIXA_VENDA, consulta.historico().get(consulta.historico().size() - 1).tipo());
        assertThrows(LoteEncerradoException.class, () ->
                lotes.registrarMortalidade(c.funcionario, lote.id(), 1, null, codigo("M-2"), AGORA));
    }

    @Test
    void consultaDeLoteInexistenteFalha() {
        assertThrows(br.ufpb.aps.granja.model.excecao.NaoEncontradoException.class,
                () -> lotes.consultar(c.gestor, 999L));
    }

    private Lote cadastrar(String codigoLancamento, int quantidadeInicial) {
        return lotes.cadastrar(c.tecnico, codigo(codigoLancamento), Especie.FRANGO_CORTE,
                Quantidade.de(quantidadeInicial), AGORA);
    }

    private static CodigoLancamento codigo(String valor) {
        return new CodigoLancamento(valor);
    }

    /** Embrulha o repositorio de lotes e falha ao gravar o movimento, depois do lote ja inserido. */
    private static LoteRepository falharEmRegistrarMovimento(LoteRepository original) {
        return (LoteRepository) Proxy.newProxyInstance(
                LoteRepository.class.getClassLoader(),
                new Class<?>[]{LoteRepository.class},
                (proxy, metodo, argumentos) -> {
                    if (metodo.getName().equals("registrarMovimento")) {
                        throw new IllegalStateException("falha simulada ao gravar o movimento");
                    }
                    try {
                        return metodo.invoke(original, argumentos);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
