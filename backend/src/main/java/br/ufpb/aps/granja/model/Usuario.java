package br.ufpb.aps.granja.model;

import java.util.Objects;

/** Usuario do sistema. RN02: inativo nao faz nada. A senha fica so como hash. */
public class Usuario {

    private final Long id;
    private final String login;
    private final String senhaHash;
    private final Perfil perfil;
    private boolean ativo = true;

    public Usuario(Long id, String login, String senhaHash, Perfil perfil) {
        this.id = id;
        this.login = Objects.requireNonNull(login, "login");
        this.senhaHash = Objects.requireNonNull(senhaHash, "senhaHash");
        this.perfil = Objects.requireNonNull(perfil, "perfil");
    }

    public boolean podeExecutar(Acao acao) {
        return ativo && perfil.permite(acao);
    }

    public void inativar() {
        ativo = false;
    }

    public Long id() { return id; }
    public String login() { return login; }
    public String senhaHash() { return senhaHash; }
    public Perfil perfil() { return perfil; }
    public boolean ativo() { return ativo; }
}
