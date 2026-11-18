package br.ufpb.aps.granja.model;

import java.time.LocalDateTime;

/** Um backup do banco que esta guardado em disco. */
public record CopiaDeSeguranca(String nome, long tamanhoBytes, LocalDateTime criadaEm) {
}
