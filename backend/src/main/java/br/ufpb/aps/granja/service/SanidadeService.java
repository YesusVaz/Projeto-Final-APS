package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MovimentoEstoque;
import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.Tratamento;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.Vacinacao;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import br.ufpb.aps.granja.repository.InsumoRepository;
import br.ufpb.aps.granja.repository.LancamentoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.SanidadeRepository;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sanidade: UC11 (vacinacao, RN08) e UC12 (ocorrencia e tratamento, RN09).
 * Vacina e tratamento gastam o insumo no estoque na mesma transacao do registro (RN23):
 * se faltar estoque, nada e gravado, nem o codigo do lancamento.
 */
public final class SanidadeService {

    /** Ocorrencia com os tratamentos dela. */
    public record ConsultaOcorrencia(OcorrenciaSanitaria ocorrencia, List<Tratamento> tratamentos) {
    }

    private final UnidadeDeTrabalho uow;
    private final LoteRepository lotes;
    private final InsumoRepository insumos;
    private final SanidadeRepository sanidade;
    private final LancamentoRepository lancamentos;
    private final BarramentoEventos eventos;

    public SanidadeService(UnidadeDeTrabalho uow, LoteRepository lotes, InsumoRepository insumos,
                           SanidadeRepository sanidade, LancamentoRepository lancamentos, BarramentoEventos eventos) {
        this.uow = uow;
        this.lotes = lotes;
        this.insumos = insumos;
        this.sanidade = sanidade;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
    }

    /** UC11: aplica a vacina no lote e baixa as doses do estoque (RN08). */
    public Vacinacao registrarVacinacao(Usuario usuario, long loteId, long insumoId, int doses, LocalDate data,
                                        LocalDate proximaDose, String observacao, CodigoLancamento codigo,
                                        LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_VACINACAO);
        Validacao.positiva(doses, "quantidade de doses");
        Validacao.naoFutura(data, agora, "data da vacinação");
        // monta antes da transacao: proxima dose antes da data ja cai aqui como 422
        Vacinacao nova = new Vacinacao(null, loteId, insumoId, Quantidade.de(doses), data, proximaDose, codigo,
                observacao);
        Vacinacao gravada = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Lote lote = loteAtivo(loteId);
            exigirDepoisDoAlojamento(lote, data);
            baixarEstoque(insumoId, CategoriaInsumo.VACINA, doses, OrigemMovimento.VACINACAO, loteId, codigo, agora,
                    "Só insumo da categoria VACINA pode ser usado na vacinação (RN08).");
            return sanidade.inserirVacinacao(nova);
        });
        eventos.publicar(new OperacaoRealizada("VACINACAO", "Lote " + codigoDoLote(loteId) + ": " + doses
                + " doses do insumo "
                + insumoId + " por " + usuario.login(), agora));
        return gravada;
    }

    public List<Vacinacao> vacinacoesDoLote(Usuario usuario, long loteId) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        Buscas.lote(lotes, loteId);
        return sanidade.vacinacoesDoLote(loteId);
    }

    /** UC12: abre a ocorrencia. Nao mexe em saldo nenhum, entao nao precisa de codigo de lancamento. */
    public OcorrenciaSanitaria abrirOcorrencia(Usuario usuario, long loteId, String descricao, LocalDate data,
                                               LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_OCORRENCIA);
        Validacao.naoFutura(data, agora, "data da ocorrência");
        OcorrenciaSanitaria nova = new OcorrenciaSanitaria(null, loteId, descricao, data);
        OcorrenciaSanitaria gravada = uow.executar(() -> {
            Lote lote = loteAtivo(loteId);
            exigirDepoisDoAlojamento(lote, data);
            return sanidade.inserirOcorrencia(nova);
        });
        eventos.publicar(new OperacaoRealizada("OCORRENCIA", "Ocorrência " + gravada.id() + " aberta no lote "
                + codigoDoLote(loteId) + ": " + gravada.descricao() + " por " + usuario.login(), agora));
        return gravada;
    }

    /** UC12: medicamento numa ocorrencia aberta. Baixa o medicamento do estoque (RN09). */
    public Tratamento registrarTratamento(Usuario usuario, long ocorrenciaId, long insumoId, int quantidade,
                                          LocalDate data, String observacao, CodigoLancamento codigo,
                                          LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_OCORRENCIA);
        Validacao.positiva(quantidade, "quantidade do medicamento");
        Validacao.naoFutura(data, agora, "data do tratamento");
        Tratamento novo = new Tratamento(null, ocorrenciaId, insumoId, Quantidade.de(quantidade), data, codigo,
                observacao);
        Tratamento gravado = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            OcorrenciaSanitaria ocorrencia = Buscas.ocorrencia(sanidade, ocorrenciaId);
            ocorrencia.exigirAberta();
            if (data.isBefore(ocorrencia.dataAbertura())) {
                throw new IllegalArgumentException("O tratamento não pode ser anterior à abertura da ocorrência.");
            }
            loteAtivo(ocorrencia.loteId());
            baixarEstoque(insumoId, CategoriaInsumo.MEDICAMENTO, quantidade, OrigemMovimento.TRATAMENTO,
                    ocorrencia.loteId(), codigo, agora,
                    "Só insumo da categoria MEDICAMENTO pode ser usado no tratamento (RN09).");
            return sanidade.inserirTratamento(novo);
        });
        eventos.publicar(new OperacaoRealizada("TRATAMENTO", "Ocorrência " + ocorrenciaId + ": " + quantidade
                + " do insumo " + insumoId + " por " + usuario.login(), agora));
        return gravado;
    }

    /** UC12: fecha a ocorrencia. Nao reabre. */
    public OcorrenciaSanitaria encerrarOcorrencia(Usuario usuario, long ocorrenciaId, LocalDate data,
                                                  LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_OCORRENCIA);
        Validacao.naoFutura(data, agora, "data de encerramento");
        OcorrenciaSanitaria encerrada = uow.executar(() -> {
            OcorrenciaSanitaria ocorrencia = Buscas.ocorrencia(sanidade, ocorrenciaId);
            ocorrencia.encerrar(data);
            sanidade.atualizarOcorrencia(ocorrencia);
            return ocorrencia;
        });
        eventos.publicar(new OperacaoRealizada("OCORRENCIA", "Ocorrência " + ocorrenciaId + " encerrada por "
                + usuario.login(), agora));
        return encerrada;
    }

    public List<OcorrenciaSanitaria> listarOcorrencias(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        return sanidade.listarOcorrencias();
    }

    public ConsultaOcorrencia consultarOcorrencia(Usuario usuario, long ocorrenciaId) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        OcorrenciaSanitaria ocorrencia = Buscas.ocorrencia(sanidade, ocorrenciaId);
        return new ConsultaOcorrencia(ocorrencia, sanidade.tratamentosDaOcorrencia(ocorrenciaId));
    }

    private String codigoDoLote(long loteId) {
        return Buscas.lote(lotes, loteId).codigo();
    }

    private Lote loteAtivo(long loteId) {
        Lote lote = Buscas.lote(lotes, loteId);
        lote.exigirAtivo();
        return lote;
    }

    private static void exigirDepoisDoAlojamento(Lote lote, LocalDate data) {
        if (data.isBefore(lote.dataAlojamento())) {
            throw new IllegalArgumentException("A data é anterior ao alojamento do lote " + lote.codigo() + ".");
        }
    }

    /** Saida automatica no estoque, ligada ao lote. Mesmo caminho da alimentacao (RN10). */
    private void baixarEstoque(long insumoId, CategoriaInsumo categoriaEsperada, int quantidade,
                               OrigemMovimento origem, long loteId, CodigoLancamento codigo, LocalDateTime agora,
                               String mensagemCategoria) {
        Insumo insumo = Buscas.insumo(insumos, insumoId);
        if (insumo.categoria() != categoriaEsperada) {
            throw new RegraDeNegocioException(mensagemCategoria);
        }
        MovimentoEstoque saida = new MovimentoEstoque(null, insumoId, TipoMovimentoEstoque.SAIDA,
                Quantidade.de(quantidade), codigo, origem, loteId, agora);
        insumo.aplicar(saida);
        insumos.registrarMovimento(saida);
        insumos.atualizar(insumo);
    }
}
