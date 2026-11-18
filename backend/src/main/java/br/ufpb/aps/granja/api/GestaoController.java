package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.GestaoDto.GraficosDto;
import br.ufpb.aps.granja.api.dto.GestaoDto.PainelDto;
import br.ufpb.aps.granja.api.dto.GestaoDto.RelatorioDto;
import br.ufpb.aps.granja.service.GestaoService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Area do gestor: painel (UC23), relatorio gerencial (UC25) e graficos de desempenho (UC26). */
final class GestaoController implements Controller {

    private final GestaoService gestao;

    GestaoController(GestaoService gestao) {
        this.gestao = gestao;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.get("/api/gestao/painel", this::painel),
                Rota.get("/api/gestao/graficos", this::graficos),
                Rota.get("/api/gestao/relatorio", this::relatorio));
    }

    private Resposta painel(Requisicao r) {
        return Resposta.ok(PainelDto.de(gestao.painel(r.usuario(), LocalDate.now())));
    }

    private Resposta graficos(Requisicao r) {
        return Resposta.ok(GraficosDto.de(gestao.graficos(r.usuario(), LocalDate.now())));
    }

    /** ?de=&ate= . Sem datas, vale do primeiro dia do mes ate hoje. */
    private Resposta relatorio(Requisicao r) {
        LocalDate hoje = LocalDate.now();
        return Resposta.ok(RelatorioDto.de(gestao.relatorio(r.usuario(), data(r, "de", hoje.withDayOfMonth(1)),
                data(r, "ate", hoje))));
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
}
