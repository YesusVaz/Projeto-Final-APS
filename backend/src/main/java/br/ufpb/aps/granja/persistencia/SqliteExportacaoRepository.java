package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.ConjuntoExportavel;
import br.ufpb.aps.granja.model.TabelaExportada;
import br.ufpb.aps.granja.repository.ExportacaoRepository;

import java.time.LocalDate;

/** Cada conjunto e uma consulta so de leitura. O apelido de cada coluna e o titulo que aparece na planilha. */
public final class SqliteExportacaoRepository implements ExportacaoRepository {

    private static final String ROTULO_CATEGORIA_FINANCEIRA = "CASE f.categoria "
            + "WHEN 'COMPRA_INSUMO' THEN 'Compra de insumos' WHEN 'MAO_DE_OBRA' THEN 'Mão de obra' "
            + "WHEN 'ENERGIA_AGUA' THEN 'Energia e água' WHEN 'MANUTENCAO' THEN 'Manutenção' "
            + "WHEN 'TRANSPORTE' THEN 'Transporte' WHEN 'OUTRA_DESPESA' THEN 'Outras despesas' "
            + "WHEN 'VENDA_OVOS' THEN 'Venda de ovos' WHEN 'VENDA_AVES' THEN 'Venda de aves' "
            + "WHEN 'OUTRA_RECEITA' THEN 'Outras receitas' ELSE f.categoria END";

    private final Jdbc jdbc;

    public SqliteExportacaoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public TabelaExportada consultar(ConjuntoExportavel conjunto, LocalDate de, LocalDate ate) {
        String inicio = de.toString();
        String fim = ate.toString();
        return switch (conjunto) {
            case LOTES -> jdbc.tabela("exportar os lotes",
                    "SELECT codigo AS \"Código\", "
                            + "CASE especie WHEN 'FRANGO_CORTE' THEN 'Frango de corte' ELSE 'Poedeira' END AS \"Espécie\", "
                            + "quantidade_inicial AS \"Quantidade inicial\", data_alojamento AS \"Alojamento\", "
                            + "idade_inicial_dias AS \"Idade inicial (dias)\", saldo AS \"Saldo\", "
                            + "CASE status WHEN 'ATIVO' THEN 'Ativo' ELSE 'Encerrado' END AS \"Situação\" "
                            + "FROM lote ORDER BY codigo");
            case ESTOQUE -> jdbc.tabela("exportar o estoque",
                    "SELECT nome AS \"Insumo\", "
                            + "CASE categoria WHEN 'RACAO' THEN 'Ração' WHEN 'VACINA' THEN 'Vacina' "
                            + "WHEN 'MEDICAMENTO' THEN 'Medicamento' ELSE 'Material' END AS \"Categoria\", "
                            + "unidade AS \"Unidade\", saldo AS \"Saldo\", nivel_minimo AS \"Nível mínimo\", "
                            + "CASE WHEN saldo <= nivel_minimo THEN 'Sim' ELSE 'Não' END AS \"Abaixo do mínimo\" "
                            + "FROM insumo WHERE ativo = 1 ORDER BY nome");
            case ALIMENTACAO -> jdbc.tabela("exportar a alimentação",
                    "SELECT a.data AS \"Data\", l.codigo AS \"Lote\", i.nome AS \"Insumo\", "
                            + "a.quantidade_kg AS \"Quantidade (kg)\" FROM registro_alimentacao a "
                            + "JOIN lote l ON l.id = a.lote_id JOIN insumo i ON i.id = a.insumo_id "
                            + "WHERE a.data BETWEEN ? AND ? ORDER BY a.data, l.codigo",
                    inicio, fim);
            case MORTALIDADE -> jdbc.tabela("exportar a mortalidade",
                    "SELECT substr(m.data_hora, 1, 10) AS \"Data\", l.codigo AS \"Lote\", "
                            + "m.quantidade AS \"Aves mortas\", COALESCE(m.observacao, '') AS \"Observação\" "
                            + "FROM movimento_lote m JOIN lote l ON l.id = m.lote_id "
                            + "WHERE m.tipo = 'MORTALIDADE' AND substr(m.data_hora, 1, 10) BETWEEN ? AND ? "
                            + "ORDER BY m.data_hora, m.id",
                    inicio, fim);
            case PRODUCAO -> jdbc.tabela("exportar a produção",
                    "SELECT p.data AS \"Data\", l.codigo AS \"Lote\", "
                            + "CASE p.tipo WHEN 'POSTURA' THEN 'Postura' ELSE 'Pesagem' END AS \"Registro\", "
                            + "p.valor AS \"Valor\", CASE p.tipo WHEN 'POSTURA' THEN 'ovos' ELSE 'g por ave' END AS \"Unidade\", "
                            + "COALESCE(p.vivos, '') AS \"Aves vivas no dia\" FROM producao p "
                            + "JOIN lote l ON l.id = p.lote_id WHERE p.data BETWEEN ? AND ? ORDER BY p.data, l.codigo",
                    inicio, fim);
            case FINANCEIRO -> jdbc.tabela("exportar o financeiro",
                    "SELECT f.data AS \"Data\", CASE f.tipo WHEN 'RECEITA' THEN 'Receita' ELSE 'Despesa' END AS \"Tipo\", "
                            + ROTULO_CATEGORIA_FINANCEIRA + " AS \"Categoria\", f.descricao AS \"Descrição\", "
                            + "printf('%d,%02d', f.valor_centavos / 100, f.valor_centavos % 100) AS \"Valor (R$)\", "
                            + "COALESCE(l.codigo, '') AS \"Lote\" FROM lancamento_financeiro f "
                            + "LEFT JOIN lote l ON l.id = f.lote_id WHERE f.data BETWEEN ? AND ? ORDER BY f.data, f.id",
                    inicio, fim);
        };
    }
}
