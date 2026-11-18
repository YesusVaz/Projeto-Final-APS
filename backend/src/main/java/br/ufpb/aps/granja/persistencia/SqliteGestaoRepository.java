package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.PontoDia;
import br.ufpb.aps.granja.repository.GestaoRepository;

import java.time.LocalDate;
import java.util.List;

public final class SqliteGestaoRepository implements GestaoRepository {

    private final Jdbc jdbc;

    public SqliteGestaoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public List<PontoDia> mortesPorDia(LocalDate de, LocalDate ate) {
        // data_hora e ISO (AAAA-MM-DDTHH:MM...), os 10 primeiros caracteres sao o dia
        return jdbc.listar("somar as mortes por dia",
                "SELECT substr(data_hora, 1, 10) AS dia, SUM(quantidade) AS total FROM movimento_lote "
                        + "WHERE tipo = 'MORTALIDADE' AND substr(data_hora, 1, 10) BETWEEN ? AND ? "
                        + "GROUP BY dia ORDER BY dia",
                rs -> new PontoDia(LocalDate.parse(rs.getString("dia")), rs.getInt("total")),
                de.toString(), ate.toString());
    }

    @Override
    public List<PontoDia> racaoPorDia(LocalDate de, LocalDate ate) {
        return jdbc.listar("somar a ração por dia",
                "SELECT data AS dia, SUM(quantidade_kg) AS total FROM registro_alimentacao "
                        + "WHERE data BETWEEN ? AND ? GROUP BY data ORDER BY data",
                rs -> new PontoDia(LocalDate.parse(rs.getString("dia")), rs.getInt("total")),
                de.toString(), ate.toString());
    }

    @Override
    public List<PontoDia> ovosPorDia(LocalDate de, LocalDate ate) {
        return jdbc.listar("somar os ovos por dia",
                "SELECT data AS dia, SUM(valor) AS total FROM producao "
                        + "WHERE tipo = 'POSTURA' AND data BETWEEN ? AND ? GROUP BY data ORDER BY data",
                rs -> new PontoDia(LocalDate.parse(rs.getString("dia")), rs.getInt("total")),
                de.toString(), ate.toString());
    }
}
