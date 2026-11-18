package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.GraficosDesempenho;
import br.ufpb.aps.granja.model.GraficosDesempenho.FinanceiroDoMes;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.LancamentoFinanceiro;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.PainelGerencial;
import br.ufpb.aps.granja.model.PontoDia;
import br.ufpb.aps.granja.model.RelatorioFinanceiro;
import br.ufpb.aps.granja.model.RelatorioGerencial;
import br.ufpb.aps.granja.model.RelatorioGerencial.InsumoAbaixoDoMinimo;
import br.ufpb.aps.granja.model.RelatorioGerencial.LinhaLote;
import br.ufpb.aps.granja.model.StatusLote;
import br.ufpb.aps.granja.model.TipoFinanceiro;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.FinanceiroRepository;
import br.ufpb.aps.granja.repository.GestaoRepository;
import br.ufpb.aps.granja.repository.InsumoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UC23 (painel), UC25 (relatorio gerencial) e UC26 (graficos). So o Gestor, so leitura: aqui nao se grava nada,
 * so se junta o que os outros servicos ja guardaram. Recebem "hoje" de fora pra serem testados sem depender do relogio.
 */
public final class GestaoService {

    private static final int DIAS_DOS_GRAFICOS = 30;
    private static final int MESES_DOS_GRAFICOS = 6;

    private final LoteRepository lotes;
    private final InsumoRepository insumos;
    private final GestaoRepository gestao;
    private final FinanceiroRepository financeiro;
    private final ProdutividadeService produtividade;

    public GestaoService(LoteRepository lotes, InsumoRepository insumos, GestaoRepository gestao,
                         FinanceiroRepository financeiro, ProdutividadeService produtividade) {
        this.lotes = lotes;
        this.insumos = insumos;
        this.gestao = gestao;
        this.financeiro = financeiro;
        this.produtividade = produtividade;
    }

    public PainelGerencial painel(Usuario usuario, LocalDate hoje) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR_GESTAO);
        LocalDate inicioDoMes = hoje.withDayOfMonth(1);
        List<Lote> ativos = lotes.listar().stream().filter(Lote::estaAtivo).toList();
        RelatorioFinanceiro doMes = RelatorioFinanceiro.de(inicioDoMes, hoje, financeiro.listarPorPeriodo(inicioDoMes, hoje));
        return new PainelGerencial(ativos.size(), ativos.stream().mapToInt(l -> l.saldo().valor()).sum(),
                soma(gestao.mortesPorDia(inicioDoMes, hoje)), soma(gestao.racaoPorDia(inicioDoMes, hoje)),
                soma(gestao.ovosPorDia(inicioDoMes, hoje)), doMes.receitasCentavos(), doMes.despesasCentavos(),
                (int) insumos.listar().stream().filter(Insumo::abaixoDoMinimo).count());
    }

    public GraficosDesempenho graficos(Usuario usuario, LocalDate hoje) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR_GESTAO);
        LocalDate de = hoje.minusDays(DIAS_DOS_GRAFICOS - 1);
        YearMonth primeiroMes = YearMonth.from(hoje).minusMonths(MESES_DOS_GRAFICOS - 1);

        List<LancamentoFinanceiro> lancamentos = financeiro.listarPorPeriodo(primeiroMes.atDay(1), hoje);
        List<FinanceiroDoMes> porMes = new ArrayList<>();
        for (int i = 0; i < MESES_DOS_GRAFICOS; i++) {
            YearMonth mes = primeiroMes.plusMonths(i);
            long receitas = 0;
            long despesas = 0;
            for (LancamentoFinanceiro l : lancamentos) {
                if (YearMonth.from(l.data()).equals(mes)) {
                    if (l.tipo() == TipoFinanceiro.RECEITA) {
                        receitas += l.valorCentavos();
                    } else {
                        despesas += l.valorCentavos();
                    }
                }
            }
            porMes.add(new FinanceiroDoMes(mes, receitas, despesas));
        }
        return new GraficosDesempenho(porMes, completar(gestao.mortesPorDia(de, hoje), de, hoje),
                completar(gestao.racaoPorDia(de, hoje), de, hoje), completar(gestao.ovosPorDia(de, hoje), de, hoje));
    }

    public RelatorioGerencial relatorio(Usuario usuario, LocalDate de, LocalDate ate) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR_GESTAO);
        if (de.isAfter(ate)) {
            throw new IllegalArgumentException("A data inicial não pode ser depois da final.");
        }
        List<LancamentoFinanceiro> doPeriodo = financeiro.listarPorPeriodo(de, ate);
        RelatorioFinanceiro total = RelatorioFinanceiro.de(de, ate, doPeriodo);

        List<LinhaLote> linhas = new ArrayList<>();
        for (Lote lote : lotes.listar()) {
            long receitas = 0;
            long despesas = 0;
            for (LancamentoFinanceiro l : doPeriodo) {
                if (lote.id().equals(l.loteId())) {
                    if (l.tipo() == TipoFinanceiro.RECEITA) {
                        receitas += l.valorCentavos();
                    } else {
                        despesas += l.valorCentavos();
                    }
                }
            }
            linhas.add(new LinhaLote(produtividade.consultar(usuario, lote.id()), lote.status(), receitas, despesas));
        }
        // ativos primeiro, depois pelo codigo
        linhas.sort((a, b) -> {
            int porStatus = Boolean.compare(a.status() != StatusLote.ATIVO, b.status() != StatusLote.ATIVO);
            return porStatus != 0 ? porStatus : a.produtividade().codigoLote().compareTo(b.produtividade().codigoLote());
        });

        List<InsumoAbaixoDoMinimo> abaixo = insumos.listar().stream().filter(Insumo::abaixoDoMinimo)
                .map(i -> new InsumoAbaixoDoMinimo(i.nome(), i.unidade(), i.saldo().valor(), i.nivelMinimo().valor()))
                .toList();
        return new RelatorioGerencial(de, ate, total.receitasCentavos(), total.despesasCentavos(), linhas, abaixo);
    }

    private static int soma(List<PontoDia> pontos) {
        return pontos.stream().mapToInt(PontoDia::valor).sum();
    }

    /** Uma linha por dia do periodo, com zero onde nao houve registro. */
    private static List<PontoDia> completar(List<PontoDia> existentes, LocalDate de, LocalDate ate) {
        Map<LocalDate, Integer> porDia = new HashMap<>();
        existentes.forEach(p -> porDia.put(p.data(), p.valor()));
        List<PontoDia> completos = new ArrayList<>();
        for (LocalDate dia = de; !dia.isAfter(ate); dia = dia.plusDays(1)) {
            completos.add(new PontoDia(dia, porDia.getOrDefault(dia, 0)));
        }
        return completos;
    }
}
