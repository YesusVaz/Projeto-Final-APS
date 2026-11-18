package br.ufpb.aps.granja.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import br.ufpb.aps.granja.model.excecao.AutenticacaoException;

import java.io.IOException;
import java.io.OutputStream;

/** Leitura e escrita HTTP/JSON da API: corpo da requisicao, resposta e token do cabecalho. */
final class Http {

    private static final ObjectMapper JSON = new ObjectMapper();

    private Http() {
    }

    static JsonNode lerCorpo(HttpExchange ex) throws IOException {
        byte[] bytes = ex.getRequestBody().readAllBytes();
        return bytes.length == 0 ? null : JSON.readTree(bytes);
    }

    static void enviar(HttpExchange ex, int status, Object corpo) throws IOException {
        if (corpo instanceof Resposta.Arquivo arquivo) {
            ex.getResponseHeaders().set("Content-Type", arquivo.tipo());
            ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + arquivo.nome() + "\"");
            ex.sendResponseHeaders(status, arquivo.bytes().length);
            try (OutputStream saida = ex.getResponseBody()) {
                saida.write(arquivo.bytes());
            }
            return;
        }
        byte[] bytes = JSON.writeValueAsBytes(corpo);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = ex.getResponseBody()) {
            saida.write(bytes);
        }
    }

    static String tokenDoCabecalho(HttpExchange ex) {
        String cabecalho = ex.getRequestHeaders().getFirst("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            throw new AutenticacaoException();
        }
        return cabecalho.substring(7).trim();
    }
}
