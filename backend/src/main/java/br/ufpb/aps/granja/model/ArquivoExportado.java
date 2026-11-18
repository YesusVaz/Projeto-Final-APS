package br.ufpb.aps.granja.model;

/** Um arquivo pronto pra baixar: nome sugerido, bytes e quantas linhas de dados tem. */
public record ArquivoExportado(String nome, byte[] conteudo, int linhas) {
}
