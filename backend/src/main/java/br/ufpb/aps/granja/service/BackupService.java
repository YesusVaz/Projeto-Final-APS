package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.CopiaDeSeguranca;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import br.ufpb.aps.granja.repository.CopiadorDoBanco;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * UC28: o Gestor tira, lista e baixa copias do banco. Cada copia e um arquivo .db inteiro numa pasta do servidor.
 * O nome do arquivo e checado contra um padrao fixo antes de qualquer acesso a disco, entao ninguem le fora da pasta.
 */
public final class BackupService {

    private static final DateTimeFormatter CARIMBO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Pattern NOME_VALIDO = Pattern.compile("granja-\\d{8}-\\d{6}(-\\d+)?\\.db");

    private final CopiadorDoBanco copiador;
    private final Path pasta;
    private final BarramentoEventos eventos;

    public BackupService(CopiadorDoBanco copiador, Path pasta, BarramentoEventos eventos) {
        this.copiador = copiador;
        this.pasta = pasta;
        this.eventos = eventos;
    }

    public CopiaDeSeguranca criar(Usuario usuario, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.GERENCIAR_BACKUP);
        try {
            Files.createDirectories(pasta);
            String base = "granja-" + CARIMBO.format(agora);
            Path destino = pasta.resolve(base + ".db");
            // duas copias no mesmo segundo nao se sobrescrevem
            for (int n = 2; Files.exists(destino); n++) {
                destino = pasta.resolve(base + "-" + n + ".db");
            }
            copiador.copiarPara(destino);
            eventos.publicar(new OperacaoRealizada("BACKUP",
                    "Backup " + destino.getFileName() + " feito por " + usuario.login(), agora));
            return descrever(destino);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar o backup.", e);
        }
    }

    /** As copias que existem, da mais nova pra mais antiga. */
    public List<CopiaDeSeguranca> listar(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.GERENCIAR_BACKUP);
        if (!Files.isDirectory(pasta)) {
            return List.of();
        }
        try (Stream<Path> arquivos = Files.list(pasta)) {
            return arquivos.filter(p -> NOME_VALIDO.matcher(p.getFileName().toString()).matches())
                    .map(BackupService::descrever)
                    .sorted(Comparator.comparing(CopiaDeSeguranca::criadaEm).reversed()
                            .thenComparing(c -> sequencia(c.nome()), Comparator.reverseOrder()))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível listar os backups.", e);
        }
    }

    public byte[] ler(Usuario usuario, String nome, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.GERENCIAR_BACKUP);
        if (!NOME_VALIDO.matcher(nome).matches() || !Files.isRegularFile(pasta.resolve(nome))) {
            throw new NaoEncontradoException("Backup não encontrado.");
        }
        try {
            byte[] bytes = Files.readAllBytes(pasta.resolve(nome));
            eventos.publicar(new OperacaoRealizada("BACKUP", "Backup " + nome + " baixado por " + usuario.login(), agora));
            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler o backup.", e);
        }
    }

    /** Numero depois do carimbo: "granja-...-2.db" e a segunda do segundo, a sem numero e a primeira. */
    private static int sequencia(String nome) {
        String semExtensao = nome.substring(0, nome.length() - 3);
        return semExtensao.length() > 22 ? Integer.parseInt(semExtensao.substring(23)) : 1;
    }

    private static CopiaDeSeguranca descrever(Path arquivo) {
        try {
            String nome = arquivo.getFileName().toString();
            // o carimbo do nome ("granja-AAAAMMDD-HHMMSS") e a hora em que a copia foi pedida
            LocalDateTime criada = LocalDateTime.parse(nome.substring(7, 22), CARIMBO);
            return new CopiaDeSeguranca(nome, Files.size(arquivo), criada);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler os dados do backup.", e);
        }
    }
}
