package br.ufpb.aps.granja.model;

import br.ufpb.aps.granja.model.excecao.InsumoInativoException;

import java.util.Objects;

/**
 * Insumo de estoque (racao, vacina, medicamento, material).
 * RN07: saldo nunca negativo. RN19: insumo inativo nao recebe movimento.
 */
public class Insumo {

    private final Long id;
    private final String nome;
    private final CategoriaInsumo categoria;
    private final String unidade;
    private final Quantidade nivelMinimo;
    private Quantidade saldo = Quantidade.ZERO;
    private boolean ativo = true;

    public Insumo(Long id, String nome, CategoriaInsumo categoria, String unidade, Quantidade nivelMinimo) {
        this.id = id;
        this.nome = Objects.requireNonNull(nome, "nome");
        this.categoria = Objects.requireNonNull(categoria, "categoria");
        this.unidade = Objects.requireNonNull(unidade, "unidade");
        this.nivelMinimo = Objects.requireNonNull(nivelMinimo, "nivelMinimo");
    }

    /** Monta o insumo a partir do banco. */
    public static Insumo reconstituir(Long id, String nome, CategoriaInsumo categoria, String unidade,
                                      Quantidade nivelMinimo, Quantidade saldo, boolean ativo) {
        Insumo insumo = new Insumo(id, nome, categoria, unidade, nivelMinimo);
        insumo.saldo = saldo;
        insumo.ativo = ativo;
        return insumo;
    }

    public void aplicar(MovimentoEstoque movimento) {
        if (!ativo) {
            throw new InsumoInativoException(nome);
        }
        saldo = movimento.tipo() == TipoMovimentoEstoque.ENTRADA
                ? saldo.mais(movimento.quantidade())
                : saldo.menos(movimento.quantidade());
    }

    /** UC09: alerta quando o saldo chega no minimo ou fica abaixo dele. */
    public boolean abaixoDoMinimo() {
        return saldo.compareTo(nivelMinimo) <= 0;
    }

    public void inativar() {
        ativo = false;
    }

    public Long id() { return id; }
    public String nome() { return nome; }
    public CategoriaInsumo categoria() { return categoria; }
    public String unidade() { return unidade; }
    public Quantidade nivelMinimo() { return nivelMinimo; }
    public Quantidade saldo() { return saldo; }
    public boolean ativo() { return ativo; }
}
