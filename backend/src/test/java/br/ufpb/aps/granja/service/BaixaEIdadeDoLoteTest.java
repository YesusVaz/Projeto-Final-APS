package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
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
import br.ufpb.aps.granja.model.excecao.SaldoInsuficienteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Baixa parcial do lote (venda, descarte, transferencia sem encerrar) e UC05 (idade). */
class BaixaEIdadeDoLoteTest {

    private Cenario c;
    private LoteService lotes;
    private Lote lote;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        lotes = c.app.lotes();
        lote = lotes.cadastrar(c.tecnico, codigo("L-1"), Especie.FRANGO_CORTE, Quantidade.de(100),
                LocalDate.of(2026, 11, 1), 1, AGORA);
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void baixaParcialReduzOSaldoEOLoteContinuaAtivo() {
        Lote depois = lotes.registrarBaixa(c.tecnico, lote.id(), MotivoEncerramento.VENDA, 30, "venda pro frigorífico",
                codigo("B-1"), AGORA);

        assertEquals(70, depois.saldo().valor());
        assertEquals(StatusLote.ATIVO, depois.status());
        LoteService.ConsultaLote consulta = lotes.consultar(c.gestor, lote.id());
        assertTrue(consulta.consistente());
        assertEquals(TipoMovimentoLote.BAIXA_VENDA, consulta.historico().get(1).tipo());
    }

    @Test
    void entradaAdicionalSomaNoSaldoMantemOHistoricoConsistenteEOLoteAtivo() {
        Lote depois = lotes.registrarEntrada(c.tecnico, lote.id(), 40, "nova remessa", codigo("E-1"), AGORA);

        assertEquals(140, depois.saldo().valor());
        assertEquals(100, depois.quantidadeInicial().valor());
        LoteService.ConsultaLote consulta = lotes.consultar(c.gestor, lote.id());
        assertTrue(consulta.consistente());
        assertEquals(TipoMovimentoLote.ENTRADA_ADICIONAL, consulta.historico().get(1).tipo());
    }

    @Test
    void entradaAdicionalRespeitaLoteEncerradoQuantidadeCodigoEPerfil() {
        assertThrows(IllegalArgumentException.class,
                () -> lotes.registrarEntrada(c.tecnico, lote.id(), 0, null, codigo("E-0"), AGORA));
        assertThrows(AcessoNegadoException.class,
                () -> lotes.registrarEntrada(c.funcionario, lote.id(), 5, null, codigo("E-2"), AGORA));
        lotes.registrarEntrada(c.tecnico, lote.id(), 5, null, codigo("E-3"), AGORA);
        assertThrows(LancamentoDuplicadoException.class,
                () -> lotes.registrarEntrada(c.tecnico, lote.id(), 5, null, codigo("E-3"), AGORA));
        lotes.encerrar(c.tecnico, lote.id(), MotivoEncerramento.VENDA, codigo("F-1"), AGORA);
        assertThrows(LoteEncerradoException.class,
                () -> lotes.registrarEntrada(c.tecnico, lote.id(), 5, null, codigo("E-4"), AGORA));
    }

    @Test
    void baixaMaiorQueOSaldoNaoGravaNada() {
        assertThrows(SaldoInsuficienteException.class, () -> lotes.registrarBaixa(c.tecnico, lote.id(),
                MotivoEncerramento.DESCARTE, 101, null, codigo("B-1"), AGORA));

        assertEquals(100, lotes.consultar(c.gestor, lote.id()).lote().saldo().valor());
        // o codigo nao ficou gravado, entao da pra usar de novo
        lotes.registrarBaixa(c.tecnico, lote.id(), MotivoEncerramento.DESCARTE, 1, null, codigo("B-1"), AGORA);
    }

    @Test
    void mesmoCodigoDeBaixaNaoAplicaDuasVezes() {
        lotes.registrarBaixa(c.tecnico, lote.id(), MotivoEncerramento.TRANSFERENCIA, 10, null, codigo("B-1"), AGORA);

        assertThrows(LancamentoDuplicadoException.class, () -> lotes.registrarBaixa(c.tecnico, lote.id(),
                MotivoEncerramento.TRANSFERENCIA, 10, null, codigo("B-1"), AGORA));
        assertEquals(90, lotes.consultar(c.gestor, lote.id()).lote().saldo().valor());
    }

    @Test
    void funcionarioNaoRegistraBaixaELoteEncerradoNaoRecebe() {
        assertThrows(AcessoNegadoException.class, () -> lotes.registrarBaixa(c.funcionario, lote.id(),
                MotivoEncerramento.VENDA, 1, null, codigo("B-1"), AGORA));

        lotes.encerrar(c.tecnico, lote.id(), MotivoEncerramento.VENDA, codigo("E-1"), AGORA);
        assertThrows(LoteEncerradoException.class, () -> lotes.registrarBaixa(c.tecnico, lote.id(),
                MotivoEncerramento.VENDA, 1, null, codigo("B-2"), AGORA));
    }

    @Test
    void idadeContaAPartirDoAlojamentoMaisAIdadeNaChegada() {
        LoteService.IdadeLote idade = lotes.idade(c.funcionario, lote.id(), LocalDate.of(2026, 11, 17));

        assertEquals(17, idade.idadeDias());
        assertThrows(IllegalArgumentException.class,
                () -> lotes.idade(c.gestor, lote.id(), LocalDate.of(2026, 10, 1)));
    }

    @Test
    void cadastroSemAlojamentoContaDeHojeComIdadeZero() {
        Lote novo = lotes.cadastrar(c.tecnico, codigo("L-2"), Especie.POEDEIRA, Quantidade.de(50), AGORA);

        assertEquals(AGORA.toLocalDate(), novo.dataAlojamento());
        assertEquals(0, lotes.idade(c.gestor, novo.id(), AGORA.toLocalDate()).idadeDias());
    }

    @Test
    void alojamentoNoFuturoOuIdadeNegativaSaoRecusados() {
        assertThrows(IllegalArgumentException.class, () -> lotes.cadastrar(c.tecnico, codigo("L-2"),
                Especie.POEDEIRA, Quantidade.de(50), AGORA.toLocalDate().plusDays(1), 0, AGORA));
        assertThrows(IllegalArgumentException.class, () -> lotes.cadastrar(c.tecnico, codigo("L-3"),
                Especie.POEDEIRA, Quantidade.de(50), AGORA.toLocalDate(), -1, AGORA));
    }

    private static CodigoLancamento codigo(String valor) {
        return new CodigoLancamento(valor);
    }
}
