package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.RegistroProducao;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.model.excecao.LoteEncerradoException;
import br.ufpb.aps.granja.model.excecao.ProducaoDuplicadaException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** UC16: postura (RN11) e pesagem com GMD (RN12). */
class ProducaoServiceTest {

    private static final LocalDate ALOJAMENTO = LocalDate.of(2026, 10, 1);

    private Cenario c;
    private ProducaoService producao;
    private Lote poedeiras;
    private Lote frangos;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        producao = c.app.producao();
        poedeiras = c.app.lotes().cadastrar(c.tecnico, codigo("L-1"), Especie.POEDEIRA, Quantidade.de(1000),
                ALOJAMENTO, 120, AGORA);
        frangos = c.app.lotes().cadastrar(c.tecnico, codigo("L-2"), Especie.FRANGO_CORTE, Quantidade.de(500),
                ALOJAMENTO, 1, AGORA);
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void taxaDePosturaUsaAsAvesVivasDaHora() {
        c.app.lotes().registrarMortalidade(c.funcionario, poedeiras.id(), 200, null, codigo("M-1"), AGORA);

        RegistroProducao r = producao.registrarPostura(c.funcionario, poedeiras.id(), 600,
                LocalDate.of(2026, 11, 10), codigo("P-1"), AGORA);

        assertEquals(800, r.vivos());
        assertEquals(75.0, r.indicador());
    }

    @Test
    void posturaEmFrangoEPesagemEmPoedeiraSaoRecusadas() {
        assertThrows(RegraDeNegocioException.class, () -> producao.registrarPostura(c.funcionario, frangos.id(), 10,
                LocalDate.of(2026, 11, 10), codigo("P-1"), AGORA));
        assertThrows(RegraDeNegocioException.class, () -> producao.registrarPesagem(c.funcionario, poedeiras.id(),
                1500, LocalDate.of(2026, 11, 10), codigo("P-2"), AGORA));
    }

    @Test
    void segundaPosturaNoMesmoDiaEhRecusadaENaoGastaOCodigo() {
        producao.registrarPostura(c.funcionario, poedeiras.id(), 800, LocalDate.of(2026, 11, 10), codigo("P-1"), AGORA);

        assertThrows(ProducaoDuplicadaException.class, () -> producao.registrarPostura(c.funcionario, poedeiras.id(),
                700, LocalDate.of(2026, 11, 10), codigo("P-2"), AGORA));
        // codigo P-2 voltou atras junto, entao vale em outro dia
        producao.registrarPostura(c.funcionario, poedeiras.id(), 700, LocalDate.of(2026, 11, 11), codigo("P-2"), AGORA);
        assertThrows(LancamentoDuplicadoException.class, () -> producao.registrarPostura(c.funcionario,
                poedeiras.id(), 700, LocalDate.of(2026, 11, 12), codigo("P-2"), AGORA));
    }

    @Test
    void pesagemCalculaOGmdContraAAnterior() {
        RegistroProducao primeira = producao.registrarPesagem(c.tecnico, frangos.id(), 700,
                LocalDate.of(2026, 10, 10), codigo("G-1"), AGORA);
        RegistroProducao segunda = producao.registrarPesagem(c.tecnico, frangos.id(), 1200,
                LocalDate.of(2026, 10, 20), codigo("G-2"), AGORA);

        assertNull(primeira.indicador());
        assertEquals(50.0, segunda.indicador());
        assertEquals(2, producao.listar(c.gestor, frangos.id()).size());
    }

    @Test
    void pesagemForaDeOrdemEhRecusada() {
        producao.registrarPesagem(c.tecnico, frangos.id(), 1200, LocalDate.of(2026, 10, 20), codigo("G-1"), AGORA);

        assertThrows(IllegalArgumentException.class, () -> producao.registrarPesagem(c.tecnico, frangos.id(), 700,
                LocalDate.of(2026, 10, 10), codigo("G-2"), AGORA));
    }

    @Test
    void loteEncerradoGestorSoConsultaEDataFuturaNaoPassam() {
        assertThrows(IllegalArgumentException.class, () -> producao.registrarPesagem(c.tecnico, frangos.id(), 700,
                AGORA.toLocalDate().plusDays(1), codigo("G-1"), AGORA));

        c.app.lotes().encerrar(c.tecnico, frangos.id(), MotivoEncerramento.VENDA, codigo("F-1"), AGORA);
        assertThrows(LoteEncerradoException.class, () -> producao.registrarPesagem(c.tecnico, frangos.id(), 700,
                LocalDate.of(2026, 10, 10), codigo("G-2"), AGORA));
    }

    @Test
    void perfilSemPermissaoNaoRegistra() {
        // o gestor tem tudo, entao o teste negativo e com um usuario inativo
        c.app.usuarios().alterarStatus(c.gestor, c.funcionario.id(), false, AGORA);
        var inativo = c.app.usuariosRepositorio().buscarPorLogin("funcionario").orElseThrow();
        assertThrows(AcessoNegadoException.class, () -> producao.registrarPostura(inativo, poedeiras.id(), 1,
                LocalDate.of(2026, 11, 10), codigo("P-1"), AGORA));
    }

    private static CodigoLancamento codigo(String valor) {
        return new CodigoLancamento(valor);
    }
}
