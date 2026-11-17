package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.RegistroAlimentacao;
import br.ufpb.aps.granja.repository.AlimentacaoRepository;

<<<<<<< HEAD
public final class SqliteAlimentacaoRepository implements AlimentacaoRepository {

    private final Jdbc jdbc;

    public SqliteAlimentacaoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
=======
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SqliteAlimentacaoRepository implements AlimentacaoRepository {

    private final Banco banco;

    public SqliteAlimentacaoRepository(Banco banco) {
        this.banco = banco;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public RegistroAlimentacao inserir(RegistroAlimentacao registro) {
<<<<<<< HEAD
        long id = jdbc.inserir("gravar a alimentação",
                "INSERT INTO registro_alimentacao (lote_id, insumo_id, quantidade_kg, data, codigo) "
                        + "VALUES (?, ?, ?, ?, ?)",
                registro.loteId(), registro.insumoId(), registro.quantidadeKg().valor(),
                registro.data().toString(), registro.codigo().valor());
        return new RegistroAlimentacao(id, registro.loteId(), registro.insumoId(),
                registro.quantidadeKg(), registro.data(), registro.codigo());
=======
        String sql = "INSERT INTO registro_alimentacao (lote_id, insumo_id, quantidade_kg, data, codigo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, registro.loteId());
            ps.setLong(2, registro.insumoId());
            ps.setInt(3, registro.quantidadeKg().valor());
            ps.setString(4, registro.data().toString());
            ps.setString(5, registro.codigo().valor());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return new RegistroAlimentacao(chaves.getLong(1), registro.loteId(), registro.insumoId(),
                        registro.quantidadeKg(), registro.data(), registro.codigo());
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar a alimentação", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }
}
