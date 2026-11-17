package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;

import java.util.Optional;

/** Contrato de persistencia dos usuarios. A senha entra e sai so como hash. */
public interface UsuarioRepository {

    Usuario inserir(String login, String senhaHash, Perfil perfil);

    Optional<Usuario> buscarPorLogin(String login);

    /** Grava se o usuario ta ativo ou nao. RN02. */
    void atualizar(Usuario usuario);

    long contar();
}
