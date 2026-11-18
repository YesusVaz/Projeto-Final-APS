package br.ufpb.aps.granja;

import br.ufpb.aps.granja.api.ApiGranja;
import br.ufpb.aps.granja.persistencia.Banco;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Ponto de entrada. Sem argumento, o banco fica so na memoria e some ao fechar.
 * Com um caminho de arquivo, o banco grava nesse .db.
 * A porta vem da variavel PORTA (padrao 8080) e a pasta dos backups da variavel BACKUPS (padrao ./backups).
 */
public final class Main {

    public static void main(String[] args) throws IOException {
        Banco banco = args.length > 0 ? Banco.arquivo(args[0]) : Banco.memoria();
        // os backups vao pra pasta da variavel BACKUPS (padrao: ./backups)
        Composicao.Aplicacao app = Composicao.criar(banco, Path.of(System.getenv().getOrDefault("BACKUPS", "backups")));
        UsuariosDeDemonstracao.semear(app);

        int porta = Integer.parseInt(System.getenv().getOrDefault("PORTA", "8080"));
        new ApiGranja(app)
                .iniciar(porta);

        System.out.println(args.length > 0
                ? "Banco em arquivo: " + args[0]
                : "Banco em memória: os dados somem ao encerrar.");
        System.out.println("API em http://127.0.0.1:" + porta + "/api");
    }
}
