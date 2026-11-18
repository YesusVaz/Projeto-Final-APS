package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.CategoriaFinanceira;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Fornecedor;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.LancamentoFinanceiro;
import br.ufpb.aps.granja.model.MovimentoEstoque;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.RelatorioFinanceiro;
import br.ufpb.aps.granja.model.TipoFinanceiro;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.FinanceiroRepository;
import br.ufpb.aps.granja.repository.FornecedorRepository;
import br.ufpb.aps.granja.repository.InsumoRepository;
import br.ufpb.aps.granja.repository.LancamentoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UC19 (compra), UC20 (despesa operacional), UC21 (receita de venda) e UC22 (relatorio). So o Gestor.
 * A compra e tudo-ou-nada (RN13, RN23): entrada no estoque e despesa na mesma transacao, com um unico
 * codigo de lancamento (RN22). Dinheiro sempre em centavos inteiros.
 */
public final class FinanceiroService {

    private final UnidadeDeTrabalho uow;
    private final FinanceiroRepository financeiro;
    private final InsumoRepository insumos;
    private final FornecedorRepository fornecedores;
    private final LoteRepository lotes;
    private final LancamentoRepository lancamentos;
    private final BarramentoEventos eventos;

    public FinanceiroService(UnidadeDeTrabalho uow, FinanceiroRepository financeiro, InsumoRepository insumos,
                             FornecedorRepository fornecedores, LoteRepository lotes,
                             LancamentoRepository lancamentos, BarramentoEventos eventos) {
        this.uow = uow;
        this.financeiro = financeiro;
        this.insumos = insumos;
        this.fornecedores = fornecedores;
        this.lotes = lotes;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
    }

    /** UC19: compra de insumo de um fornecedor. Entra no estoque e vira despesa de COMPRA_INSUMO (RN13). */
    public LancamentoFinanceiro registrarCompra(Usuario usuario, long insumoId, long fornecedorId, int quantidade,
                                                long valorCentavos, LocalDate data, CodigoLancamento codigo,
                                                LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.GERENCIAR_FINANCEIRO);
        Validacao.positiva(quantidade, "quantidade comprada");
        validarValor(valorCentavos);
        Validacao.naoFutura(data, agora, "data da compra");
        LancamentoFinanceiro gravado = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Insumo insumo = Buscas.insumo(insumos, insumoId);
            Fornecedor fornecedor = Buscas.fornecedor(fornecedores, fornecedorId);
            MovimentoEstoque movimento = new MovimentoEstoque(null, insumoId, TipoMovimentoEstoque.ENTRADA,
                    Quantidade.de(quantidade), codigo, OrigemMovimento.COMPRA, null, agora);
            insumo.aplicar(movimento);
            insumos.registrarMovimento(movimento);
            insumos.atualizar(insumo);
            String descricao = "Compra de " + quantidade + " " + insumo.unidade() + " de " + insumo.nome()
                    + " de " + fornecedor.nome();
            return financeiro.inserir(new LancamentoFinanceiro(null, CategoriaFinanceira.COMPRA_INSUMO,
                    descricao, valorCentavos, data, null, fornecedorId, insumoId, quantidade, codigo));
        });
        publicar(usuario, "Compra", gravado, agora);
        return gravado;
    }

    /** UC20: despesa que nao e compra de insumo (mao de obra, energia, manutencao...). Pode ser de um lote. */
    public LancamentoFinanceiro registrarDespesa(Usuario usuario, CategoriaFinanceira categoria, String descricao,
                                                 long valorCentavos, LocalDate data, Long loteId,
                                                 CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.GERENCIAR_FINANCEIRO);
        if (categoria == null || categoria.tipo() != TipoFinanceiro.DESPESA) {
            throw new IllegalArgumentException("Escolha uma categoria de despesa.");
        }
        if (categoria == CategoriaFinanceira.COMPRA_INSUMO) {
            throw new IllegalArgumentException("Compra de insumo se registra como compra, pra entrar no estoque.");
        }
        return lancar(usuario, "Despesa", categoria, descricao, valorCentavos, data, loteId, codigo, agora);
    }

    /** UC21: receita de venda (ovos, aves) ou outra receita. Pode ser de um lote, ate de um encerrado. */
    public LancamentoFinanceiro registrarReceita(Usuario usuario, CategoriaFinanceira categoria, String descricao,
                                                 long valorCentavos, LocalDate data, Long loteId,
                                                 CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.GERENCIAR_FINANCEIRO);
        if (categoria == null || categoria.tipo() != TipoFinanceiro.RECEITA) {
            throw new IllegalArgumentException("Escolha uma categoria de receita.");
        }
        return lancar(usuario, "Receita", categoria, descricao, valorCentavos, data, loteId, codigo, agora);
    }

    /** UC22: o que entrou e saiu entre as duas datas (incluidas). Com {@code loteId}, so o que e daquele lote. */
    public RelatorioFinanceiro relatorio(Usuario usuario, LocalDate de, LocalDate ate, Long loteId) {
        Autorizacao.exigir(usuario, Acao.GERENCIAR_FINANCEIRO);
        if (de.isAfter(ate)) {
            throw new IllegalArgumentException("A data inicial não pode ser depois da final.");
        }
        List<LancamentoFinanceiro> doPeriodo = financeiro.listarPorPeriodo(de, ate);
        if (loteId != null) {
            Buscas.lote(lotes, loteId);
            doPeriodo = doPeriodo.stream().filter(l -> loteId.equals(l.loteId())).toList();
        }
        return RelatorioFinanceiro.de(de, ate, doPeriodo);
    }

    private LancamentoFinanceiro lancar(Usuario usuario, String rotulo, CategoriaFinanceira categoria,
                                        String descricao, long valorCentavos, LocalDate data, Long loteId,
                                        CodigoLancamento codigo, LocalDateTime agora) {
        validarValor(valorCentavos);
        Validacao.naoFutura(data, agora, "data do lançamento");
        LancamentoFinanceiro novo = new LancamentoFinanceiro(null, categoria, descricao, valorCentavos, data,
                loteId, null, null, null, codigo);
        LancamentoFinanceiro gravado = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            if (loteId != null) {
                Buscas.lote(lotes, loteId);
            }
            return financeiro.inserir(novo);
        });
        publicar(usuario, rotulo, gravado, agora);
        return gravado;
    }

    private static void validarValor(long valorCentavos) {
        if (valorCentavos <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
    }

    private void publicar(Usuario usuario, String rotulo, LancamentoFinanceiro l, LocalDateTime agora) {
        eventos.publicar(new OperacaoRealizada("FINANCEIRO", rotulo + " de " + moeda(l.valorCentavos()) + ": "
                + l.descricao() + " por " + usuario.login(), agora));
    }

    static String moeda(long centavos) {
        return String.format("R$ %d,%02d", centavos / 100, centavos % 100);
    }
}
