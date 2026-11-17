package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.service.AutenticacaoService;

public record SessaoDto(String token, String login, String nome, String perfil) {

    public static SessaoDto de(AutenticacaoService.SessaoAberta sessao) {
        return new SessaoDto(sessao.token(), sessao.usuario().login(), sessao.usuario().nome(),
                sessao.usuario().perfil().name());
    }
}
