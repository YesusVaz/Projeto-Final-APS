package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.InsumoDto;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.service.EstoqueService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.codigo;
import static br.ufpb.aps.granja.api.CorpoJson.enumeracao;
import static br.ufpb.aps.granja.api.CorpoJson.inteiro;
import static br.ufpb.aps.granja.api.CorpoJson.texto;

/** Insumos e movimentos de estoque. */
final class InsumoController implements Controller {

    private final EstoqueService estoque;

    InsumoController(EstoqueService estoque) {
        this.estoque = estoque;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.get("/api/insumos", this::listar),
                Rota.post("/api/insumos", this::cadastrar),
                Rota.get("/api/insumos/alertas", this::alertasDeEstoque),
                Rota.post("/api/insumos/(\\d+)/movimentos", this::movimentar));
    }

    private Resposta listar(Requisicao r) {
        return Resposta.ok(estoque.listar(r.usuario()).stream().map(InsumoDto::de).toList());
    }

    private Resposta cadastrar(Requisicao r) {
        JsonNode c = r.corpo();
        Insumo insumo = estoque.cadastrarInsumo(r.usuario(), texto(c, "nome"),
                enumeracao(CategoriaInsumo.class, c, "categoria"), texto(c, "unidade"),
                Quantidade.de(inteiro(c, "nivelMinimo")));
        return Resposta.criado(InsumoDto.de(insumo));
    }

    private Resposta alertasDeEstoque(Requisicao r) {
        return Resposta.ok(estoque.alertasDeEstoque(r.usuario()).stream().map(InsumoDto::de).toList());
    }

    private Resposta movimentar(Requisicao r) {
        JsonNode c = r.corpo();
        Insumo insumo = estoque.movimentar(r.usuario(), r.id(), enumeracao(TipoMovimentoEstoque.class, c, "tipo"),
                inteiro(c, "quantidade"), enumeracao(OrigemMovimento.class, c, "origem"), codigo(c),
                LocalDateTime.now());
        return Resposta.ok(InsumoDto.de(insumo));
    }
}
