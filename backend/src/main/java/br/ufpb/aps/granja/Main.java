package br.ufpb.aps.granja;

import br.ufpb.aps.granja.api.ApiGranja;
import br.ufpb.aps.granja.persistencia.Banco;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Ponto de entrada. Sem argumento, o banco fica so na memoria e some ao fechar.
 * Com um caminho de arquivo (argumento ou variavel GRANJA_DB), o banco grava nesse .db.
 * Com --demo (ou GRANJA_DEMO=1), cria dados de exemplo se o banco ainda nao tiver lotes.
 * A porta vem da variavel PORTA (padrao 8080) e a pasta dos backups da variavel BACKUPS (padrao ./backups).
 * O endereco de escuta vem de HOST (padrao 127.0.0.1; no Docker usa 0.0.0.0).
 */
public final class Main {

    public static void main(String[] args) throws IOException {
        // --demo (ou GRANJA_DEMO=1) enche o sistema de dados de exemplo; qualquer outro argumento e o caminho do .db
        boolean demo = "1".equals(System.getenv("GRANJA_DEMO"));
        String arquivo = System.getenv("GRANJA_DB");
        if (arquivo != null && arquivo.isBlank()) {
            arquivo = null;
        }
        for (String arg : args) {
            if ("--demo".equals(arg)) {
                demo = true;
            } else {
                arquivo = arg;
            }
        }
        Banco banco = arquivo != null ? Banco.arquivo(arquivo) : Banco.memoria();
        // os backups vao pra pasta da variavel BACKUPS (padrao: ./backups)
        Composicao.Aplicacao app = Composicao.criar(banco, Path.of(System.getenv().getOrDefault("BACKUPS", "backups")));
        UsuariosDeDemonstracao.semear(app);
        if (demo) {
            try {
                System.out.println(DadosDeDemonstracao.semear(app)
                        ? "Dados de exemplo criados (lotes, estoque, financeiro, chamados e usuários extras)."
                        : "Dados de exemplo não criados: o banco já tem lotes.");
            } catch (RuntimeException e) {
                // por exemplo, um banco em arquivo que ja tem os usuarios de exemplo mas nenhum lote
                System.out.println("Dados de exemplo não foram criados: " + e.getMessage());
            }
        }

        int porta = Integer.parseInt(System.getenv().getOrDefault("PORTA", "8080"));
        String host = System.getenv().getOrDefault("HOST", "127.0.0.1");
        new ApiGranja(app)
                .iniciar(host, porta);

        System.out.println(arquivo != null
                ? "Banco em arquivo: " + arquivo
                : "Banco em memória: os dados somem ao encerrar.");
        System.out.println("API em http://" + host + ":" + porta + "/api");
    }
}
