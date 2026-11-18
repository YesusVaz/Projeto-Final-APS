package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.repository.CopiadorDoBanco;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

/**
 * Conexao SQLite (ADR-001). {@link #memoria()} deixa o banco so na memoria, sem arquivo.
 * So tem uma conexao. A transacao de {@link #executar} vale pra todos os repositorios.
 */
public final class Banco implements UnidadeDeTrabalho, CopiadorDoBanco, AutoCloseable {

    private static final String[] ESQUEMA = {
            "CREATE TABLE IF NOT EXISTS lancamento (codigo TEXT PRIMARY KEY, data_hora TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS lote (id INTEGER PRIMARY KEY AUTOINCREMENT, codigo TEXT NOT NULL UNIQUE, "
                    + "especie TEXT NOT NULL, quantidade_inicial INTEGER NOT NULL CHECK (quantidade_inicial > 0), "
                    + "data_alojamento TEXT NOT NULL, idade_inicial_dias INTEGER NOT NULL CHECK (idade_inicial_dias >= 0), "
                    + "saldo INTEGER NOT NULL CHECK (saldo >= 0), status TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS movimento_lote (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "lote_id INTEGER NOT NULL REFERENCES lote(id), tipo TEXT NOT NULL, "
                    + "quantidade INTEGER NOT NULL CHECK (quantidade > 0), "
                    + "codigo TEXT NOT NULL REFERENCES lancamento(codigo), data_hora TEXT NOT NULL, observacao TEXT)",
            "CREATE TABLE IF NOT EXISTS insumo (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL, "
                    + "categoria TEXT NOT NULL, unidade TEXT NOT NULL, nivel_minimo INTEGER NOT NULL, "
                    + "saldo INTEGER NOT NULL CHECK (saldo >= 0), ativo INTEGER NOT NULL)",
            "CREATE TABLE IF NOT EXISTS movimento_estoque (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "insumo_id INTEGER NOT NULL REFERENCES insumo(id), tipo TEXT NOT NULL, "
                    + "quantidade INTEGER NOT NULL CHECK (quantidade > 0), "
                    + "codigo TEXT NOT NULL REFERENCES lancamento(codigo), origem TEXT NOT NULL, "
                    + "lote_id INTEGER REFERENCES lote(id), data_hora TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS registro_alimentacao (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "lote_id INTEGER NOT NULL REFERENCES lote(id), insumo_id INTEGER NOT NULL REFERENCES insumo(id), "
                    + "quantidade_kg INTEGER NOT NULL CHECK (quantidade_kg > 0), data TEXT NOT NULL, "
                    + "codigo TEXT NOT NULL REFERENCES lancamento(codigo))",
            "CREATE TABLE IF NOT EXISTS usuario (id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT NOT NULL UNIQUE, "
                    + "nome TEXT NOT NULL, email TEXT NOT NULL UNIQUE, senha_hash TEXT NOT NULL, perfil TEXT NOT NULL, ativo INTEGER NOT NULL)",
            "CREATE TABLE IF NOT EXISTS auditoria (id INTEGER PRIMARY KEY AUTOINCREMENT, categoria TEXT NOT NULL, "
                    + "detalhe TEXT NOT NULL, data_hora TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS alerta_mortalidade (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "lote_id INTEGER NOT NULL REFERENCES lote(id), codigo_lote TEXT NOT NULL, "
                    + "nivel TEXT NOT NULL, data_hora TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS configuracao_alerta (id INTEGER PRIMARY KEY CHECK (id = 1), "
                    + "criterio TEXT NOT NULL, atencao REAL NOT NULL, critico REAL NOT NULL)",
            // RN15: o log de auditoria so aceita INSERT. Quem barra e o proprio banco
            "CREATE TRIGGER IF NOT EXISTS auditoria_sem_alteracao BEFORE UPDATE ON auditoria "
                    + "BEGIN SELECT RAISE(ABORT, 'O log de auditoria é imutável (RN15).'); END",
            "CREATE TRIGGER IF NOT EXISTS auditoria_sem_exclusao BEFORE DELETE ON auditoria "
                    + "BEGIN SELECT RAISE(ABORT, 'O log de auditoria é imutável (RN15).'); END",
            "CREATE TABLE IF NOT EXISTS fornecedor (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL, "
                    + "documento TEXT NOT NULL UNIQUE, telefone TEXT, email TEXT)",
            "CREATE TABLE IF NOT EXISTS vacinacao (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "lote_id INTEGER NOT NULL REFERENCES lote(id), insumo_id INTEGER NOT NULL REFERENCES insumo(id), "
                    + "doses INTEGER NOT NULL CHECK (doses > 0), data TEXT NOT NULL, proxima_dose TEXT, "
                    + "codigo TEXT NOT NULL REFERENCES lancamento(codigo), observacao TEXT)",
            "CREATE TABLE IF NOT EXISTS ocorrencia_sanitaria (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "lote_id INTEGER NOT NULL REFERENCES lote(id), descricao TEXT NOT NULL, data_abertura TEXT NOT NULL, "
                    + "status TEXT NOT NULL, data_encerramento TEXT)",
            "CREATE TABLE IF NOT EXISTS tratamento (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "ocorrencia_id INTEGER NOT NULL REFERENCES ocorrencia_sanitaria(id), "
                    + "insumo_id INTEGER NOT NULL REFERENCES insumo(id), quantidade INTEGER NOT NULL CHECK (quantidade > 0), "
                    + "data TEXT NOT NULL, codigo TEXT NOT NULL REFERENCES lancamento(codigo), observacao TEXT)",
            "CREATE TABLE IF NOT EXISTS producao (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "lote_id INTEGER NOT NULL REFERENCES lote(id), tipo TEXT NOT NULL, data TEXT NOT NULL, "
                    + "valor INTEGER NOT NULL CHECK (valor >= 0), vivos INTEGER, indicador REAL, "
                    + "codigo TEXT NOT NULL REFERENCES lancamento(codigo), UNIQUE (lote_id, tipo, data))",
            "CREATE TABLE IF NOT EXISTS lancamento_financeiro (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "tipo TEXT NOT NULL, categoria TEXT NOT NULL, descricao TEXT NOT NULL, "
                    + "valor_centavos INTEGER NOT NULL CHECK (valor_centavos > 0), data TEXT NOT NULL, "
                    + "lote_id INTEGER REFERENCES lote(id), fornecedor_id INTEGER REFERENCES fornecedor(id), "
                    + "insumo_id INTEGER REFERENCES insumo(id), quantidade INTEGER, "
                    + "codigo TEXT NOT NULL REFERENCES lancamento(codigo))",
            "CREATE TABLE IF NOT EXISTS higienizacao (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "lote_id INTEGER NOT NULL REFERENCES lote(id), tipo TEXT NOT NULL, data TEXT NOT NULL, "
                    + "observacao TEXT, codigo TEXT NOT NULL REFERENCES lancamento(codigo))",
            "CREATE TABLE IF NOT EXISTS programa_iluminacao (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "lote_id INTEGER NOT NULL REFERENCES lote(id), "
                    + "horas_de_luz INTEGER NOT NULL CHECK (horas_de_luz BETWEEN 1 AND 24), "
                    + "intensidade_lux INTEGER CHECK (intensidade_lux > 0), data_inicio TEXT NOT NULL, "
                    + "observacao TEXT, codigo TEXT NOT NULL REFERENCES lancamento(codigo))",
            "CREATE TABLE IF NOT EXISTS chamado (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "solicitante_id INTEGER NOT NULL REFERENCES usuario(id), categoria TEXT NOT NULL, "
                    + "assunto TEXT NOT NULL, descricao TEXT NOT NULL, status TEXT NOT NULL, aberto_em TEXT NOT NULL, "
                    + "resposta TEXT, atualizado_em TEXT NOT NULL, codigo TEXT NOT NULL REFERENCES lancamento(codigo))",
            "CREATE TABLE IF NOT EXISTS parametros_sistema (id INTEGER PRIMARY KEY CHECK (id = 1), "
                    + "dias_aviso_vacina INTEGER NOT NULL, dias_ocorrencia_aberta INTEGER NOT NULL)"
    };

    private final Connection conexao;

    private Banco(Connection conexao) {
        this.conexao = conexao;
    }

    public static Banco memoria() {
        return abrir("jdbc:sqlite::memory:");
    }

    public static Banco arquivo(String caminho) {
        return abrir("jdbc:sqlite:" + caminho);
    }

    private static Banco abrir(String url) {
        try {
            Connection conexao = DriverManager.getConnection(url);
            try (Statement st = conexao.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            Banco banco = new Banco(conexao);
            banco.criarEsquema();
            return banco;
        } catch (SQLException e) {
            throw new ErroDePersistencia("abrir o banco", e);
        }
    }

    Connection conexao() {
        return conexao;
    }

    @Override
    public <T> T executar(Supplier<T> trabalho) {
        try {
            conexao.setAutoCommit(false);
            try {
                T resultado = trabalho.get();
                conexao.commit();
                return resultado;
            } catch (RuntimeException | Error e) {
                conexao.rollback();
                throw e;
            } finally {
                conexao.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("concluir a transação", e);
        }
    }

    /** UC28: copia o banco inteiro, de forma consistente, pra um arquivo novo. Funciona com banco em memoria tambem. */
    @Override
    public void copiarPara(Path destino) {
        String caminho = destino.toAbsolutePath().toString().replace("'", "''");
        try (Statement st = conexao.createStatement()) {
            st.execute("VACUUM INTO '" + caminho + "'");
        } catch (SQLException e) {
            throw new ErroDePersistencia("copiar o banco", e);
        }
    }

    private void criarEsquema() throws SQLException {
        try (Statement st = conexao.createStatement()) {
            for (String sql : ESQUEMA) {
                st.execute(sql);
            }
        }
    }

    @Override
    public void close() {
        try {
            conexao.close();
        } catch (SQLException e) {
            throw new ErroDePersistencia("fechar o banco", e);
        }
    }
}
