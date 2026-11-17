package br.ufpb.aps.granja.model.excecao;

/** RN14: CPF ou CNPJ ja cadastrado em outro fornecedor. Vira HTTP 409. */
public class FornecedorJaExisteException extends ConflitoException {
    public FornecedorJaExisteException(String documento) {
        super("Já existe um fornecedor com o documento " + documento + ".");
    }
}
