package br.ufpb.aps.granja.evento;

import java.time.LocalDateTime;

/**
 * Evento generico pras operacoes que so precisam ir pro log (baixa, vacina, usuario...).
 * Quem precisa de mais dado, tipo a mortalidade com o nivel do alerta, tem evento proprio.
 */
public record OperacaoRealizada(String categoria, String detalhe, LocalDateTime dataHora) implements EventoAuditavel {
}
