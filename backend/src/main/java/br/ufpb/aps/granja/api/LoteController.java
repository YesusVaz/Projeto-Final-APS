package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.AlertaDto;
import br.ufpb.aps.granja.api.dto.HistoricoLoteDto;
import br.ufpb.aps.granja.api.dto.IdadeLoteDto;
import br.ufpb.aps.granja.api.dto.LoteDetalheDto;
import br.ufpb.aps.granja.api.dto.LoteDto;
import br.ufpb.aps.granja.api.dto.MortalidadeDto;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.service.HistoricoLoteService;
import br.ufpb.aps.granja.service.LoteService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.codigo;
import static br.ufpb.aps.granja.api.CorpoJson.dataOpcional;
import static br.ufpb.aps.granja.api.CorpoJson.enumeracao;
import static br.ufpb.aps.granja.api.CorpoJson.inteiro;
import static br.ufpb.aps.granja.api.CorpoJson.inteiroOpcional;
import static br.ufpb.aps.granja.api.CorpoJson.textoOpcional;

/** Lotes, mortalidade, baixas, idade, historico completo e os alertas de mortalidade. */
final class LoteController implements Controller {

    private final LoteService lotes;
    private final HistoricoLoteService historico;

    LoteController(LoteService lotes, HistoricoLoteService historico) {
        this.lotes = lotes;
        this.historico = historico;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.get("/api/lotes", this::listar),
                Rota.post("/api/lotes", this::cadastrar),
                Rota.get("/api/lotes/(\\d+)", this::consultar),
                Rota.post("/api/lotes/(\\d+)/encerramento", this::encerrar),
                Rota.post("/api/lotes/(\\d+)/mortalidades", this::registrarMortalidade),
                Rota.post("/api/lotes/(\\d+)/baixas", this::registrarBaixa),
                Rota.get("/api/lotes/(\\d+)/idade", this::idade),
                Rota.get("/api/lotes/(\\d+)/historico", this::historico),
                Rota.get("/api/alertas-mortalidade", this::listarAlertas));
    }

    private Resposta listar(Requisicao r) {
        return Resposta.ok(lotes.listar(r.usuario()).stream().map(LoteDto::de).toList());
    }

    private Resposta cadastrar(Requisicao r) {
        JsonNode c = r.corpo();
        // alojamento e idade na chegada sao opcionais: sem eles, chegou hoje com idade zero
        Lote lote = lotes.cadastrar(r.usuario(), codigo(c), enumeracao(Especie.class, c, "especie"),
                Quantidade.de(inteiro(c, "quantidadeInicial")), dataOpcional(c, "dataAlojamento"),
                inteiroOpcional(c, "idadeInicialDias", 0), LocalDateTime.now());
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

    /** Venda, descarte ou transferencia de parte do lote. O motivo usa os mesmos valores do encerramento. */
    private Resposta registrarBaixa(Requisicao r) {
        JsonNode c = r.corpo();
        Lote lote = lotes.registrarBaixa(r.usuario(), r.id(), enumeracao(MotivoEncerramento.class, c, "motivo"),
                inteiro(c, "quantidade"), textoOpcional(c, "observacao"), codigo(c), LocalDateTime.now());
        return Resposta.criado(LoteDto.de(lote));
    }

    /** ?data=AAAA-MM-DD opcional. Sem ela, idade de hoje. */
    private Resposta idade(Requisicao r) {
        return Resposta.ok(IdadeLoteDto.de(lotes.idade(r.usuario(), r.id(), r.dataDaConsulta("data"))));
    }

    private Resposta historico(Requisicao r) {
        return Resposta.ok(HistoricoLoteDto.de(historico.historico(r.usuario(), r.id())));
    }

    private Resposta listarAlertas(Requisicao r) {
        return Resposta.ok(lotes.alertas(r.usuario()).stream().map(AlertaDto::de).toList());
    }
}
