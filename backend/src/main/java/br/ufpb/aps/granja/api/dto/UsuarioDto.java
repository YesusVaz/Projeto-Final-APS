package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.Usuario;

public record UsuarioDto(long id, String login, String email, String nome, String perfil, boolean ativo) {

    public static UsuarioDto de(Usuario usuario) {
        return new UsuarioDto(usuario.id(), usuario.login(), usuario.email(), usuario.nome(),
                usuario.perfil().name(), usuario.ativo());
    }
}
