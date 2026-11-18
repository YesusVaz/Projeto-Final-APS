package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.LancamentoFinanceiroDto;
import br.ufpb.aps.granja.api.dto.RelatorioFinanceiroDto;
import br.ufpb.aps.granja.model.CategoriaFinanceira;
import br.ufpb.aps.granja.service.FinanceiroService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.codigo;
import static br.ufpb.aps.granja.api.CorpoJson.dataOpcional;
import static br.ufpb.aps.granja.api.CorpoJson.enumeracao;
import static br.ufpb.aps.granja.api.CorpoJson.inteiro;
import static br.ufpb.aps.granja.api.CorpoJson.longo;
import static br.ufpb.aps.granja.api.CorpoJson.texto;

/** UC19 a UC22: compras, despesas, receitas e relatorio financeiro. */
final class FinanceiroController implements Controller {

    private final FinanceiroService financeiro;

    FinanceiroController(FinanceiroService financeiro) {
        this.financeiro = financeiro;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.post("/api/compras", this::compra),
                Rota.post("/api/despesas", this::despesa),
                Rota.post("/api/receitas", this::receita),
                Rota.get("/api/financeiro", this::relatorio));
    }

    private Resposta compra(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(LancamentoFinanceiroDto.de(financeiro.registrarCompra(r.usuario(),
                longo(c, "insumoId"), longo(c, "fornecedorId"), inteiro(c, "quantidade"),
                longo(c, "valorCentavos"), dataOpcional(c, "data"), codigo(c), LocalDateTime.now())));
    }

    private Resposta despesa(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(LancamentoFinanceiroDto.de(financeiro.registrarDespesa(r.usuario(),
                enumeracao(CategoriaFinanceira.class, c, "categoria"), texto(c, "descricao"),
                longo(c, "valorCentavos"), dataOpcional(c, "data"), loteOpcional(c), codigo(c),
                LocalDateTime.now())));
    }

    private Resposta receita(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.criado(LancamentoFinanceiroDto.de(financeiro.registrarReceita(r.usuario(),
                enumeracao(CategoriaFinanceira.class, c, "categoria"), texto(c, "descricao"),
                longo(c, "valorCentavos"), dataOpcional(c, "data"), loteOpcional(c), codigo(c),
                LocalDateTime.now())));
    }

    /** ?de=&ate=&lote= . Sem datas, vale do primeiro dia do mes ate hoje. */
    private Resposta relatorio(Requisicao r) {
        LocalDate hoje = LocalDate.now();
        LocalDate de = data(r, "de", hoje.withDayOfMonth(1));
        LocalDate ate = data(r, "ate", hoje);
        String lote = r.consulta().get("lote");
        Long loteId = null;
        if (lote != null && !lote.isBlank()) {
            try {
                loteId = Long.parseLong(lote.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Parâmetro lote deve ser o número do lote.");
            }
        }
        return Resposta.ok(RelatorioFinanceiroDto.de(financeiro.relatorio(r.usuario(), de, ate, loteId)));
    }

    private static LocalDate data(Requisicao r, String nome, LocalDate padrao) {
        String valor = r.consulta().get(nome);
        if (valor == null || valor.isBlank()) {
            return padrao;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Parâmetro " + nome + " deve ser uma data no formato AAAA-MM-DD.");
        }
    }

    private static Long loteOpcional(JsonNode c) {
        return c.hasNonNull("loteId") ? longo(c, "loteId") : null;
    }
}
