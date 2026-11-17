package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.AlertaDto;
import br.ufpb.aps.granja.api.dto.LoteDetalheDto;
import br.ufpb.aps.granja.api.dto.LoteDto;
import br.ufpb.aps.granja.api.dto.MortalidadeDto;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.service.LoteService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.codigo;
import static br.ufpb.aps.granja.api.CorpoJson.enumeracao;
import static br.ufpb.aps.granja.api.CorpoJson.inteiro;
import static br.ufpb.aps.granja.api.CorpoJson.textoOpcional;

/** Lotes, mortalidade e os alertas de mortalidade. */
final class LoteController implements Controller {

    private final LoteService lotes;

    LoteController(LoteService lotes) {
        this.lotes = lotes;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.get("/api/lotes", this::listar),
                Rota.post("/api/lotes", this::cadastrar),
                Rota.get("/api/lotes/(\\d+)", this::consultar),
                Rota.post("/api/lotes/(\\d+)/encerramento", this::encerrar),
                Rota.post("/api/lotes/(\\d+)/mortalidades", this::registrarMortalidade),
                Rota.get("/api/alertas-mortalidade", this::listarAlertas));
    }

    private Resposta listar(Requisicao r) {
        return Resposta.ok(lotes.listar(r.usuario()).stream().map(LoteDto::de).toList());
    }

    private Resposta cadastrar(Requisicao r) {
        JsonNode c = r.corpo();
        Lote lote = lotes.cadastrar(r.usuario(), codigo(c), enumeracao(Especie.class, c, "especie"),
                Quantidade.de(inteiro(c, "quantidadeInicial")), LocalDateTime.now());
        return Resposta.criado(LoteDto.de(lote));
    }

    private Resposta consultar(Requisicao r) {
        return Resposta.ok(LoteDetalheDto.de(lotes.consultar(r.usuario(), r.id())));
    }

    private Resposta encerrar(Requisicao r) {
        JsonNode c = r.corpo();
        Lote lote = lotes.encerrar(r.usuario(), r.id(), enumeracao(MotivoEncerramento.class, c, "motivo"),
                codigo(c), LocalDateTime.now());
        return Resposta.ok(LoteDto.de(lote));
    }

    private Resposta registrarMortalidade(Requisicao r) {
        JsonNode c = r.corpo();
        LoteService.RegistroMortalidade registro = lotes.registrarMortalidade(r.usuario(), r.id(),
                inteiro(c, "quantidade"), textoOpcional(c, "observacao"), codigo(c), LocalDateTime.now());
        return Resposta.criado(MortalidadeDto.de(registro));
    }

    private Resposta listarAlertas(Requisicao r) {
        return Resposta.ok(lotes.alertas(r.usuario()).stream().map(AlertaDto::de).toList());
    }
}
