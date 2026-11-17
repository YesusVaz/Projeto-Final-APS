package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.AlertaSanitarioDto;
import br.ufpb.aps.granja.api.dto.OcorrenciaDto;
import br.ufpb.aps.granja.api.dto.TratamentoDto;
import br.ufpb.aps.granja.api.dto.VacinacaoDto;
import br.ufpb.aps.granja.service.AlertaSanitarioService;
import br.ufpb.aps.granja.service.SanidadeService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.codigo;
import static br.ufpb.aps.granja.api.CorpoJson.dataOpcional;
import static br.ufpb.aps.granja.api.CorpoJson.dataOuNula;
import static br.ufpb.aps.granja.api.CorpoJson.inteiro;
import static br.ufpb.aps.granja.api.CorpoJson.longo;
import static br.ufpb.aps.granja.api.CorpoJson.texto;
import static br.ufpb.aps.granja.api.CorpoJson.textoOpcional;

/** Sanidade: vacinas (UC11), ocorrencias e tratamentos (UC12) e alertas sanitarios (UC24). */
final class SanidadeController implements Controller {

    private final SanidadeService sanidade;
    private final AlertaSanitarioService alertas;

    SanidadeController(SanidadeService sanidade, AlertaSanitarioService alertas) {
        this.sanidade = sanidade;
        this.alertas = alertas;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.post("/api/lotes/(\\d+)/vacinacoes", this::vacinar),
                Rota.get("/api/lotes/(\\d+)/vacinacoes", this::vacinacoes),
                Rota.post("/api/lotes/(\\d+)/ocorrencias", this::abrirOcorrencia),
                Rota.get("/api/ocorrencias", this::listarOcorrencias),
                Rota.get("/api/ocorrencias/(\\d+)", this::consultarOcorrencia),
                Rota.post("/api/ocorrencias/(\\d+)/tratamentos", this::tratar),
                Rota.post("/api/ocorrencias/(\\d+)/encerramento", this::encerrarOcorrencia),
                Rota.get("/api/alertas-sanitarios", this::alertasSanitarios));
    }

    private Resposta vacinar(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(VacinacaoDto.de(sanidade.registrarVacinacao(r.usuario(), r.id(), longo(c, "insumoId"),
                inteiro(c, "doses"), dataOpcional(c, "data"), dataOuNula(c, "proximaDose"),
                textoOpcional(c, "observacao"), codigo(c), LocalDateTime.now())));
    }

    private Resposta vacinacoes(Requisicao r) {
        return Resposta.ok(sanidade.vacinacoesDoLote(r.usuario(), r.id()).stream().map(VacinacaoDto::de).toList());
    }

    private Resposta abrirOcorrencia(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(OcorrenciaDto.de(sanidade.abrirOcorrencia(r.usuario(), r.id(), texto(c, "descricao"),
                dataOpcional(c, "data"), LocalDateTime.now())));
    }

    private Resposta listarOcorrencias(Requisicao r) {
        return Resposta.ok(sanidade.listarOcorrencias(r.usuario()).stream().map(OcorrenciaDto::de).toList());
    }

    private Resposta consultarOcorrencia(Requisicao r) {
        return Resposta.ok(OcorrenciaDto.de(sanidade.consultarOcorrencia(r.usuario(), r.id())));
    }

    private Resposta tratar(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(TratamentoDto.de(sanidade.registrarTratamento(r.usuario(), r.id(),
                longo(c, "insumoId"), inteiro(c, "quantidade"), dataOpcional(c, "data"),
                textoOpcional(c, "observacao"), codigo(c), LocalDateTime.now())));
    }

    private Resposta encerrarOcorrencia(Requisicao r) {
        return Resposta.ok(OcorrenciaDto.de(sanidade.encerrarOcorrencia(r.usuario(), r.id(),
                dataOpcional(r.corpo(), "data"), LocalDateTime.now())));
    }

    /** ?data=AAAA-MM-DD opcional. Sem ela, verifica hoje (RN17). */
    private Resposta alertasSanitarios(Requisicao r) {
        return Resposta.ok(alertas.verificar(r.usuario(), r.dataDaConsulta("data")).stream()
                .map(AlertaSanitarioDto::de).toList());
    }
}
