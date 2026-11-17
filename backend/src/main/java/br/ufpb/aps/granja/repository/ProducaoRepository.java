package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.RegistroProducao;
import br.ufpb.aps.granja.model.TipoProducao;

import java.time.LocalDate;
import java.util.List;

/** Contrato de persistencia da producao (UC16). */
public interface ProducaoRepository {

    RegistroProducao inserir(RegistroProducao registro);

    boolean existe(long loteId, TipoProducao tipo, LocalDate data);

    List<RegistroProducao> listarPorLote(long loteId);
}
