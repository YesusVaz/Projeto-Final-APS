package br.ufpb.aps.granja.evento;

import br.ufpb.aps.granja.repository.AuditoriaRepository;

/** RN15: toda operacao auditavel vai pro log de auditoria, que so aceita insercao. */
public final class AuditoriaListener implements OuvinteEvento<EventoAuditavel> {

    private final AuditoriaRepository auditoria;

    public AuditoriaListener(AuditoriaRepository auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public void ouvir(EventoAuditavel evento) {
        auditoria.registrar(evento.categoria(), evento.detalhe(), evento.dataHora());
    }
}
