package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.ParametrosDto;
import br.ufpb.aps.granja.service.ParametrosService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.inteiro;

/** UC27: parametros do sistema. Os limites de mortalidade ficam no ConfiguracaoAlertaController. */
final class ParametrosController implements Controller {

    private final ParametrosService parametros;

    ParametrosController(ParametrosService parametros) {
        this.parametros = parametros;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.get("/api/configuracao/parametros", this::consultar),
                Rota.put("/api/configuracao/parametros", this::configurar));
    }

    private Resposta consultar(Requisicao r) {
        return Resposta.ok(ParametrosDto.de(parametros.consultar(r.usuario())));
    }

    private Resposta configurar(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.ok(ParametrosDto.de(parametros.configurar(r.usuario(), inteiro(c, "diasAvisoVacina"),
                inteiro(c, "diasOcorrenciaAberta"), LocalDateTime.now())));
    }
}
