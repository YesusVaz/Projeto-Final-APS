package br.ufpb.aps.granja.model;

import br.ufpb.aps.granja.model.excecao.LoteEncerradoException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Lote de animais. As regras ficam aqui no dominio, nao na tela nem no Main:
 * <ul>
 *   <li>RN05: lote encerrado nao recebe mais nada;</li>
 *   <li>RN06 (revisada): saldo = soma do historico, nunca negativo;</li>
 *   <li>RN21: encerrar gera a baixa do que sobrou e nao volta atras.</li>
 * </ul>
 */
public class Lote {

    private final Long id;
    private final String codigo;
    private final Especie especie;
    private final Quantidade quantidadeInicial;
    private Quantidade saldo;
    private StatusLote status;

    public Lote(Long id, String codigo, Especie especie, Quantidade quantidadeInicial) {
        this.id = id;
        this.codigo = Objects.requireNonNull(codigo, "codigo");
        this.especie = Objects.requireNonNull(especie, "especie");
        this.quantidadeInicial = Objects.requireNonNull(quantidadeInicial, "quantidadeInicial");
        if (quantidadeInicial.ehZero()) {
            throw new IllegalArgumentException("A quantidade inicial deve ser maior que zero");
        }
        this.saldo = quantidadeInicial;
        this.status = StatusLote.ATIVO;
    }

    /** Monta o lote a partir do banco. Os dados ja passaram no cadastro, entao nao valida de novo. */
    public static Lote reconstituir(Long id, String codigo, Especie especie, Quantidade quantidadeInicial,
                                    Quantidade saldo, StatusLote status) {
        Lote lote = new Lote(id, codigo, especie, quantidadeInicial);
        lote.saldo = saldo;
        lote.status = status;
        return lote;
    }

    /** Primeiro movimento do historico. Assim o saldo sempre bate com a soma. */
    public MovimentoLote movimentoInicial(CodigoLancamento codigoLancamento, LocalDateTime agora) {
        return new MovimentoLote(null, id, TipoMovimentoLote.ENTRADA_INICIAL, quantidadeInicial, codigoLancamento, agora, null);
    }

    /** Aplica mortalidade ou baixa no saldo. Falha se o lote ja estiver encerrado ou se o saldo ficar negativo. */
    public void aplicar(MovimentoLote movimento) {
        exigirAtivo();
        if (movimento.tipo() == TipoMovimentoLote.ENTRADA_INICIAL) {
            throw new IllegalArgumentException("ENTRADA_INICIAL só existe no cadastro do lote");
        }
        saldo = movimento.tipo().sinal() > 0
                ? saldo.mais(movimento.quantidade())
                : saldo.menos(movimento.quantidade());
    }

    /**
     * Encerra o lote. Se sobrar saldo, gera a baixa do motivo (venda, descarte ou
     * transferencia) pro historico continuar batendo com o saldo, que vira zero.
     */
    public Optional<MovimentoLote> encerrar(MotivoEncerramento motivo, CodigoLancamento codigoLancamento, LocalDateTime agora) {
        exigirAtivo();
        Optional<MovimentoLote> baixa = saldo.ehZero()
                ? Optional.empty()
                : Optional.of(new MovimentoLote(null, id, motivo.tipoDeBaixa(), saldo, codigoLancamento, agora,
                        "Encerramento do lote: " + motivo));
        saldo = Quantidade.ZERO;
        status = StatusLote.ENCERRADO;
        return baixa;
    }

    /** Invariante RN06 (revisada): o saldo tem que ser igual a soma do historico. */
    public boolean consistenteCom(List<MovimentoLote> historico) {
        int soma = historico.stream().mapToInt(MovimentoLote::efeitoNoSaldo).sum();
        return soma == saldo.valor();
    }

    public boolean estaAtivo() {
        return status == StatusLote.ATIVO;
    }

    private void exigirAtivo() {
        if (!estaAtivo()) {
            throw new LoteEncerradoException(codigo);
        }
    }

    public Long id() { return id; }
    public String codigo() { return codigo; }
    public Especie especie() { return especie; }
    public Quantidade quantidadeInicial() { return quantidadeInicial; }
    public Quantidade saldo() { return saldo; }
    public StatusLote status() { return status; }
}
