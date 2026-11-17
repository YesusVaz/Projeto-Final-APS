package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia dos usuarios. A senha entra e sai so como hash. */
public interface UsuarioRepository {

    /** Grava o usuario ja com o status de ativo que foi escolhido. */
    Usuario inserir(String login, String email, String nome, String senhaHash, Perfil perfil, boolean ativo);

    Optional<Usuario> buscarPorLogin(String login);

    Optional<Usuario> buscarPorId(long id);

    List<Usuario> listar();

    /** E-mail tambem e unico, entao o cadastro confere antes de gravar. */
    Optional<Usuario> buscarPorEmail(String email);

    /** Grava se o usuario ta ativo ou nao. RN02. */
    void atualizar(Usuario usuario);

    long contar();
}
