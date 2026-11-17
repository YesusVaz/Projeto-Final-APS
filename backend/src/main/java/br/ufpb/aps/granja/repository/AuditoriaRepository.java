package br.ufpb.aps.granja.repository;

import java.time.LocalDateTime;
import java.util.List;

/** Log de auditoria (RN15). So insere e le, nao tem alterar nem apagar. */
public interface AuditoriaRepository {

    void registrar(String categoria, String detalhe, LocalDateTime dataHora);

    List<Entrada> listar();

    record Entrada(String categoria, String detalhe, LocalDateTime dataHora) {
    }
}
