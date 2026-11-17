package br.ufpb.aps.granja;

import br.ufpb.aps.granja.api.ApiGranja;
import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.persistencia.Banco;

import java.io.IOException;

/**
 * Ponto de entrada. Sem argumento, o banco fica so na memoria e some ao fechar.
 * Com um caminho de arquivo, o banco grava nesse .db.
 * A porta vem da variavel PORTA (padrao 8080).
 */
public final class Main {

    public static void main(String[] args) throws IOException {
        Banco banco = args.length > 0 ? Banco.arquivo(args[0]) : Banco.memoria();
        Composicao.Aplicacao app = Composicao.criar(banco);
        semearUsuarios(app);

        int porta = Integer.parseInt(System.getenv().getOrDefault("PORTA", "8080"));
        new ApiGranja(app.autenticacao(), app.lotes(), app.estoque(), app.alimentacao(), app.configuracao())
                .iniciar(porta);

        System.out.println(args.length > 0
                ? "Banco em arquivo: " + args[0]
                : "Banco em memória: os dados somem ao encerrar.");
        System.out.println("API em http://127.0.0.1:" + porta + "/api");
    }

    /** Cria 3 usuarios de demo se o banco estiver vazio. Senhas vem de variaveis de ambiente. */
    private static void semearUsuarios(Composicao.Aplicacao app) {
        if (app.usuariosRepositorio().contar() > 0) {
            return;
        }
        app.autenticacao().cadastrar("gestor", senha("GRANJA_SENHA_GESTOR", "gestor123"), Perfil.GESTOR);
        app.autenticacao().cadastrar("tecnico", senha("GRANJA_SENHA_TECNICO", "tecnico123"), Perfil.TECNICO);
        app.autenticacao().cadastrar("funcionario", senha("GRANJA_SENHA_FUNCIONARIO", "funcionario123"), Perfil.FUNCIONARIO);
        System.out.println("Usuários de demonstração criados. Troque as senhas antes de qualquer uso real.");
    }

    private static String senha(String variavel, String padrao) {
        return System.getenv().getOrDefault(variavel, padrao);
    }
}
