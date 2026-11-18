package br.ufpb.aps.granja.model;

import java.time.LocalDate;

/** Um valor somado num dia, pros graficos (mortes, kg de racao, ovos). */
public record PontoDia(LocalDate data, int valor) {
}
