package br.ufpb.aps.granja.evento;

import br.ufpb.aps.granja.policy.AlertaMortalidade;
import br.ufpb.aps.granja.policy.NivelAlerta;
import br.ufpb.aps.granja.repository.AlertaRepository;

/** RN24: gera alerta se a mortalidade chegar no nivel de atencao ou no critico. */
public final class AlertaListener implements OuvinteEvento<MortalidadeRegistrada> {

    private final AlertaRepository alertas;

    public AlertaListener(AlertaRepository alertas) {
        this.alertas = alertas;
    }

    @Override
    public void ouvir(MortalidadeRegistrada evento) {
        if (evento.nivel() != NivelAlerta.NORMAL) {
            alertas.registrar(new AlertaMortalidade(evento.loteId(), evento.codigoLote(), evento.nivel(), evento.dataHora()));
        }
    }
}
