package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.policy.ConfiguracaoAlerta;
import br.ufpb.aps.granja.policy.CriterioAlerta;
import br.ufpb.aps.granja.repository.ConfiguracaoAlertaRepository;

<<<<<<< HEAD
=======
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
import java.util.Optional;

public final class SqliteConfiguracaoAlertaRepository implements ConfiguracaoAlertaRepository {

<<<<<<< HEAD
    private final Jdbc jdbc;

    public SqliteConfiguracaoAlertaRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
=======
    private final Banco banco;

    public SqliteConfiguracaoAlertaRepository(Banco banco) {
        this.banco = banco;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public Optional<ConfiguracaoAlerta> buscar() {
<<<<<<< HEAD
        return jdbc.buscar("ler a configuração de alerta",
                "SELECT criterio, atencao, critico FROM configuracao_alerta WHERE id = 1",
                rs -> new ConfiguracaoAlerta(CriterioAlerta.valueOf(rs.getString("criterio")),
                        rs.getDouble("atencao"), rs.getDouble("critico")));
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "SELECT criterio, atencao, critico FROM configuracao_alerta WHERE id = 1");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(new ConfiguracaoAlerta(CriterioAlerta.valueOf(rs.getString("criterio")),
                    rs.getDouble("atencao"), rs.getDouble("critico")));
        } catch (SQLException e) {
            throw new ErroDePersistencia("ler a configuração de alerta", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public void salvar(ConfiguracaoAlerta configuracao) {
<<<<<<< HEAD
        jdbc.executar("gravar a configuração de alerta",
                "INSERT OR REPLACE INTO configuracao_alerta (id, criterio, atencao, critico) VALUES (1, ?, ?, ?)",
                configuracao.criterio().name(), configuracao.atencao(), configuracao.critico());
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "INSERT OR REPLACE INTO configuracao_alerta (id, criterio, atencao, critico) VALUES (1, ?, ?, ?)")) {
            ps.setString(1, configuracao.criterio().name());
            ps.setDouble(2, configuracao.atencao());
            ps.setDouble(3, configuracao.critico());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar a configuração de alerta", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }
}
