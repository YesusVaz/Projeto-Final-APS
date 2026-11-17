package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.MovimentoEstoque;
import br.ufpb.aps.granja.model.Quantidade;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia dos insumos e do extrato. */
public interface InsumoRepository {

    Insumo inserir(String nome, CategoriaInsumo categoria, String unidade, Quantidade nivelMinimo);

    Optional<Insumo> buscarPorId(long id);

    List<Insumo> listar();

    /** Grava saldo e se o insumo ta ativo. */
    void atualizar(Insumo insumo);

    void registrarMovimento(MovimentoEstoque movimento);

    List<MovimentoEstoque> extrato(long insumoId);
}
