package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.policy.ConfiguracaoAlerta;
import br.ufpb.aps.granja.policy.CriterioAlerta;
import br.ufpb.aps.granja.repository.ConfiguracaoAlertaRepository;

import java.util.Optional;

public final class SqliteConfiguracaoAlertaRepository implements ConfiguracaoAlertaRepository {

    private final Jdbc jdbc;

    public SqliteConfiguracaoAlertaRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public Optional<ConfiguracaoAlerta> buscar() {
        return jdbc.buscar("ler a configuração de alerta",
                "SELECT criterio, atencao, critico FROM configuracao_alerta WHERE id = 1",
                rs -> new ConfiguracaoAlerta(CriterioAlerta.valueOf(rs.getString("criterio")),
                        rs.getDouble("atencao"), rs.getDouble("critico")));
    }

    @Override
    public void salvar(ConfiguracaoAlerta configuracao) {
        jdbc.executar("gravar a configuração de alerta",
                "INSERT OR REPLACE INTO configuracao_alerta (id, criterio, atencao, critico) VALUES (1, ?, ?, ?)",
                configuracao.criterio().name(), configuracao.atencao(), configuracao.critico());
    }
}
