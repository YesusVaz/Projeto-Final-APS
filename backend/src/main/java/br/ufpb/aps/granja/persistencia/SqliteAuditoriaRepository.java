package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.repository.AuditoriaRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class SqliteAuditoriaRepository implements AuditoriaRepository {

    private final Banco banco;

    public SqliteAuditoriaRepository(Banco banco) {
        this.banco = banco;
    }

    @Override
    public void registrar(String categoria, String detalhe, LocalDateTime dataHora) {
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "INSERT INTO auditoria (categoria, detalhe, data_hora) VALUES (?, ?, ?)")) {
            ps.setString(1, categoria);
            ps.setString(2, detalhe);
            ps.setString(3, dataHora.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar a auditoria", e);
        }
    }

    @Override
    public List<Entrada> listar() {
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "SELECT categoria, detalhe, data_hora FROM auditoria ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<Entrada> entradas = new ArrayList<>();
            while (rs.next()) {
                entradas.add(new Entrada(rs.getString("categoria"), rs.getString("detalhe"),
                        LocalDateTime.parse(rs.getString("data_hora"))));
            }
            return entradas;
        } catch (SQLException e) {
            throw new ErroDePersistencia("listar a auditoria", e);
        }
    }
}
