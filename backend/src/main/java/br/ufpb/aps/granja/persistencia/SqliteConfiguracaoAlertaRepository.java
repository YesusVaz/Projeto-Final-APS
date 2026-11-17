package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.policy.ConfiguracaoAlerta;
import br.ufpb.aps.granja.policy.CriterioAlerta;
import br.ufpb.aps.granja.repository.ConfiguracaoAlertaRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class SqliteConfiguracaoAlertaRepository implements ConfiguracaoAlertaRepository {

    private final Banco banco;

    public SqliteConfiguracaoAlertaRepository(Banco banco) {
        this.banco = banco;
    }

    @Override
    public Optional<ConfiguracaoAlerta> buscar() {
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
    }

    @Override
    public void salvar(ConfiguracaoAlerta configuracao) {
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "INSERT OR REPLACE INTO configuracao_alerta (id, criterio, atencao, critico) VALUES (1, ?, ?, ?)")) {
            ps.setString(1, configuracao.criterio().name());
            ps.setDouble(2, configuracao.atencao());
            ps.setDouble(3, configuracao.critico());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar a configuração de alerta", e);
        }
    }
}
