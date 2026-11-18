package br.ufpb.aps.granja.api;

/** Status HTTP e corpo que sera serializado em JSON. */
record Resposta(int status, Object corpo) {

    /** Arquivo pra baixar: sai como esta, sem virar JSON. */
    record Arquivo(String nome, String tipo, byte[] bytes) {
    }

    static Resposta arquivo(String nome, String tipo, byte[] bytes) {
        return new Resposta(200, new Arquivo(nome, tipo, bytes));
    }

    static Resposta ok(Object corpo) {
        return new Resposta(200, corpo);
    }

    static Resposta criado(Object corpo) {
        return new Resposta(201, corpo);
    }
}
