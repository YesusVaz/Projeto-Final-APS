package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.StatusOcorrencia;
import br.ufpb.aps.granja.model.Tratamento;
import br.ufpb.aps.granja.model.Vacinacao;
import br.ufpb.aps.granja.repository.SanidadeRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class SqliteSanidadeRepository implements SanidadeRepository {

    private static final String COLUNAS_VACINA = "id, lote_id, insumo_id, doses, data, proxima_dose, codigo, observacao";
    private static final String COLUNAS_OCORRENCIA = "id, lote_id, descricao, data_abertura, status, data_encerramento";
    private static final String COLUNAS_TRATAMENTO = "id, ocorrencia_id, insumo_id, quantidade, data, codigo, observacao";

    private final Jdbc jdbc;

    public SqliteSanidadeRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public Vacinacao inserirVacinacao(Vacinacao v) {
        long id = jdbc.inserir("gravar a vacinação",
                "INSERT INTO vacinacao (lote_id, insumo_id, doses, data, proxima_dose, codigo, observacao) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                v.loteId(), v.insumoId(), v.doses().valor(), v.data().toString(), texto(v.proximaDose()),
                v.codigo().valor(), v.observacao());
        return new Vacinacao(id, v.loteId(), v.insumoId(), v.doses(), v.data(), v.proximaDose(), v.codigo(),
                v.observacao());
    }

    @Override
    public List<Vacinacao> vacinacoesDoLote(long loteId) {
        return jdbc.listar("listar as vacinações do lote",
                "SELECT " + COLUNAS_VACINA + " FROM vacinacao WHERE lote_id = ? ORDER BY data, id",
                SqliteSanidadeRepository::mapearVacinacao, loteId);
    }

    @Override
    public List<Vacinacao> listarVacinacoes() {
        return jdbc.listar("listar as vacinações",
                "SELECT " + COLUNAS_VACINA + " FROM vacinacao ORDER BY data, id",
                SqliteSanidadeRepository::mapearVacinacao);
    }

    @Override
    public OcorrenciaSanitaria inserirOcorrencia(OcorrenciaSanitaria o) {
        long id = jdbc.inserir("gravar a ocorrência",
                "INSERT INTO ocorrencia_sanitaria (lote_id, descricao, data_abertura, status, data_encerramento) "
                        + "VALUES (?, ?, ?, ?, ?)",
                o.loteId(), o.descricao(), o.dataAbertura().toString(), o.status().name(), texto(o.dataEncerramento()));
        return OcorrenciaSanitaria.reconstituir(id, o.loteId(), o.descricao(), o.dataAbertura(), o.status(),
                o.dataEncerramento());
    }

    @Override
    public Optional<OcorrenciaSanitaria> buscarOcorrencia(long id) {
        return jdbc.buscar("ler a ocorrência",
                "SELECT " + COLUNAS_OCORRENCIA + " FROM ocorrencia_sanitaria WHERE id = ?",
                SqliteSanidadeRepository::mapearOcorrencia, id);
    }

    @Override
    public void atualizarOcorrencia(OcorrenciaSanitaria o) {
        jdbc.executar("atualizar a ocorrência",
                "UPDATE ocorrencia_sanitaria SET status = ?, data_encerramento = ? WHERE id = ?",
                o.status().name(), texto(o.dataEncerramento()), o.id());
    }

    @Override
    public List<OcorrenciaSanitaria> ocorrenciasDoLote(long loteId) {
        return jdbc.listar("listar as ocorrências do lote",
                "SELECT " + COLUNAS_OCORRENCIA + " FROM ocorrencia_sanitaria WHERE lote_id = ? ORDER BY data_abertura, id",
                SqliteSanidadeRepository::mapearOcorrencia, loteId);
    }

    @Override
    public List<OcorrenciaSanitaria> listarOcorrencias() {
        return jdbc.listar("listar as ocorrências",
                "SELECT " + COLUNAS_OCORRENCIA + " FROM ocorrencia_sanitaria ORDER BY data_abertura, id",
                SqliteSanidadeRepository::mapearOcorrencia);
    }

    @Override
    public Tratamento inserirTratamento(Tratamento t) {
        long id = jdbc.inserir("gravar o tratamento",
                "INSERT INTO tratamento (ocorrencia_id, insumo_id, quantidade, data, codigo, observacao) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                t.ocorrenciaId(), t.insumoId(), t.quantidade().valor(), t.data().toString(), t.codigo().valor(),
                t.observacao());
        return new Tratamento(id, t.ocorrenciaId(), t.insumoId(), t.quantidade(), t.data(), t.codigo(), t.observacao());
    }

    @Override
    public List<Tratamento> tratamentosDaOcorrencia(long ocorrenciaId) {
        return jdbc.listar("listar os tratamentos",
                "SELECT " + COLUNAS_TRATAMENTO + " FROM tratamento WHERE ocorrencia_id = ? ORDER BY data, id",
                SqliteSanidadeRepository::mapearTratamento, ocorrenciaId);
    }

    private static Vacinacao mapearVacinacao(ResultSet rs) throws SQLException {
        return new Vacinacao(rs.getLong("id"), rs.getLong("lote_id"), rs.getLong("insumo_id"),
                Quantidade.de(rs.getInt("doses")), LocalDate.parse(rs.getString("data")),
                data(rs.getString("proxima_dose")), new CodigoLancamento(rs.getString("codigo")),
                rs.getString("observacao"));
    }

    private static OcorrenciaSanitaria mapearOcorrencia(ResultSet rs) throws SQLException {
        return OcorrenciaSanitaria.reconstituir(rs.getLong("id"), rs.getLong("lote_id"), rs.getString("descricao"),
                LocalDate.parse(rs.getString("data_abertura")), StatusOcorrencia.valueOf(rs.getString("status")),
                data(rs.getString("data_encerramento")));
    }

    private static Tratamento mapearTratamento(ResultSet rs) throws SQLException {
        return new Tratamento(rs.getLong("id"), rs.getLong("ocorrencia_id"), rs.getLong("insumo_id"),
                Quantidade.de(rs.getInt("quantidade")), LocalDate.parse(rs.getString("data")),
                new CodigoLancamento(rs.getString("codigo")), rs.getString("observacao"));
    }

    private static String texto(LocalDate data) {
        return data == null ? null : data.toString();
    }

    private static LocalDate data(String texto) {
        return texto == null ? null : LocalDate.parse(texto);
    }
}
