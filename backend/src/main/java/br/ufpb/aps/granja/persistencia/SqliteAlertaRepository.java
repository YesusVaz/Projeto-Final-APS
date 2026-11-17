package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.policy.AlertaMortalidade;
import br.ufpb.aps.granja.policy.NivelAlerta;
import br.ufpb.aps.granja.repository.AlertaRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class SqliteAlertaRepository implements AlertaRepository {

    private final Banco banco;

    public SqliteAlertaRepository(Banco banco) {
        this.banco = banco;
    }

    @Override
    public void registrar(AlertaMortalidade alerta) {
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
    }

    @Override
    public List<AlertaMortalidade> listar() {
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
    }
}
