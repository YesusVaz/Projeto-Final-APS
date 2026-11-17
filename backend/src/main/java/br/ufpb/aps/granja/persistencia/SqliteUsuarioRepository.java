package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.UsuarioRepository;

<<<<<<< HEAD
import java.sql.ResultSet;
import java.sql.SQLException;
=======
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
import java.util.Optional;

public final class SqliteUsuarioRepository implements UsuarioRepository {

<<<<<<< HEAD
    private static final String COLUNAS = "id, login, email, nome, senha_hash, perfil, ativo";

    private final Jdbc jdbc;

    public SqliteUsuarioRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public Usuario inserir(String login, String email, String nome, String senhaHash, Perfil perfil, boolean ativo) {
        long id = jdbc.inserir("gravar o usuário",
                "INSERT INTO usuario (login, email, nome, senha_hash, perfil, ativo) VALUES (?, ?, ?, ?, ?, ?)",
                login, email, nome, senhaHash, perfil.name(), Jdbc.comoInteiro(ativo));
        Usuario usuario = new Usuario(id, login, email, nome, senhaHash, perfil);
        if (!ativo) {
            usuario.inativar();
        }
        return usuario;
=======
    private final Banco banco;

    public SqliteUsuarioRepository(Banco banco) {
        this.banco = banco;
    }

    @Override
    public Usuario inserir(String login, String senhaHash, Perfil perfil) {
        String sql = "INSERT INTO usuario (login, senha_hash, perfil, ativo) VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, login);
            ps.setString(2, senhaHash);
            ps.setString(3, perfil.name());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return new Usuario(chaves.getLong(1), login, senhaHash, perfil);
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar o usuário", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
<<<<<<< HEAD
        return buscarUm("login = ?", login);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return buscarUm("email = ?", email);
    }

    // a condicao vem so do codigo, nunca do usuario. o valor entra como parametro
    private Optional<Usuario> buscarUm(String condicao, String valor) {
        return jdbc.buscar("ler o usuário", "SELECT " + COLUNAS + " FROM usuario WHERE " + condicao,
                SqliteUsuarioRepository::mapear, valor);
=======
        String sql = "SELECT id, login, senha_hash, perfil, ativo FROM usuario WHERE login = ?";
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Usuario usuario = new Usuario(rs.getLong("id"), rs.getString("login"),
                        rs.getString("senha_hash"), Perfil.valueOf(rs.getString("perfil")));
                if (rs.getInt("ativo") == 0) {
                    usuario.inativar();
                }
                return Optional.of(usuario);
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("ler o usuário", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public void atualizar(Usuario usuario) {
<<<<<<< HEAD
        jdbc.executar("atualizar o usuário", "UPDATE usuario SET ativo = ? WHERE id = ?",
                Jdbc.comoInteiro(usuario.ativo()), usuario.id());
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement("UPDATE usuario SET ativo = ? WHERE id = ?")) {
            ps.setInt(1, usuario.ativo() ? 1 : 0);
            ps.setLong(2, usuario.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("atualizar o usuário", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public long contar() {
<<<<<<< HEAD
        return jdbc.contar("contar os usuários", "SELECT COUNT(*) FROM usuario");
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(rs.getLong("id"), rs.getString("login"), rs.getString("email"),
                rs.getString("nome"), rs.getString("senha_hash"), Perfil.valueOf(rs.getString("perfil")));
        if (rs.getInt("ativo") == 0) {
            usuario.inativar();
        }
        return usuario;
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement("SELECT COUNT(*) FROM usuario");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new ErroDePersistencia("contar os usuários", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }
}
