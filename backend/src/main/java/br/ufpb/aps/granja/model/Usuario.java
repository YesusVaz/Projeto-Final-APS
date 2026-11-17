package br.ufpb.aps.granja.model;

import java.util.Objects;

/** Usuario do sistema. RN02: inativo nao faz nada. A senha fica so como hash. */
public class Usuario {

    private final Long id;
    private final String login;
<<<<<<< HEAD
    private final String email;
    private final String nome;
=======
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    private final String senhaHash;
    private final Perfil perfil;
    private boolean ativo = true;

<<<<<<< HEAD
    public Usuario(Long id, String login, String email, String nome, String senhaHash, Perfil perfil) {
        this.id = id;
        this.login = Objects.requireNonNull(login, "login");
        this.email = Objects.requireNonNull(email, "email");
        this.nome = Objects.requireNonNull(nome, "nome");
=======
    public Usuario(Long id, String login, String senhaHash, Perfil perfil) {
        this.id = id;
        this.login = Objects.requireNonNull(login, "login");
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
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
<<<<<<< HEAD
    public String email() { return email; }
    public String nome() { return nome; }
=======
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    public String senhaHash() { return senhaHash; }
    public Perfil perfil() { return perfil; }
    public boolean ativo() { return ativo; }
}
