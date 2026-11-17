package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.repository.AuditoriaRepository;

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

public final class SqliteAuditoriaRepository implements AuditoriaRepository {

<<<<<<< HEAD
    private final Jdbc jdbc;

    public SqliteAuditoriaRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
=======
    private final Banco banco;

    public SqliteAuditoriaRepository(Banco banco) {
        this.banco = banco;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public void registrar(String categoria, String detalhe, LocalDateTime dataHora) {
<<<<<<< HEAD
        jdbc.executar("gravar a auditoria",
                "INSERT INTO auditoria (categoria, detalhe, data_hora) VALUES (?, ?, ?)",
                categoria, detalhe, dataHora.toString());
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "INSERT INTO auditoria (categoria, detalhe, data_hora) VALUES (?, ?, ?)")) {
            ps.setString(1, categoria);
            ps.setString(2, detalhe);
            ps.setString(3, dataHora.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar a auditoria", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public List<Entrada> listar() {
<<<<<<< HEAD
        return jdbc.listar("listar a auditoria",
                "SELECT categoria, detalhe, data_hora FROM auditoria ORDER BY id",
                rs -> new Entrada(rs.getString("categoria"), rs.getString("detalhe"),
                        LocalDateTime.parse(rs.getString("data_hora"))));
=======
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
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }
}
