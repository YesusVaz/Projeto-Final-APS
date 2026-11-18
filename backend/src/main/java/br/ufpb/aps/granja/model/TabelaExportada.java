package br.ufpb.aps.granja.model;

import java.util.List;

/** Cabecalho e linhas ja em texto, prontas pra virar CSV. */
public record TabelaExportada(List<String> cabecalho, List<List<String>> linhas) {
}
