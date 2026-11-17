package br.ufpb.aps.granja.evento;

import java.time.LocalDateTime;

/** Todo evento que vai pro log de auditoria (RN15). O AuditoriaListener so conhece essa interface. */
public interface EventoAuditavel {

    String categoria();

    String detalhe();

    LocalDateTime dataHora();
}
