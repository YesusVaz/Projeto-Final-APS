package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MovimentoLote;
import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.RegistroAlimentacao;
import br.ufpb.aps.granja.model.RegistroProducao;
import br.ufpb.aps.granja.model.TipoMovimentoLote;
import br.ufpb.aps.granja.model.TipoProducao;
import br.ufpb.aps.granja.model.Tratamento;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.Vacinacao;
import br.ufpb.aps.granja.repository.AlimentacaoRepository;
import br.ufpb.aps.granja.repository.InsumoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.ProducaoRepository;
import br.ufpb.aps.granja.repository.SanidadeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UC17: tudo que aconteceu com o lote numa lista so, em ordem de data. Junta movimentos, alimentacao,
 * vacinas, ocorrencias, tratamentos e producao. So le, nao grava nada.
 */
public final class HistoricoLoteService {

    /** {@code hora} so existe pras mortes e baixas. O resto e registrado por dia. */
    public record ItemHistorico(LocalDate data, LocalTime hora, String categoria, String descricao) {
    }

    public record HistoricoCompleto(Lote lote, boolean consistente, List<ItemHistorico> itens) {
    }

    private final LoteRepository lotes;
    private final AlimentacaoRepository alimentacoes;
    private final SanidadeRepository sanidade;
    private final ProducaoRepository producao;
    private final InsumoRepository insumos;

    public HistoricoLoteService(LoteRepository lotes, AlimentacaoRepository alimentacoes, SanidadeRepository sanidade,
                                ProducaoRepository producao, InsumoRepository insumos) {
        this.lotes = lotes;
        this.alimentacoes = alimentacoes;
        this.sanidade = sanidade;
        this.producao = producao;
        this.insumos = insumos;
    }

    public HistoricoCompleto historico(Usuario usuario, long loteId) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        Lote lote = Buscas.lote(lotes, loteId);
        List<MovimentoLote> movimentos = lotes.historico(loteId);
        Map<Long, String> nomes = new HashMap<>();
        List<ItemHistorico> itens = new ArrayList<>();

        for (MovimentoLote m : movimentos) {
            if (m.tipo() == TipoMovimentoLote.ENTRADA_INICIAL) {
                // a entrada vale no dia que os animais chegaram, nao no dia que alguem cadastrou
                itens.add(new ItemHistorico(lote.dataAlojamento(), null, "MOVIMENTO", descreverMovimento(m)));
            } else {
                itens.add(new ItemHistorico(m.dataHora().toLocalDate(), m.dataHora().toLocalTime(), "MOVIMENTO",
                        descreverMovimento(m)));
            }
        }
        for (RegistroAlimentacao a : alimentacoes.listarPorLote(loteId)) {
            itens.add(new ItemHistorico(a.data(), null, "ALIMENTACAO",
                    a.quantidadeKg().valor() + " kg de " + nome(nomes, a.insumoId())));
        }
        for (Vacinacao v : sanidade.vacinacoesDoLote(loteId)) {
            itens.add(new ItemHistorico(v.data(), null, "VACINACAO", v.doses().valor() + " doses de "
                    + nome(nomes, v.insumoId()) + (v.proximaDose() == null ? "" : ", reforço em " + v.proximaDose())));
        }
        for (OcorrenciaSanitaria o : sanidade.ocorrenciasDoLote(loteId)) {
            itens.add(new ItemHistorico(o.dataAbertura(), null, "OCORRENCIA",
                    "Ocorrência " + o.id() + " aberta: " + o.descricao()));
            for (Tratamento t : sanidade.tratamentosDaOcorrencia(o.id())) {
                itens.add(new ItemHistorico(t.data(), null, "TRATAMENTO", "Ocorrência " + o.id() + ": "
                        + t.quantidade().valor() + " de " + nome(nomes, t.insumoId())));
            }
            if (!o.aberta()) {
                itens.add(new ItemHistorico(o.dataEncerramento(), null, "OCORRENCIA",
                        "Ocorrência " + o.id() + " encerrada"));
            }
        }
        for (RegistroProducao p : producao.listarPorLote(loteId)) {
            itens.add(new ItemHistorico(p.data(), null, "PRODUCAO", descreverProducao(p)));
        }

        itens.sort(Comparator.comparing(ItemHistorico::data)
                .thenComparingInt(HistoricoLoteService::posicaoNoDia)
                .thenComparing(ItemHistorico::hora, Comparator.nullsFirst(Comparator.naturalOrder())));
        return new HistoricoCompleto(lote, lote.consistenteCom(movimentos), itens);
    }

    /**
     * Dentro do mesmo dia: a entrada do lote vem primeiro, depois o que foi registrado por dia,
     * e por ultimo mortes e baixas pela hora. Assim o encerramento fica no fim do dia.
     */
    private static int posicaoNoDia(ItemHistorico item) {
        if (item.descricao().startsWith("Entrada")) {
            return 0;
        }
        return item.hora() == null ? 1 : 2;
    }

    private static String descreverMovimento(MovimentoLote m) {
        String texto = switch (m.tipo()) {
            case ENTRADA_INICIAL -> "Entrada inicial de " + m.quantidade().valor() + " animais";
            case MORTALIDADE -> m.quantidade().valor() + " mortes";
            default -> "Baixa por " + nomeDaBaixa(m.tipo()) + " de " + m.quantidade().valor() + " animais";
        };
        return m.observacao() == null || m.observacao().isBlank() ? texto : texto + " (" + m.observacao() + ")";
    }

    private static String nomeDaBaixa(TipoMovimentoLote tipo) {
        return switch (tipo) {
            case BAIXA_VENDA -> "venda";
            case BAIXA_DESCARTE -> "descarte";
            case BAIXA_TRANSFERENCIA -> "transferência";
            default -> tipo.name();
        };
    }

    private static String descreverProducao(RegistroProducao p) {
        if (p.tipo() == TipoProducao.POSTURA) {
            return p.valor() + " ovos, taxa de postura " + p.indicador() + "%";
        }
        return "Peso médio " + p.valor() + " g" + (p.indicador() == null ? "" : ", GMD " + p.indicador() + " g/dia");
    }

    private String nome(Map<Long, String> cache, long insumoId) {
        return cache.computeIfAbsent(insumoId, id -> insumos.buscarPorId(id).map(Insumo::nome).orElse("insumo " + id));
    }
}
