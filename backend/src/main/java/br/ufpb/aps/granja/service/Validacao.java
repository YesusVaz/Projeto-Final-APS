package br.ufpb.aps.granja.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Validacoes de entrada, antes de qualquer transacao. Erro vira HTTP 422. */
final class Validacao {

    private Validacao() {
    }

    static void positiva(int valor, String campo) {
        if (valor <= 0) {
            throw new IllegalArgumentException("A " + campo + " deve ser maior que zero.");
        }
    }

    /** Registro de algo que ja aconteceu nao pode ter data de amanha. */
    static void naoFutura(LocalDate data, LocalDateTime agora, String campo) {
        if (data.isAfter(agora.toLocalDate())) {
            throw new IllegalArgumentException("A " + campo + " não pode estar no futuro.");
        }
    }
}
