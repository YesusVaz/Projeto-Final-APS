package br.ufpb.aps.granja.service;

/** RN04: codigo no formato LOT-AAAA-NNN, o sequencial recomeca todo ano. */
public final class GeradorCodigoLote {

    private GeradorCodigoLote() {
    }

    public static String gerar(int ano, int sequencial) {
        if (sequencial < 1) {
            throw new IllegalArgumentException("O sequencial começa em 1.");
        }
        return String.format("LOT-%d-%03d", ano, sequencial);
    }
}
