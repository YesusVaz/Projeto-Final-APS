package br.ufpb.aps.granja;

import br.ufpb.aps.granja.api.ApiGranja;
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
        UsuariosDeDemonstracao.semear(app);

        int porta = Integer.parseInt(System.getenv().getOrDefault("PORTA", "8080"));
        new ApiGranja(app.autenticacao(), app.lotes(), app.estoque(), app.alimentacao(), app.configuracao())
                .iniciar(porta);

        System.out.println(args.length > 0
                ? "Banco em arquivo: " + args[0]
                : "Banco em memória: os dados somem ao encerrar.");
        System.out.println("API em http://127.0.0.1:" + porta + "/api");
    }
}
