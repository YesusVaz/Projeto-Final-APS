package br.ufpb.aps.granja;

import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.persistencia.Banco;

import java.time.LocalDateTime;

/** Sistema novo pros testes: banco em memoria e um usuario de cada perfil. */
public final class Cenario implements AutoCloseable {

    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 17, 9, 0);

    public final Banco banco;
    public final Composicao.Aplicacao app;
    public final Usuario gestor;
    public final Usuario tecnico;
    public final Usuario funcionario;

    public Cenario() {
        this.banco = Banco.memoria();
        this.app = Composicao.criar(banco);
        this.gestor = app.autenticacao().cadastrar("gestor", "gestor@granja.local", "Gestor de teste",
                "senha-gestor", Perfil.GESTOR);
        this.tecnico = app.autenticacao().cadastrar("tecnico", "tecnico@granja.local", "Técnico de teste",
                "senha-tecnico", Perfil.TECNICO);
        this.funcionario = app.autenticacao().cadastrar("funcionario", "funcionario@granja.local",
                "Funcionário de teste", "senha-funcionario", Perfil.FUNCIONARIO);
    }

    @Override
    public void close() {
        banco.close();
    }
}
