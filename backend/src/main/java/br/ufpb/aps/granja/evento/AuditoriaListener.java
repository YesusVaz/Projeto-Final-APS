package br.ufpb.aps.granja.evento;

import br.ufpb.aps.granja.repository.AuditoriaRepository;

/** RN15: toda mortalidade registrada vai pro log de auditoria. */
public final class AuditoriaListener implements OuvinteEvento<MortalidadeRegistrada> {

    private final AuditoriaRepository auditoria;

    public AuditoriaListener(AuditoriaRepository auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public void ouvir(MortalidadeRegistrada evento) {
        auditoria.registrar("MORTALIDADE",
                "Lote " + evento.codigoLote() + ": " + evento.quantidade() + " mortes, nível " + evento.nivel(),
                evento.dataHora());
    }
}
