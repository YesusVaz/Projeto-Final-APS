package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.ManejoDto.HigienizacaoDto;
import br.ufpb.aps.granja.api.dto.ManejoDto.ProgramaDto;
import br.ufpb.aps.granja.api.dto.ProdutividadeDto;
import br.ufpb.aps.granja.model.TipoHigienizacao;
import br.ufpb.aps.granja.service.ManejoService;
import br.ufpb.aps.granja.service.ProdutividadeService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.codigo;
import static br.ufpb.aps.granja.api.CorpoJson.dataOpcional;
import static br.ufpb.aps.granja.api.CorpoJson.enumeracao;
import static br.ufpb.aps.granja.api.CorpoJson.inteiro;
import static br.ufpb.aps.granja.api.CorpoJson.textoOpcional;

/** UC14 (higienizacao), UC15 (iluminacao) e UC18 (produtividade do lote). */
final class ManejoController implements Controller {

    private final ManejoService manejo;
    private final ProdutividadeService produtividade;

    ManejoController(ManejoService manejo, ProdutividadeService produtividade) {
        this.manejo = manejo;
        this.produtividade = produtividade;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.post("/api/lotes/(\\d+)/higienizacoes", this::higienizar),
                Rota.get("/api/lotes/(\\d+)/higienizacoes", this::listarHigienizacoes),
                Rota.post("/api/lotes/(\\d+)/iluminacao", this::programar),
                Rota.get("/api/lotes/(\\d+)/iluminacao", this::listarProgramas),
                Rota.get("/api/lotes/(\\d+)/produtividade", this::produtividadeDoLote));
    }

    private Resposta higienizar(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(HigienizacaoDto.de(manejo.registrarHigienizacao(r.usuario(), r.id(),
                enumeracao(TipoHigienizacao.class, c, "tipo"), dataOpcional(c, "data"), textoOpcional(c, "observacao"),
                codigo(c), LocalDateTime.now())));
    }

    private Resposta listarHigienizacoes(Requisicao r) {
        return Resposta.ok(manejo.listarHigienizacoes(r.usuario(), r.id()).stream().map(HigienizacaoDto::de).toList());
    }

    private Resposta programar(Requisicao r) {
        JsonNode c = r.corpo();
        Integer lux = c.hasNonNull("intensidadeLux") ? inteiro(c, "intensidadeLux") : null;
        return Resposta.criado(ProgramaDto.de(manejo.registrarPrograma(r.usuario(), r.id(), inteiro(c, "horasDeLuz"),
                lux, dataOpcional(c, "dataInicio"), textoOpcional(c, "observacao"), codigo(c), LocalDateTime.now())));
    }

    private Resposta listarProgramas(Requisicao r) {
        return Resposta.ok(manejo.listarProgramas(r.usuario(), r.id()).stream().map(ProgramaDto::de).toList());
    }

    private Resposta produtividadeDoLote(Requisicao r) {
        return Resposta.ok(ProdutividadeDto.de(produtividade.consultar(r.usuario(), r.id())));
    }
}
