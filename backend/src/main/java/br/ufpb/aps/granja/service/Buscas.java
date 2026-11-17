package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import br.ufpb.aps.granja.repository.InsumoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.SanidadeRepository;

/** Busca por id que vira NaoEncontradoException (404) quando nao acha. Usada por todos os servicos. */
final class Buscas {

    private Buscas() {
    }

    static Lote lote(LoteRepository lotes, long loteId) {
        return lotes.buscarPorId(loteId)
                .orElseThrow(() -> new NaoEncontradoException("Lote " + loteId + " não encontrado."));
    }

    static Insumo insumo(InsumoRepository insumos, long insumoId) {
        return insumos.buscarPorId(insumoId)
                .orElseThrow(() -> new NaoEncontradoException("Insumo " + insumoId + " não encontrado."));
    }

    static OcorrenciaSanitaria ocorrencia(SanidadeRepository sanidade, long ocorrenciaId) {
        return sanidade.buscarOcorrencia(ocorrenciaId)
                .orElseThrow(() -> new NaoEncontradoException("Ocorrência " + ocorrenciaId + " não encontrada."));
    }
}
