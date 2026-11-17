package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.policy.AlertaMortalidade;
import br.ufpb.aps.granja.policy.NivelAlerta;
import br.ufpb.aps.granja.repository.AlertaRepository;

<<<<<<< HEAD
import java.time.LocalDateTime;
=======
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
import java.util.List;

public final class SqliteAlertaRepository implements AlertaRepository {

<<<<<<< HEAD
    private final Jdbc jdbc;

    public SqliteAlertaRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
=======
    private final Banco banco;

    public SqliteAlertaRepository(Banco banco) {
        this.banco = banco;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public void registrar(AlertaMortalidade alerta) {
<<<<<<< HEAD
        jdbc.executar("gravar o alerta",
                "INSERT INTO alerta_mortalidade (lote_id, codigo_lote, nivel, data_hora) VALUES (?, ?, ?, ?)",
                alerta.loteId(), alerta.codigoLote(), alerta.nivel().name(), alerta.dataHora().toString());
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "INSERT INTO alerta_mortalidade (lote_id, codigo_lote, nivel, data_hora) VALUES (?, ?, ?, ?)")) {
            ps.setLong(1, alerta.loteId());
            ps.setString(2, alerta.codigoLote());
            ps.setString(3, alerta.nivel().name());
            ps.setString(4, alerta.dataHora().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar o alerta", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public List<AlertaMortalidade> listar() {
<<<<<<< HEAD
        return jdbc.listar("listar os alertas",
                "SELECT lote_id, codigo_lote, nivel, data_hora FROM alerta_mortalidade ORDER BY id",
                rs -> new AlertaMortalidade(rs.getLong("lote_id"), rs.getString("codigo_lote"),
                        NivelAlerta.valueOf(rs.getString("nivel")),
                        LocalDateTime.parse(rs.getString("data_hora"))));
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "SELECT lote_id, codigo_lote, nivel, data_hora FROM alerta_mortalidade ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<AlertaMortalidade> alertas = new ArrayList<>();
            while (rs.next()) {
                alertas.add(new AlertaMortalidade(rs.getLong("lote_id"), rs.getString("codigo_lote"),
                        NivelAlerta.valueOf(rs.getString("nivel")),
                        LocalDateTime.parse(rs.getString("data_hora"))));
            }
            return alertas;
        } catch (SQLException e) {
            throw new ErroDePersistencia("listar os alertas", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }
}
