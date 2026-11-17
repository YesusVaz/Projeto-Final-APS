package br.ufpb.aps.granja.evento;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer (GoF). O barramento guarda as inscricoes e entrega cada evento pra quem assinou aquele tipo.
 * Os servicos publicam depois do commit. Assim um ouvinte nunca ve operacao desfeita.
 */
public final class BarramentoEventos {

    private final List<Assinatura<?>> assinaturas = new CopyOnWriteArrayList<>();

    public <E> void inscrever(Class<E> tipo, OuvinteEvento<? super E> ouvinte) {
        assinaturas.add(new Assinatura<>(tipo, ouvinte));
    }

    public void publicar(Object evento) {
        for (Assinatura<?> assinatura : assinaturas) {
            assinatura.entregar(evento);
        }
    }

    private record Assinatura<E>(Class<E> tipo, OuvinteEvento<? super E> ouvinte) {
        void entregar(Object evento) {
            if (tipo.isInstance(evento)) {
                ouvinte.ouvir(tipo.cast(evento));
            }
        }
    }
}
