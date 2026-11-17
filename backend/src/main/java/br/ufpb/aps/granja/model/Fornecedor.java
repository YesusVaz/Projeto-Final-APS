package br.ufpb.aps.granja.model;

import java.util.Objects;

/** UC10: fornecedor de insumos. O documento e unico (RN14). Telefone e e-mail sao opcionais. */
public record Fornecedor(Long id, String nome, Documento documento, String telefone, String email) {

    public Fornecedor {
        Objects.requireNonNull(documento, "documento");
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do fornecedor é obrigatório.");
        }
        nome = nome.trim();
        if (nome.length() > 120) {
            throw new IllegalArgumentException("O nome do fornecedor passou de 120 caracteres.");
        }
        telefone = vazioViraNulo(telefone);
        email = vazioViraNulo(email);
        if (email != null && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Informe um e-mail válido para o fornecedor.");
        }
    }

    private static String vazioViraNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
