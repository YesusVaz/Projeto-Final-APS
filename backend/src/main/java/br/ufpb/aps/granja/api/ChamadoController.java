package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.SuporteDto.ChamadoDto;
import br.ufpb.aps.granja.model.CategoriaChamado;
import br.ufpb.aps.granja.model.StatusChamado;
import br.ufpb.aps.granja.service.ChamadoService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.codigo;
import static br.ufpb.aps.granja.api.CorpoJson.enumeracao;
import static br.ufpb.aps.granja.api.CorpoJson.texto;
import static br.ufpb.aps.granja.api.CorpoJson.textoOpcional;

/** Chamados de suporte (UC30): qualquer perfil abre e ve os seus; o Gestor ve todos e responde. */
final class ChamadoController implements Controller {

    private final ChamadoService chamados;

    ChamadoController(ChamadoService chamados) {
        this.chamados = chamados;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.post("/api/chamados", this::abrir),
                Rota.get("/api/chamados", this::meus),
                Rota.get("/api/chamados/todos", this::todos),
                Rota.put("/api/chamados/(\\d+)", this::responder));
    }

    private Resposta abrir(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(ChamadoDto.de(chamados.abrir(r.usuario(), enumeracao(CategoriaChamado.class, c, "categoria"),
                texto(c, "assunto"), texto(c, "descricao"), codigo(c), LocalDateTime.now())));
    }

    private Resposta meus(Requisicao r) {
        return Resposta.ok(chamados.meus(r.usuario()).stream().map(ChamadoDto::de).toList());
    }

    private Resposta todos(Requisicao r) {
        return Resposta.ok(chamados.todos(r.usuario()).stream().map(ChamadoDto::de).toList());
    }

    private Resposta responder(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.ok(ChamadoDto.de(chamados.responder(r.usuario(), r.id(), enumeracao(StatusChamado.class, c, "status"),
                textoOpcional(c, "resposta"), LocalDateTime.now())));
    }
}
