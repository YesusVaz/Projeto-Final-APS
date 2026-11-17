package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.repository.AuditoriaRepository;

import java.time.LocalDateTime;
import java.util.List;

public final class SqliteAuditoriaRepository implements AuditoriaRepository {

    private final Jdbc jdbc;

    public SqliteAuditoriaRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public void registrar(String categoria, String detalhe, LocalDateTime dataHora) {
        jdbc.executar("gravar a auditoria",
                "INSERT INTO auditoria (categoria, detalhe, data_hora) VALUES (?, ?, ?)",
                categoria, detalhe, dataHora.toString());
    }

    @Override
    public List<Entrada> listar() {
        return jdbc.listar("listar a auditoria",
                "SELECT categoria, detalhe, data_hora FROM auditoria ORDER BY id",
                rs -> new Entrada(rs.getString("categoria"), rs.getString("detalhe"),
                        LocalDateTime.parse(rs.getString("data_hora"))));
    }
}
