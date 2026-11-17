package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.model.Usuario;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/** Tudo que o handler precisa da requisicao. {@code usuario} e {@code token} sao nulos nas rotas publicas. */
record Requisicao(Usuario usuario, String token, List<String> parametros, JsonNode corpo) {

    /** Id numerico capturado no caminho da rota. */
    long id() {
        return Long.parseLong(parametros.get(0));
    }
}
