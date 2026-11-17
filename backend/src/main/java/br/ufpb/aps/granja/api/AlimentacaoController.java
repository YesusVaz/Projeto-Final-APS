package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.AlimentacaoDto;
import br.ufpb.aps.granja.model.RegistroAlimentacao;
import br.ufpb.aps.granja.service.AlimentacaoService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.codigo;
import static br.ufpb.aps.granja.api.CorpoJson.dataOpcional;
import static br.ufpb.aps.granja.api.CorpoJson.inteiro;
import static br.ufpb.aps.granja.api.CorpoJson.longo;

/** Registro de alimentacao do lote. */
final class AlimentacaoController implements Controller {

    private final AlimentacaoService alimentacao;

    AlimentacaoController(AlimentacaoService alimentacao) {
        this.alimentacao = alimentacao;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(Rota.post("/api/lotes/(\\d+)/alimentacoes", this::registrar));
    }

    private Resposta registrar(Requisicao r) {
        JsonNode c = r.corpo();
        RegistroAlimentacao registro = alimentacao.registrar(r.usuario(), r.id(), longo(c, "insumoId"),
                inteiro(c, "quantidadeKg"), dataOpcional(c, "data"), codigo(c), LocalDateTime.now());
        return Resposta.criado(AlimentacaoDto.de(registro));
    }
}
