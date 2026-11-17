package br.ufpb.aps.granja.model;

/**
 * CPF ou CNPJ do fornecedor (RN14). Guarda so os digitos e confere os digitos verificadores,
 * entao "123.456.789-09" e "12345678909" viram o mesmo documento.
 */
public record Documento(String numero) {

    private static final int[] PESOS_CNPJ_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Documento {
        if (numero == null || !(numero.matches("\\d{11}") || numero.matches("\\d{14}"))) {
            throw new IllegalArgumentException("O documento deve ser um CPF (11 dígitos) ou um CNPJ (14 dígitos).");
        }
        // 111.111.111-11 passa na conta dos digitos mas nao e documento de verdade
        if (numero.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF ou CNPJ inválido.");
        }
        boolean valido = numero.length() == 11 ? cpfValido(numero) : cnpjValido(numero);
        if (!valido) {
            throw new IllegalArgumentException("CPF ou CNPJ inválido.");
        }
    }

    /** Aceita com ou sem pontuacao. */
    public static Documento de(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("O CPF ou CNPJ é obrigatório.");
        }
        return new Documento(texto.replaceAll("[.\\-/\\s]", ""));
    }

    public boolean ehCpf() {
        return numero.length() == 11;
    }

    private static boolean cpfValido(String cpf) {
        int[] d = digitos(cpf);
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += d[i] * (10 - i);
        }
        if (verificador(soma) != d[9]) {
            return false;
        }
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += d[i] * (11 - i);
        }
        return verificador(soma) == d[10];
    }

    private static boolean cnpjValido(String cnpj) {
        int[] d = digitos(cnpj);
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += d[i] * PESOS_CNPJ_1[i];
        }
        if (verificador(soma) != d[12]) {
            return false;
        }
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += d[i] * PESOS_CNPJ_2[i];
        }
        return verificador(soma) == d[13];
    }

    private static int verificador(int soma) {
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int[] digitos(String texto) {
        return texto.chars().map(c -> c - '0').toArray();
    }
}
