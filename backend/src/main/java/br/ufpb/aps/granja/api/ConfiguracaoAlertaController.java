package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.ConfiguracaoAlertaDto;
import br.ufpb.aps.granja.policy.ConfiguracaoAlerta;
import br.ufpb.aps.granja.policy.CriterioAlerta;
import br.ufpb.aps.granja.service.ConfiguracaoAlertaService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.enumeracao;
import static br.ufpb.aps.granja.api.CorpoJson.real;

/** Configuracao dos limites do alerta de mortalidade. */
final class ConfiguracaoAlertaController implements Controller {

    private final ConfiguracaoAlertaService configuracao;

    ConfiguracaoAlertaController(ConfiguracaoAlertaService configuracao) {
        this.configuracao = configuracao;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.get("/api/configuracao/alerta", this::consultar),
                Rota.put("/api/configuracao/alerta", this::configurar));
    }

    private Resposta consultar(Requisicao r) {
        return Resposta.ok(ConfiguracaoAlertaDto.de(configuracao.consultar(r.usuario())));
    }

    private Resposta configurar(Requisicao r) {
        JsonNode c = r.corpo();
        ConfiguracaoAlerta atual = configuracao.configurar(r.usuario(), enumeracao(CriterioAlerta.class, c, "criterio"),
                real(c, "atencao"), real(c, "critico"));
        return Resposta.ok(ConfiguracaoAlertaDto.de(atual));
    }
}
