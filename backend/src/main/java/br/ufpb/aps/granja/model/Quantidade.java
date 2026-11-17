package br.ufpb.aps.granja.model;

import br.ufpb.aps.granja.model.excecao.SaldoInsuficienteException;

/**
 * Valor pra quantidades inteiras (animais, kg, unidades). Nunca negativa.
 * Tirar mais do que tem lanca {@link SaldoInsuficienteException}.
 */
public record Quantidade(int valor) implements Comparable<Quantidade> {

    public static final Quantidade ZERO = new Quantidade(0);

    public Quantidade {
        if (valor < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa: " + valor);
        }
    }

    public static Quantidade de(int valor) {
        return new Quantidade(valor);
    }

    public Quantidade mais(Quantidade outra) {
        return new Quantidade(Math.addExact(valor, outra.valor));
    }

    public Quantidade menos(Quantidade outra) {
        if (outra.valor > valor) {
            throw new SaldoInsuficienteException(valor);
        }
        return new Quantidade(valor - outra.valor);
    }

    public boolean ehZero() {
        return valor == 0;
    }

    @Override
    public int compareTo(Quantidade outra) {
        return Integer.compare(valor, outra.valor);
    }
}
