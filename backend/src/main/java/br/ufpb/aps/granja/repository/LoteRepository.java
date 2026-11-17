package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MovimentoLote;
import br.ufpb.aps.granja.model.Quantidade;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia dos lotes e do historico. O servico so conhece essa interface. */
public interface LoteRepository {

    Lote inserir(String codigo, Especie especie, Quantidade quantidadeInicial);

    Optional<Lote> buscarPorId(long id);

    List<Lote> listar();

    List<MovimentoLote> historico(long loteId);

    void registrarMovimento(MovimentoLote movimento);

    void atualizar(Lote lote);

    /** Quantos lotes ja tem codigo desse ano. Base do sequencial LOT-AAAA-NNN (RN04). */
    int contarPorAno(int ano);
}
