package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.Documento;
import br.ufpb.aps.granja.model.Fornecedor;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia dos fornecedores (UC10). */
public interface FornecedorRepository {

    Fornecedor inserir(Fornecedor fornecedor);

    Optional<Fornecedor> buscarPorDocumento(Documento documento);

    List<Fornecedor> listar();
}
