package br.ufpb.aps.granja.evento;

/** Observer (GoF). Quem quer reagir a um evento implementa essa interface. */
@FunctionalInterface
public interface OuvinteEvento<E> {
    void ouvir(E evento);
}
