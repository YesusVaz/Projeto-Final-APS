package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.Higienizacao;
import br.ufpb.aps.granja.model.ProgramaIluminacao;

import java.util.List;

/** Contrato de persistencia do manejo do galpao: higienizacao (UC14) e iluminacao (UC15). */
public interface ManejoRepository {

    Higienizacao inserirHigienizacao(Higienizacao higienizacao);

    /** Em ordem de data. Entra no historico do lote (UC17). */
    List<Higienizacao> higienizacoesDoLote(long loteId);

    ProgramaIluminacao inserirPrograma(ProgramaIluminacao programa);

    /** Em ordem de data de inicio. O ultimo e o programa em vigor. */
    List<ProgramaIluminacao> programasDoLote(long loteId);
}
