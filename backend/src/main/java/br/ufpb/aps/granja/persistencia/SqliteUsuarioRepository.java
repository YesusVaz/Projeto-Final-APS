package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.UsuarioRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class SqliteUsuarioRepository implements UsuarioRepository {

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
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
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
    }

    @Override
    public void atualizar(Usuario usuario) {
        jdbc.executar("atualizar o usuário", "UPDATE usuario SET ativo = ? WHERE id = ?",
                Jdbc.comoInteiro(usuario.ativo()), usuario.id());
    }

    @Override
    public long contar() {
        return jdbc.contar("contar os usuários", "SELECT COUNT(*) FROM usuario");
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(rs.getLong("id"), rs.getString("login"), rs.getString("email"),
                rs.getString("nome"), rs.getString("senha_hash"), Perfil.valueOf(rs.getString("perfil")));
        if (rs.getInt("ativo") == 0) {
            usuario.inativar();
        }
        return usuario;
    }
}
