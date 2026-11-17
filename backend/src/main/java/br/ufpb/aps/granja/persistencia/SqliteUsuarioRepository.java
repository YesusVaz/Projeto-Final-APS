package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.UsuarioRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public final class SqliteUsuarioRepository implements UsuarioRepository {

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
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
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
    }

    @Override
    public void atualizar(Usuario usuario) {
        try (PreparedStatement ps = banco.conexao().prepareStatement("UPDATE usuario SET ativo = ? WHERE id = ?")) {
            ps.setInt(1, usuario.ativo() ? 1 : 0);
            ps.setLong(2, usuario.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("atualizar o usuário", e);
        }
    }

    @Override
    public long contar() {
        try (PreparedStatement ps = banco.conexao().prepareStatement("SELECT COUNT(*) FROM usuario");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new ErroDePersistencia("contar os usuários", e);
        }
    }
}
