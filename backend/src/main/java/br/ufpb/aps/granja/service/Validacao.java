package br.ufpb.aps.granja.service;

/** Validacoes de entrada, antes de qualquer transacao. Erro vira HTTP 422. */
final class Validacao {

    private Validacao() {
    }

    static void positiva(int valor, String campo) {
        if (valor <= 0) {
            throw new IllegalArgumentException("A " + campo + " deve ser maior que zero.");
        }
    }
}
