package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.InsumoInativoException;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstoqueServiceTest {

    private Cenario c;
    private EstoqueService estoque;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        estoque = c.app.estoque();
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void insumoNovoComecaComSaldoZeroEAtivo() {
        Insumo insumo = cadastrarRacao("Ração inicial", 10);

        assertEquals(0, insumo.saldo().valor());
        assertTrue(insumo.ativo());
    }

    @Test
    void entradaEBaixaAtualizamOSaldo() {
        Insumo insumo = cadastrarRacao("Ração", 10);

        estoque.movimentar(c.gestor, insumo.id(), TipoMovimentoEstoque.ENTRADA, 50, OrigemMovimento.COMPRA, codigo("E-1"), AGORA);
        Insumo depois = estoque.movimentar(c.gestor, insumo.id(), TipoMovimentoEstoque.SAIDA, 20, OrigemMovimento.USO_GERAL, codigo("S-1"), AGORA);

        assertEquals(30, depois.saldo().valor());
    }

    @Test
    void saidaAcimaDoSaldoEhRecusadaSemAlterarOEstoque() {
        Insumo insumo = cadastrarRacao("Ração", 10);
        estoque.movimentar(c.gestor, insumo.id(), TipoMovimentoEstoque.ENTRADA, 50, OrigemMovimento.COMPRA, codigo("E-1"), AGORA);

        assertThrows(RegraDeNegocioException.class, () ->
                estoque.movimentar(c.gestor, insumo.id(), TipoMovimentoEstoque.SAIDA, 80, OrigemMovimento.USO_GERAL, codigo("S-1"), AGORA));

        assertEquals(50, estoque.listar(c.gestor).get(0).saldo().valor());
    }

    @Test
    void alertaDeEstoqueListaSoOsInsumosNoMinimo() {
        Insumo baixo = cadastrarRacao("Ração baixa", 10);
        Insumo alto = cadastrarRacao("Ração alta", 10);
        estoque.movimentar(c.gestor, baixo.id(), TipoMovimentoEstoque.ENTRADA, 5, OrigemMovimento.COMPRA, codigo("E-1"), AGORA);
        estoque.movimentar(c.gestor, alto.id(), TipoMovimentoEstoque.ENTRADA, 50, OrigemMovimento.COMPRA, codigo("E-2"), AGORA);

        List<Insumo> alertas = estoque.alertasDeEstoque(c.gestor);

        assertEquals(1, alertas.size());
        assertEquals("Ração baixa", alertas.get(0).nome());
    }

    @Test
    void insumoInativoNaoAceitaMovimento() {
        Insumo insumo = cadastrarRacao("Ração", 10);
        var repositorio = c.app.insumosRepositorio();
        Insumo doBanco = repositorio.buscarPorId(insumo.id()).orElseThrow();
        doBanco.inativar();
        repositorio.atualizar(doBanco);

        assertThrows(InsumoInativoException.class, () ->
                estoque.movimentar(c.gestor, insumo.id(), TipoMovimentoEstoque.ENTRADA, 5, OrigemMovimento.COMPRA, codigo("E-1"), AGORA));
    }

    @Test
    void codigoDeLancamentoJaUsadoEmOutraOperacaoEhRecusado() {
        c.app.lotes().cadastrar(c.tecnico, codigo("X-1"), Especie.FRANGO_CORTE, Quantidade.de(100), AGORA);
        Insumo insumo = cadastrarRacao("Ração", 10);

        assertThrows(LancamentoDuplicadoException.class, () ->
                estoque.movimentar(c.gestor, insumo.id(), TipoMovimentoEstoque.ENTRADA, 5, OrigemMovimento.COMPRA, codigo("X-1"), AGORA));
    }

    @Test
    void funcionarioMovimentaEstoqueMasNaoCadastraInsumo() {
        Insumo insumo = cadastrarRacao("Ração", 10);

        estoque.movimentar(c.funcionario, insumo.id(), TipoMovimentoEstoque.ENTRADA, 5, OrigemMovimento.COMPRA, codigo("E-1"), AGORA);

        assertThrows(AcessoNegadoException.class, () ->
                estoque.cadastrarInsumo(c.funcionario, "Vacina", CategoriaInsumo.VACINA, "dose", Quantidade.de(5)));
    }

    @Test
    void insumoSemNomeEhRecusado() {
        assertThrows(IllegalArgumentException.class, () ->
                estoque.cadastrarInsumo(c.gestor, " ", CategoriaInsumo.RACAO, "kg", Quantidade.de(5)));
        assertFalse(estoque.listar(c.gestor).stream().anyMatch(i -> i.nome().isBlank()));
    }

    private Insumo cadastrarRacao(String nome, int nivelMinimo) {
        return estoque.cadastrarInsumo(c.gestor, nome, CategoriaInsumo.RACAO, "kg", Quantidade.de(nivelMinimo));
    }

    private static CodigoLancamento codigo(String valor) {
        return new CodigoLancamento(valor);
    }
}
