package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;

import java.util.Optional;

/** Contrato de persistencia dos usuarios. A senha entra e sai so como hash. */
public interface UsuarioRepository {

<<<<<<< HEAD
    /** Grava o usuario ja com o status de ativo que foi escolhido. */
    Usuario inserir(String login, String email, String nome, String senhaHash, Perfil perfil, boolean ativo);

    Optional<Usuario> buscarPorLogin(String login);

    /** E-mail tambem e unico, entao o cadastro confere antes de gravar. */
    Optional<Usuario> buscarPorEmail(String email);

=======
    Usuario inserir(String login, String senhaHash, Perfil perfil);

    Optional<Usuario> buscarPorLogin(String login);

>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    /** Grava se o usuario ta ativo ou nao. RN02. */
    void atualizar(Usuario usuario);

    long contar();
}
