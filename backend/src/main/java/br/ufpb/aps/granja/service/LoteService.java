package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.MortalidadeRegistrada;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.MovimentoLote;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoMovimentoLote;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.policy.AlertaMortalidade;
import br.ufpb.aps.granja.policy.NivelAlerta;
import br.ufpb.aps.granja.policy.ResumoMortalidade;
import br.ufpb.aps.granja.repository.AlertaRepository;
import br.ufpb.aps.granja.repository.LancamentoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Casos de uso de lote: UC04 (cadastrar, consultar, encerrar, baixa parcial), UC05 (idade)
 * e UC06 (mortalidade).
 */
public final class LoteService {

    /** Resultado da mortalidade: lote atualizado e nivel de alerta avaliado. */
    public record RegistroMortalidade(Lote lote, NivelAlerta nivel) {
    }

    /** Lote com o historico completo e se bate com o saldo (RN06r). */
    public record ConsultaLote(Lote lote, List<MovimentoLote> historico, boolean consistente) {
    }

    /** UC05: idade do lote num dia. */
    public record IdadeLote(Lote lote, LocalDate dia, int idadeDias) {
    }

    private final UnidadeDeTrabalho uow;
    private final LoteRepository lotes;
    private final LancamentoRepository lancamentos;
    private final AlertaRepository alertasRepo;
    private final ConfiguracaoAlertaService configuracaoAlerta;
    private final BarramentoEventos eventos;

    public LoteService(UnidadeDeTrabalho uow, LoteRepository lotes, LancamentoRepository lancamentos,
                       AlertaRepository alertasRepo, ConfiguracaoAlertaService configuracaoAlerta,
                       BarramentoEventos eventos) {
        this.uow = uow;
        this.lotes = lotes;
        this.lancamentos = lancamentos;
        this.alertasRepo = alertasRepo;
        this.configuracaoAlerta = configuracaoAlerta;
        this.eventos = eventos;
    }

    /** UC04 sem dizer alojamento: os animais chegaram hoje, com idade zero. */
    public Lote cadastrar(Usuario usuario, CodigoLancamento codigo, Especie especie,
                          Quantidade quantidadeInicial, LocalDateTime agora) {
        return cadastrar(usuario, codigo, especie, quantidadeInicial, agora.toLocalDate(), 0, agora);
    }

    /**
     * UC04: grava o lote e o movimento inicial juntos (RN23, P03). Gera o LOT-AAAA-NNN (RN04).
     * Alojamento e idade na chegada servem pra calcular a idade depois (UC05).
     */
    public Lote cadastrar(Usuario usuario, CodigoLancamento codigo, Especie especie, Quantidade quantidadeInicial,
                          LocalDate dataAlojamento, int idadeInicialDias, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.CADASTRAR_LOTE);
        Validacao.positiva(quantidadeInicial.valor(), "quantidade inicial do lote");
        if (idadeInicialDias < 0) {
            throw new IllegalArgumentException("A idade inicial dos animais não pode ser negativa.");
        }
        if (dataAlojamento.isAfter(agora.toLocalDate())) {
            throw new IllegalArgumentException("A data de alojamento não pode estar no futuro.");
        }
        Lote novo = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            int ano = agora.getYear();
            String codigoLote = GeradorCodigoLote.gerar(ano, lotes.contarPorAno(ano) + 1);
            Lote lote = lotes.inserir(codigoLote, especie, quantidadeInicial, dataAlojamento, idadeInicialDias);
            lotes.registrarMovimento(lote.movimentoInicial(codigo, agora));
            return lote;
        });
        eventos.publicar(new OperacaoRealizada("LOTE", "Lote " + novo.codigo() + " cadastrado com "
                + quantidadeInicial.valor() + " animais (" + especie + ") por " + usuario.login(), agora));
        return novo;
    }

    /** UC04.A: encerra o lote e grava a baixa do que sobrou no historico (RN21). */
    public Lote encerrar(Usuario usuario, long loteId, MotivoEncerramento motivo,
                         CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.ENCERRAR_LOTE);
        Lote encerrado = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Lote lote = carregar(loteId);
            lote.encerrar(motivo, codigo, agora).ifPresent(lotes::registrarMovimento);
            lotes.atualizar(lote);
            return lote;
        });
        eventos.publicar(new OperacaoRealizada("ENCERRAMENTO",
                "Lote " + encerrado.codigo() + " encerrado por " + motivo + " por " + usuario.login(), agora));
        return encerrado;
    }

    /**
     * Baixa parcial: venda, descarte ou transferencia de parte dos animais sem encerrar o lote.
     * Mesmas regras da mortalidade: lote ativo (RN05), saldo nunca negativo (RN07), codigo unico (RN22).
     */
    public Lote registrarBaixa(Usuario usuario, long loteId, MotivoEncerramento motivo, int quantidade,
                               String observacao, CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_BAIXA);
        Validacao.positiva(quantidade, "quantidade da baixa");
        Lote atualizado = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Lote lote = carregar(loteId);
            MovimentoLote baixa = new MovimentoLote(null, loteId, motivo.tipoDeBaixa(), Quantidade.de(quantidade),
                    codigo, agora, observacao);
            lote.aplicar(baixa);
            lotes.registrarMovimento(baixa);
            lotes.atualizar(lote);
            return lote;
        });
        eventos.publicar(new OperacaoRealizada("BAIXA", "Lote " + atualizado.codigo() + ": baixa de " + quantidade
                + " por " + motivo + " por " + usuario.login(), agora));
        return atualizado;
    }

    /**
     * Acrescenta animais a um lote ativo (RN05). Mesmo cuidado dos outros lancamentos: codigo unico (RN22)
     * e gravacao do movimento junto com o saldo (RN23). Nao muda a data de alojamento nem a idade do lote.
     */
    public Lote registrarEntrada(Usuario usuario, long loteId, int quantidade, String observacao,
                                 CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_ENTRADA_ANIMAIS);
        Validacao.positiva(quantidade, "quantidade da entrada");
        Lote atualizado = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Lote lote = carregar(loteId);
            MovimentoLote entrada = new MovimentoLote(null, loteId, TipoMovimentoLote.ENTRADA_ADICIONAL,
                    Quantidade.de(quantidade), codigo, agora, observacao);
            lote.aplicar(entrada);
            lotes.registrarMovimento(entrada);
            lotes.atualizar(lote);
            return lote;
        });
        eventos.publicar(new OperacaoRealizada("ENTRADA", "Lote " + atualizado.codigo() + ": entrada de "
                + quantidade + " animais por " + usuario.login(), agora));
        return atualizado;
    }

    /** UC05: idade dos animais no dia pedido. */
    public IdadeLote idade(Usuario usuario, long loteId, LocalDate dia) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        Lote lote = carregar(loteId);
        return new IdadeLote(lote, dia, lote.idadeEmDias(dia));
    }

    /**
     * UC06: registra a mortalidade (RN05, RN07, RN22, RN23), avalia o alerta e so depois do commit
     * publica o evento pros ouvintes (Observer).
     */
    public RegistroMortalidade registrarMortalidade(Usuario usuario, long loteId, int quantidade, String observacao,
                                                    CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_MORTALIDADE);
        Validacao.positiva(quantidade, "quantidade de mortes");
        RegistroMortalidade registro = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Lote lote = carregar(loteId);
            MovimentoLote movimento = new MovimentoLote(null, loteId, TipoMovimentoLote.MORTALIDADE,
                    Quantidade.de(quantidade), codigo, agora, observacao);
            lote.aplicar(movimento);
            lotes.registrarMovimento(movimento);
            lotes.atualizar(lote);
            return new RegistroMortalidade(lote, avaliarAlerta(lote, agora));
        });
        eventos.publicar(new MortalidadeRegistrada(registro.lote().id(), registro.lote().codigo(),
                quantidade, registro.nivel(), agora));
        return registro;
    }

    /** UC04.B: consulta o lote, o historico e se o saldo bate com ele. */
    public ConsultaLote consultar(Usuario usuario, long loteId) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        Lote lote = carregar(loteId);
        List<MovimentoLote> historico = lotes.historico(loteId);
        return new ConsultaLote(lote, historico, lote.consistenteCom(historico));
    }

    public List<Lote> listar(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        return lotes.listar();
    }

    public List<AlertaMortalidade> alertas(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        return alertasRepo.listar();
    }

    private NivelAlerta avaliarAlerta(Lote lote, LocalDateTime agora) {
        List<MovimentoLote> historico = lotes.historico(lote.id());
        // a base da porcentagem e tudo que ja entrou no lote, nao so o que veio no cadastro
        ResumoMortalidade resumo = new ResumoMortalidade(
                totalEntrado(historico),
                somarMortes(historico, agora.toLocalDate()),
                somarMortes(historico, null));
        return configuracaoAlerta.politicaAtual().avaliar(resumo);
    }

    private static int totalEntrado(List<MovimentoLote> historico) {
        return historico.stream()
                .filter(m -> m.tipo().sinal() > 0)
                .mapToInt(m -> m.quantidade().valor())
                .sum();
    }

    /** Soma as mortes de um dia. Com {@code dia} nulo, soma todas (acumulado). */
    private static int somarMortes(List<MovimentoLote> historico, LocalDate dia) {
        return historico.stream()
                .filter(m -> m.tipo() == TipoMovimentoLote.MORTALIDADE)
                .filter(m -> dia == null || m.dataHora().toLocalDate().equals(dia))
                .mapToInt(m -> m.quantidade().valor())
                .sum();
    }

    private Lote carregar(long loteId) {
        return Buscas.lote(lotes, loteId);
    }
}
