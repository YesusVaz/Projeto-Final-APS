package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.Fornecedor;

public record FornecedorDto(long id, String nome, String documento, String tipoDocumento, String telefone,
                            String email) {

    public static FornecedorDto de(Fornecedor f) {
        return new FornecedorDto(f.id(), f.nome(), f.documento().numero(), f.documento().ehCpf() ? "CPF" : "CNPJ",
                f.telefone(), f.email());
    }
}
