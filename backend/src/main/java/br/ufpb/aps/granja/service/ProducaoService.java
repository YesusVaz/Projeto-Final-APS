package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.IndicadoresProducao;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.RegistroProducao;
import br.ufpb.aps.granja.model.TipoProducao;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.ProducaoDuplicadaException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import br.ufpb.aps.granja.repository.LancamentoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.ProducaoRepository;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * UC16: producao do lote. Postura so em poedeira (RN11, taxa de postura) e pesagem so em frango
 * de corte (RN12, GMD). Um registro de cada tipo por lote por dia, lote ativo (RN05) e codigo
 * de lancamento unico (RN22).
 */
public final class ProducaoService {

    private final UnidadeDeTrabalho uow;
    private final LoteRepository lotes;
    private final ProducaoRepository producao;
    private final LancamentoRepository lancamentos;
    private final BarramentoEventos eventos;

    public ProducaoService(UnidadeDeTrabalho uow, LoteRepository lotes, ProducaoRepository producao,
                           LancamentoRepository lancamentos, BarramentoEventos eventos) {
        this.uow = uow;
        this.lotes = lotes;
        this.producao = producao;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
    }

    /** RN11: guarda as aves vivas da hora do registro junto, pra taxa nao mudar depois. */
    public RegistroProducao registrarPostura(Usuario usuario, long loteId, int ovos, LocalDate data,
                                             CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_PRODUCAO);
        if (ovos < 0) {
            throw new IllegalArgumentException("A quantidade de ovos não pode ser negativa.");
        }
        Validacao.naoFutura(data, agora, "data da postura");
        RegistroProducao registro = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Lote lote = loteParaProducao(loteId, Especie.POEDEIRA, data,
                    "Postura só pode ser registrada em lote de poedeiras (RN11).");
            exigirUnicoNoDia(lote, TipoProducao.POSTURA, data);
            int vivos = lote.saldo().valor();
            double taxa = IndicadoresProducao.taxaDePostura(ovos, vivos);
            return producao.inserir(new RegistroProducao(null, loteId, TipoProducao.POSTURA, data, ovos, vivos,
                    taxa, codigo));
        });
        publicar(usuario, registro, agora);
        return registro;
    }

    /**
     * RN12: GMD contra a pesagem anterior. A primeira nao tem GMD. Pesagem fora de ordem e recusada,
     * senao o GMD das que vieram depois ficaria errado.
     */
    public RegistroProducao registrarPesagem(Usuario usuario, long loteId, int pesoMedioGramas, LocalDate data,
                                             CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_PRODUCAO);
        Validacao.positiva(pesoMedioGramas, "peso médio em gramas");
        Validacao.naoFutura(data, agora, "data da pesagem");
        RegistroProducao registro = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Lote lote = loteParaProducao(loteId, Especie.FRANGO_CORTE, data,
                    "Pesagem só pode ser registrada em lote de frango de corte (RN12).");
            exigirUnicoNoDia(lote, TipoProducao.PESAGEM, data);
            RegistroProducao anterior = ultimaPesagem(loteId);
            Double gmd = null;
            if (anterior != null) {
                if (!data.isAfter(anterior.data())) {
                    throw new IllegalArgumentException("A pesagem deve ser posterior à última registrada ("
                            + anterior.data() + ").");
                }
                gmd = IndicadoresProducao.ganhoMedioDiario(pesoMedioGramas, anterior.valor(),
                        ChronoUnit.DAYS.between(anterior.data(), data));
            }
            return producao.inserir(new RegistroProducao(null, loteId, TipoProducao.PESAGEM, data, pesoMedioGramas,
                    null, gmd, codigo));
        });
        publicar(usuario, registro, agora);
        return registro;
    }

    public List<RegistroProducao> listar(Usuario usuario, long loteId) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        Buscas.lote(lotes, loteId);
        return producao.listarPorLote(loteId);
    }

    private Lote loteParaProducao(long loteId, Especie especie, LocalDate data, String mensagemEspecie) {
        Lote lote = Buscas.lote(lotes, loteId);
        lote.exigirAtivo();
        if (lote.especie() != especie) {
            throw new RegraDeNegocioException(mensagemEspecie);
        }
        if (data.isBefore(lote.dataAlojamento())) {
            throw new IllegalArgumentException("A data é anterior ao alojamento do lote " + lote.codigo() + ".");
        }
        return lote;
    }

    private void exigirUnicoNoDia(Lote lote, TipoProducao tipo, LocalDate data) {
        if (producao.existe(lote.id(), tipo, data)) {
            throw new ProducaoDuplicadaException(tipo == TipoProducao.POSTURA ? "postura" : "pesagem",
                    lote.codigo(), data);
        }
    }

    private RegistroProducao ultimaPesagem(long loteId) {
        List<RegistroProducao> pesagens = producao.listarPorLote(loteId).stream()
                .filter(r -> r.tipo() == TipoProducao.PESAGEM)
                .toList();
        return pesagens.isEmpty() ? null : pesagens.get(pesagens.size() - 1);
    }

    private void publicar(Usuario usuario, RegistroProducao r, LocalDateTime agora) {
        String detalhe = r.tipo() == TipoProducao.POSTURA
                ? r.valor() + " ovos em " + r.data() + ", taxa " + r.indicador() + "%"
                : "peso médio " + r.valor() + " g em " + r.data() + (r.indicador() == null ? "" : ", GMD " + r.indicador() + " g/dia");
        eventos.publicar(new OperacaoRealizada("PRODUCAO", "Lote " + r.loteId() + ": " + detalhe + " por "
                + usuario.login(), agora));
    }
}
