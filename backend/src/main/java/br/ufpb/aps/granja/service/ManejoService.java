package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Higienizacao;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.ProgramaIluminacao;
import br.ufpb.aps.granja.model.TipoHigienizacao;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.LancamentoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.ManejoRepository;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UC14 (higienizacao) e UC15 (programa de iluminacao). Os dois so valem em lote ativo (RN05), com data que nao
 * seja futura nem anterior ao alojamento, e com codigo de lancamento unico (RN22).
 */
public final class ManejoService {

    private final UnidadeDeTrabalho uow;
    private final LoteRepository lotes;
    private final ManejoRepository manejo;
    private final LancamentoRepository lancamentos;
    private final BarramentoEventos eventos;

    public ManejoService(UnidadeDeTrabalho uow, LoteRepository lotes, ManejoRepository manejo,
                         LancamentoRepository lancamentos, BarramentoEventos eventos) {
        this.uow = uow;
        this.lotes = lotes;
        this.manejo = manejo;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
    }

    public Higienizacao registrarHigienizacao(Usuario usuario, long loteId, TipoHigienizacao tipo, LocalDate data,
                                              String observacao, CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_HIGIENIZACAO);
        Validacao.naoFutura(data, agora, "data da higienização");
        Higienizacao gravada = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Lote lote = loteParaManejo(loteId, data);
            return manejo.inserirHigienizacao(new Higienizacao(null, lote.id(), tipo, data, observacao, codigo));
        });
        eventos.publicar(new OperacaoRealizada("HIGIENIZACAO", "Lote " + loteId + ": " + gravada.tipo()
                + " em " + gravada.data() + " por " + usuario.login(), agora));
        return gravada;
    }

    public ProgramaIluminacao registrarPrograma(Usuario usuario, long loteId, int horasDeLuz, Integer intensidadeLux,
                                                LocalDate dataInicio, String observacao, CodigoLancamento codigo,
                                                LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_ILUMINACAO);
        Validacao.naoFutura(dataInicio, agora, "data de início do programa");
        ProgramaIluminacao gravado = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Lote lote = loteParaManejo(loteId, dataInicio);
            return manejo.inserirPrograma(new ProgramaIluminacao(null, lote.id(), horasDeLuz, intensidadeLux,
                    dataInicio, observacao, codigo));
        });
        eventos.publicar(new OperacaoRealizada("ILUMINACAO", "Lote " + loteId + ": " + gravado.horasDeLuz()
                + " h de luz por dia a partir de " + gravado.dataInicio() + " por " + usuario.login(), agora));
        return gravado;
    }

    public List<Higienizacao> listarHigienizacoes(Usuario usuario, long loteId) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        Buscas.lote(lotes, loteId);
        return manejo.higienizacoesDoLote(loteId);
    }

    /** Do mais antigo pro mais novo. O ultimo e o que esta em vigor. */
    public List<ProgramaIluminacao> listarProgramas(Usuario usuario, long loteId) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        Buscas.lote(lotes, loteId);
        return manejo.programasDoLote(loteId);
    }

    private Lote loteParaManejo(long loteId, LocalDate data) {
        Lote lote = Buscas.lote(lotes, loteId);
        lote.exigirAtivo();
        if (data.isBefore(lote.dataAlojamento())) {
            throw new IllegalArgumentException("A data é anterior ao alojamento do lote " + lote.codigo() + ".");
        }
        return lote;
    }
}
