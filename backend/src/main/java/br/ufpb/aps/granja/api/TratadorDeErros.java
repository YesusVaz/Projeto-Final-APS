package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.AutenticacaoException;
import br.ufpb.aps.granja.model.excecao.ConflitoException;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/** Traduz cada excecao pro status HTTP (401, 403, 404, 409, 400, 422, 500). A ordem dos ifs importa. */
final class TratadorDeErros {

    private TratadorDeErros() {
    }

    static Map<String, Object> erro(String mensagem) {
        return Collections.singletonMap("erro", mensagem);
    }

    static void responder(HttpExchange ex, Exception e) throws IOException {
        if (e instanceof AutenticacaoException) {
            ex.getResponseHeaders().add("WWW-Authenticate", "Bearer realm=\"granja\"");
            Http.enviar(ex, 401, erro(e.getMessage()));
        } else if (e instanceof AcessoNegadoException) {
            Http.enviar(ex, 403, erro(e.getMessage()));
        } else if (e instanceof NaoEncontradoException) {
            Http.enviar(ex, 404, erro(e.getMessage()));
        } else if (e instanceof ConflitoException) {
            Http.enviar(ex, 409, erro(e.getMessage()));
        } else if (e instanceof JsonProcessingException) {
            Http.enviar(ex, 400, erro("Corpo JSON inválido."));
        } else if (e instanceof RegraDeNegocioException || e instanceof IllegalArgumentException) {
            Http.enviar(ex, 422, erro(e.getMessage()));
        } else {
            System.err.println("Erro interno: " + e);
            e.printStackTrace(System.err);
            Http.enviar(ex, 500, erro("Erro interno no servidor."));
        }
    }
}
