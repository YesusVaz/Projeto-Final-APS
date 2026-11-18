package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Higienizacao;
import br.ufpb.aps.granja.model.ProgramaIluminacao;
import br.ufpb.aps.granja.model.TipoHigienizacao;
import br.ufpb.aps.granja.repository.ManejoRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class SqliteManejoRepository implements ManejoRepository {

    private final Jdbc jdbc;

    public SqliteManejoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public Higienizacao inserirHigienizacao(Higienizacao h) {
        long id = jdbc.inserir("gravar a higienização",
                "INSERT INTO higienizacao (lote_id, tipo, data, observacao, codigo) VALUES (?, ?, ?, ?, ?)",
                h.loteId(), h.tipo().name(), h.data().toString(), h.observacao(), h.codigo().valor());
        return new Higienizacao(id, h.loteId(), h.tipo(), h.data(), h.observacao(), h.codigo());
    }

    @Override
    public List<Higienizacao> higienizacoesDoLote(long loteId) {
        return jdbc.listar("listar as higienizações do lote",
                "SELECT id, lote_id, tipo, data, observacao, codigo FROM higienizacao WHERE lote_id = ? ORDER BY data, id",
                rs -> new Higienizacao(rs.getLong("id"), rs.getLong("lote_id"), TipoHigienizacao.valueOf(rs.getString("tipo")),
                        LocalDate.parse(rs.getString("data")), rs.getString("observacao"),
                        new CodigoLancamento(rs.getString("codigo"))),
                loteId);
    }

    @Override
    public ProgramaIluminacao inserirPrograma(ProgramaIluminacao p) {
        long id = jdbc.inserir("gravar o programa de iluminação",
                "INSERT INTO programa_iluminacao (lote_id, horas_de_luz, intensidade_lux, data_inicio, observacao, codigo) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                p.loteId(), p.horasDeLuz(), p.intensidadeLux(), p.dataInicio().toString(), p.observacao(),
                p.codigo().valor());
        return new ProgramaIluminacao(id, p.loteId(), p.horasDeLuz(), p.intensidadeLux(), p.dataInicio(),
                p.observacao(), p.codigo());
    }

    @Override
    public List<ProgramaIluminacao> programasDoLote(long loteId) {
        return jdbc.listar("listar os programas de iluminação do lote",
                "SELECT id, lote_id, horas_de_luz, intensidade_lux, data_inicio, observacao, codigo "
                        + "FROM programa_iluminacao WHERE lote_id = ? ORDER BY data_inicio, id",
                SqliteManejoRepository::mapearPrograma, loteId);
    }

    private static ProgramaIluminacao mapearPrograma(ResultSet rs) throws SQLException {
        int lux = rs.getInt("intensidade_lux");
        Integer luxOuNulo = rs.wasNull() ? null : lux;
        return new ProgramaIluminacao(rs.getLong("id"), rs.getLong("lote_id"), rs.getInt("horas_de_luz"), luxOuNulo,
                LocalDate.parse(rs.getString("data_inicio")), rs.getString("observacao"),
                new CodigoLancamento(rs.getString("codigo")));
    }
}
