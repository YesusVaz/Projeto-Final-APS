package br.ufpb.aps.granja.api;

/** Status HTTP e corpo que sera serializado em JSON. */
record Resposta(int status, Object corpo) {

    static Resposta ok(Object corpo) {
        return new Resposta(200, corpo);
    }

    static Resposta criado(Object corpo) {
        return new Resposta(201, corpo);
    }
}
