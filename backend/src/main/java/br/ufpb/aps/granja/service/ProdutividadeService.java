package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.IndicadoresProducao;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MovimentoLote;
import br.ufpb.aps.granja.model.ProdutividadeLote;
import br.ufpb.aps.granja.model.RegistroAlimentacao;
import br.ufpb.aps.granja.model.RegistroProducao;
import br.ufpb.aps.granja.model.TipoMovimentoLote;
import br.ufpb.aps.granja.model.TipoProducao;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.AlimentacaoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.ProducaoRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * UC18: produtividade de um lote, so pro Gestor. So le e calcula, nao grava nada.
 * <p>
 * Frango de corte: o ganho de peso vai da primeira pesagem ate a ultima (peso medio x aves vivas hoje), e a
 * racao conta so o que foi servida depois da primeira pesagem, ate a ultima (RN20). Poedeira: a racao conta
 * do primeiro ao ultimo dia de postura registrado, os dois incluidos, pra casar com os ovos contados.
 * Sem dados suficientes o indicador fica nulo.
 */
public final class ProdutividadeService {

    private final LoteRepository lotes;
    private final AlimentacaoRepository alimentacoes;
    private final ProducaoRepository producao;

    public ProdutividadeService(LoteRepository lotes, AlimentacaoRepository alimentacoes, ProducaoRepository producao) {
        this.lotes = lotes;
        this.alimentacoes = alimentacoes;
        this.producao = producao;
    }

    public ProdutividadeLote consultar(Usuario usuario, long loteId) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR_PRODUTIVIDADE);
        Lote lote = Buscas.lote(lotes, loteId);
        List<MovimentoLote> movimentos = lotes.historico(loteId);
        int entrado = movimentos.stream()
                .filter(m -> m.tipo() == TipoMovimentoLote.ENTRADA_INICIAL || m.tipo() == TipoMovimentoLote.ENTRADA_ADICIONAL)
                .mapToInt(m -> m.quantidade().valor()).sum();
        int mortes = movimentos.stream().filter(m -> m.tipo() == TipoMovimentoLote.MORTALIDADE)
                .mapToInt(m -> m.quantidade().valor()).sum();
        double taxaDeMortalidade = entrado > 0 ? IndicadoresProducao.arredondar(mortes * 100.0 / entrado) : 0;

        List<RegistroAlimentacao> racoes = alimentacoes.listarPorLote(loteId);
        int racaoTotal = racoes.stream().mapToInt(a -> a.quantidadeKg().valor()).sum();
        List<RegistroProducao> registros = producao.listarPorLote(loteId);

        Double ganho = null;
        Double gmdMedio = null;
        Double conversao = null;
        Integer ovos = null;
        Double taxaMedia = null;
        Double racaoPorDuzia = null;

        if (lote.especie() == Especie.FRANGO_CORTE) {
            List<RegistroProducao> pesagens = registros.stream().filter(r -> r.tipo() == TipoProducao.PESAGEM).toList();
            if (pesagens.size() >= 2) {
                RegistroProducao primeira = pesagens.get(0);
                RegistroProducao ultima = pesagens.get(pesagens.size() - 1);
                ganho = IndicadoresProducao.arredondar((ultima.valor() - primeira.valor()) * lote.saldo().valor() / 1000.0);
                gmdMedio = media(pesagens);
                double racaoDoPeriodo = racoes.stream()
                        .filter(a -> a.data().isAfter(primeira.data()) && !a.data().isAfter(ultima.data()))
                        .mapToInt(a -> a.quantidadeKg().valor()).sum();
                if (ganho > 0 && racaoDoPeriodo > 0) {
                    conversao = IndicadoresProducao.conversaoAlimentar(racaoDoPeriodo, ganho);
                }
            }
        } else {
            List<RegistroProducao> posturas = registros.stream().filter(r -> r.tipo() == TipoProducao.POSTURA).toList();
            if (!posturas.isEmpty()) {
                LocalDate inicio = posturas.get(0).data();
                LocalDate fim = posturas.get(posturas.size() - 1).data();
                ovos = posturas.stream().mapToInt(RegistroProducao::valor).sum();
                taxaMedia = media(posturas);
                double racaoDoPeriodo = racoes.stream()
                        .filter(a -> !a.data().isBefore(inicio) && !a.data().isAfter(fim))
                        .mapToInt(a -> a.quantidadeKg().valor()).sum();
                if (ovos > 0 && racaoDoPeriodo > 0) {
                    racaoPorDuzia = IndicadoresProducao.racaoPorDuzia(racaoDoPeriodo, ovos);
                }
            }
        }
        return new ProdutividadeLote(loteId, lote.codigo(), lote.especie(), entrado, lote.saldo().valor(), mortes,
                taxaDeMortalidade, racaoTotal, ganho, gmdMedio, conversao, ovos, taxaMedia, racaoPorDuzia);
    }

    /** Media dos indicadores que existem. Sem nenhum, nulo. */
    private static Double media(List<RegistroProducao> registros) {
        return registros.stream().map(RegistroProducao::indicador).filter(i -> i != null)
                .mapToDouble(Double::doubleValue).average().stream().boxed()
                .map(IndicadoresProducao::arredondar).findFirst().orElse(null);
    }
}
