package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.AuditoriaDto;
import br.ufpb.aps.granja.service.AuditoriaService;

import java.util.List;

/** UC03: so leitura. Nao existe rota que altere ou apague o log (RN15). */
final class AuditoriaController implements Controller {

    private final AuditoriaService auditoria;

    AuditoriaController(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(Rota.get("/api/auditoria", this::listar));
    }

    private Resposta listar(Requisicao r) {
        return Resposta.ok(auditoria.listar(r.usuario()).stream().map(AuditoriaDto::de).toList());
    }
}
