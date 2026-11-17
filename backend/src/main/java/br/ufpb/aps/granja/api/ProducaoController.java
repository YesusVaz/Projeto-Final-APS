package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.ProducaoDto;
import br.ufpb.aps.granja.service.ProducaoService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.codigo;
import static br.ufpb.aps.granja.api.CorpoJson.dataOpcional;
import static br.ufpb.aps.granja.api.CorpoJson.inteiro;

/** UC16: postura (poedeira) e pesagem (frango de corte). */
final class ProducaoController implements Controller {

    private final ProducaoService producao;

    ProducaoController(ProducaoService producao) {
        this.producao = producao;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.post("/api/lotes/(\\d+)/posturas", this::postura),
                Rota.post("/api/lotes/(\\d+)/pesagens", this::pesagem),
                Rota.get("/api/lotes/(\\d+)/producao", this::listar));
    }

    private Resposta postura(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(ProducaoDto.de(producao.registrarPostura(r.usuario(), r.id(), inteiro(c, "ovos"),
                dataOpcional(c, "data"), codigo(c), LocalDateTime.now())));
    }

    private Resposta pesagem(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(ProducaoDto.de(producao.registrarPesagem(r.usuario(), r.id(),
                inteiro(c, "pesoMedioGramas"), dataOpcional(c, "data"), codigo(c), LocalDateTime.now())));
    }

    private Resposta listar(Requisicao r) {
        return Resposta.ok(producao.listar(r.usuario(), r.id()).stream().map(ProducaoDto::de).toList());
    }
}
