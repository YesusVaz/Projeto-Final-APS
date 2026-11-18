package br.ufpb.aps.granja.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Uma despesa ou receita. O valor fica em centavos inteiros pra nao ter erro de arredondamento.
 * {@code loteId}, {@code fornecedorId}, {@code insumoId} e {@code quantidade} so existem quando fazem sentido
 * (a compra preenche fornecedor, insumo e quantidade).
 */
public record LancamentoFinanceiro(Long id, CategoriaFinanceira categoria, String descricao, long valorCentavos,
                                   LocalDate data, Long loteId, Long fornecedorId, Long insumoId,
                                   Integer quantidade, CodigoLancamento codigo) {

    public LancamentoFinanceiro {
        Objects.requireNonNull(categoria, "categoria");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(codigo, "codigo");
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição do lançamento é obrigatória.");
        }
        descricao = descricao.trim();
        if (descricao.length() > 200) {
            throw new IllegalArgumentException("A descrição passou de 200 caracteres.");
        }
        if (valorCentavos <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
    }

    public TipoFinanceiro tipo() {
        return categoria.tipo();
    }
}
