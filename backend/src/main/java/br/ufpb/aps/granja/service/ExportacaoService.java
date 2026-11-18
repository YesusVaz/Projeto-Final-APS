package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.ArquivoExportado;
import br.ufpb.aps.granja.model.ConjuntoExportavel;
import br.ufpb.aps.granja.model.Csv;
import br.ufpb.aps.granja.model.TabelaExportada;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.ExportacaoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** UC29: o Gestor leva os dados da granja em CSV, pra abrir no Excel. So leitura; cada exportacao fica na auditoria. */
public final class ExportacaoService {

    private final ExportacaoRepository exportacoes;
    private final BarramentoEventos eventos;

    public ExportacaoService(ExportacaoRepository exportacoes, BarramentoEventos eventos) {
        this.exportacoes = exportacoes;
        this.eventos = eventos;
    }

    public ArquivoExportado exportar(Usuario usuario, ConjuntoExportavel conjunto, LocalDate de, LocalDate ate,
                                     LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.EXPORTAR_DADOS);
        if (conjunto.usaPeriodo() && de.isAfter(ate)) {
            throw new IllegalArgumentException("A data inicial não pode ser depois da final.");
        }
        TabelaExportada tabela = exportacoes.consultar(conjunto, de, ate);
        String nome = "granja-" + conjunto.chave() + (conjunto.usaPeriodo() ? "-" + de + "-a-" + ate : "-" + agora.toLocalDate())
                + ".csv";
        eventos.publicar(new OperacaoRealizada("EXPORTACAO",
                "Exportou " + conjunto.chave() + " (" + tabela.linhas().size() + " linhas) por " + usuario.login(), agora));
        return new ArquivoExportado(nome, Csv.montar(tabela), tabela.linhas().size());
    }
}
