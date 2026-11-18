package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.model.ArquivoExportado;
import br.ufpb.aps.granja.model.ConjuntoExportavel;
import br.ufpb.aps.granja.service.ExportacaoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Exportacao de dados em CSV (UC29). ?de=&ate= valem so pros conjuntos que tem periodo; sem datas, vai do dia 1 ate hoje. */
final class ExportacaoController implements Controller {

    private final ExportacaoService exportacao;

    ExportacaoController(ExportacaoService exportacao) {
        this.exportacao = exportacao;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(Rota.get("/api/exportacoes/([a-z]+)", this::exportar));
    }

    private Resposta exportar(Requisicao r) {
        LocalDate hoje = LocalDate.now();
        ArquivoExportado arquivo = exportacao.exportar(r.usuario(), ConjuntoExportavel.daChave(r.parametros().get(0)),
                data(r, "de", hoje.withDayOfMonth(1)), data(r, "ate", hoje), LocalDateTime.now());
        return Resposta.arquivo(arquivo.nome(), "text/csv; charset=utf-8", arquivo.conteudo());
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
