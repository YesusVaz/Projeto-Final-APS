package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.policy.AlertaMortalidade;
import br.ufpb.aps.granja.policy.NivelAlerta;
import br.ufpb.aps.granja.repository.AlertaRepository;

import java.time.LocalDateTime;
import java.util.List;

public final class SqliteAlertaRepository implements AlertaRepository {

    private final Jdbc jdbc;

    public SqliteAlertaRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public void registrar(AlertaMortalidade alerta) {
        jdbc.executar("gravar o alerta",
                "INSERT INTO alerta_mortalidade (lote_id, codigo_lote, nivel, data_hora) VALUES (?, ?, ?, ?)",
                alerta.loteId(), alerta.codigoLote(), alerta.nivel().name(), alerta.dataHora().toString());
    }

    @Override
    public List<AlertaMortalidade> listar() {
        return jdbc.listar("listar os alertas",
                "SELECT lote_id, codigo_lote, nivel, data_hora FROM alerta_mortalidade ORDER BY id",
                rs -> new AlertaMortalidade(rs.getLong("lote_id"), rs.getString("codigo_lote"),
                        NivelAlerta.valueOf(rs.getString("nivel")),
                        LocalDateTime.parse(rs.getString("data_hora"))));
    }
}
