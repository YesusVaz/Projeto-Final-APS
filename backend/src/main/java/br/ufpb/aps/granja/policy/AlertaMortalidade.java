package br.ufpb.aps.granja.policy;

import java.time.LocalDateTime;

/** Alerta gerado quando a mortalidade do lote chega no nivel de atencao ou no critico (RN24). */
public record AlertaMortalidade(Long loteId, String codigoLote, NivelAlerta nivel, LocalDateTime dataHora) {
}
