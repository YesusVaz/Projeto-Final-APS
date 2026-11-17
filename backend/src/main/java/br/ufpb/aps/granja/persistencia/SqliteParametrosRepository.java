package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.ParametrosSistema;
import br.ufpb.aps.granja.repository.ParametrosRepository;

import java.util.Optional;

public final class SqliteParametrosRepository implements ParametrosRepository {

    private final Jdbc jdbc;

    public SqliteParametrosRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public Optional<ParametrosSistema> buscar() {
        return jdbc.buscar("ler os parâmetros",
                "SELECT dias_aviso_vacina, dias_ocorrencia_aberta FROM parametros_sistema WHERE id = 1",
                rs -> new ParametrosSistema(rs.getInt("dias_aviso_vacina"), rs.getInt("dias_ocorrencia_aberta")));
    }

    @Override
    public void salvar(ParametrosSistema parametros) {
        jdbc.executar("gravar os parâmetros",
                "INSERT OR REPLACE INTO parametros_sistema (id, dias_aviso_vacina, dias_ocorrencia_aberta) VALUES (1, ?, ?)",
                parametros.diasAvisoVacina(), parametros.diasOcorrenciaAberta());
    }
}
