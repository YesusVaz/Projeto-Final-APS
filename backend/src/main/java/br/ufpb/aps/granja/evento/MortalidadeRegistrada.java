package br.ufpb.aps.granja.evento;

import br.ufpb.aps.granja.policy.NivelAlerta;

import java.time.LocalDateTime;

/** Evento publicado depois de gravar a mortalidade. Ja leva o nivel avaliado pela politica. */
public record MortalidadeRegistrada(Long loteId, String codigoLote, int quantidade, NivelAlerta nivel,
                                    LocalDateTime dataHora) {
}
