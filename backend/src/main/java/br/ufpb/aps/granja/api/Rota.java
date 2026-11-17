package br.ufpb.aps.granja.api;

import java.util.regex.Pattern;

/** Uma rota: metodo, caminho (regex, os grupos viram parametros) e o handler. Rota publica nao exige token. */
record Rota(String metodo, Pattern caminho, boolean publica, Handler handler) {

    @FunctionalInterface
    interface Handler {
        Resposta tratar(Requisicao requisicao);
    }

    static Rota get(String regex, Handler handler) {
        return new Rota("GET", Pattern.compile(regex), false, handler);
    }

    static Rota post(String regex, Handler handler) {
        return new Rota("POST", Pattern.compile(regex), false, handler);
    }

    static Rota put(String regex, Handler handler) {
        return new Rota("PUT", Pattern.compile(regex), false, handler);
    }

    static Rota postPublica(String regex, Handler handler) {
        return new Rota("POST", Pattern.compile(regex), true, handler);
    }
}
