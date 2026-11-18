package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.Documento;
import br.ufpb.aps.granja.model.Fornecedor;
import br.ufpb.aps.granja.repository.FornecedorRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class SqliteFornecedorRepository implements FornecedorRepository {

    private static final String COLUNAS = "id, nome, documento, telefone, email";

    private final Jdbc jdbc;

    public SqliteFornecedorRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public Fornecedor inserir(Fornecedor fornecedor) {
        long id = jdbc.inserir("gravar o fornecedor",
                "INSERT INTO fornecedor (nome, documento, telefone, email) VALUES (?, ?, ?, ?)",
                fornecedor.nome(), fornecedor.documento().numero(), fornecedor.telefone(), fornecedor.email());
        return new Fornecedor(id, fornecedor.nome(), fornecedor.documento(), fornecedor.telefone(), fornecedor.email());
    }

    @Override
    public Optional<Fornecedor> buscarPorId(long id) {
        return jdbc.buscar("ler o fornecedor", "SELECT " + COLUNAS + " FROM fornecedor WHERE id = ?",
                SqliteFornecedorRepository::mapear, id);
    }

    @Override
    public Optional<Fornecedor> buscarPorDocumento(Documento documento) {
        return jdbc.buscar("ler o fornecedor", "SELECT " + COLUNAS + " FROM fornecedor WHERE documento = ?",
                SqliteFornecedorRepository::mapear, documento.numero());
    }

    @Override
    public List<Fornecedor> listar() {
        return jdbc.listar("listar os fornecedores", "SELECT " + COLUNAS + " FROM fornecedor ORDER BY nome, id",
                SqliteFornecedorRepository::mapear);
    }

    private static Fornecedor mapear(ResultSet rs) throws SQLException {
        return new Fornecedor(rs.getLong("id"), rs.getString("nome"), new Documento(rs.getString("documento")),
                rs.getString("telefone"), rs.getString("email"));
    }
}
