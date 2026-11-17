package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.AutenticacaoException;
import br.ufpb.aps.granja.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;

/** RN02 (inativo nao entra) e RNF09 (senha guardada como hash bcrypt). */
public final class AutenticacaoService {

    /** Custo do bcrypt. A RNF09 pede 10 ou mais. */
    private static final int CUSTO_BCRYPT = 10;

    private final UsuarioRepository usuarios;

    public AutenticacaoService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario autenticar(String login, String senha) {
        Usuario usuario = usuarios.buscarPorLogin(login).orElseThrow(AutenticacaoException::new);
        if (!usuario.ativo() || senha == null || !BCrypt.checkpw(senha, usuario.senhaHash())) {
            throw new AutenticacaoException();
        }
        return usuario;
    }

    /** Cria usuario ja com a senha em hash. Usado na subida do sistema. */
    public Usuario cadastrar(String login, String senha, Perfil perfil) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("O login é obrigatório.");
        }
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha precisa ter pelo menos 6 caracteres.");
        }
        return usuarios.inserir(login.trim(), BCrypt.hashpw(senha, BCrypt.gensalt(CUSTO_BCRYPT)), perfil);
    }
}
