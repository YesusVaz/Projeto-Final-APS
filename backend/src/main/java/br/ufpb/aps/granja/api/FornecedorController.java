package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.FornecedorDto;
import br.ufpb.aps.granja.service.FornecedorService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.texto;
import static br.ufpb.aps.granja.api.CorpoJson.textoOpcional;

/** UC10: fornecedores. */
final class FornecedorController implements Controller {

    private final FornecedorService fornecedores;

    FornecedorController(FornecedorService fornecedores) {
        this.fornecedores = fornecedores;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.get("/api/fornecedores", this::listar),
                Rota.post("/api/fornecedores", this::cadastrar));
    }

    private Resposta listar(Requisicao r) {
        return Resposta.ok(fornecedores.listar(r.usuario()).stream().map(FornecedorDto::de).toList());
    }

    private Resposta cadastrar(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(FornecedorDto.de(fornecedores.cadastrar(r.usuario(), texto(c, "nome"),
                texto(c, "documento"), textoOpcional(c, "telefone"), textoOpcional(c, "email"),
                LocalDateTime.now())));
    }
}
