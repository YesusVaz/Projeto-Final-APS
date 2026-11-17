package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MovimentoEstoque;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.RegistroAlimentacao;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.LoteEncerradoException;
<<<<<<< HEAD
=======
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import br.ufpb.aps.granja.repository.AlimentacaoRepository;
import br.ufpb.aps.granja.repository.InsumoRepository;
import br.ufpb.aps.granja.repository.LancamentoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * UC13: registro da alimentacao. Numa transacao so (RN23) grava o registro, a saida de racao
 * no estoque (RN10) e o codigo do lancamento. Se faltar racao, nao grava nada.
 */
public final class AlimentacaoService {

    private final UnidadeDeTrabalho uow;
    private final LoteRepository lotes;
    private final InsumoRepository insumos;
    private final AlimentacaoRepository alimentacoes;
    private final LancamentoRepository lancamentos;

    public AlimentacaoService(UnidadeDeTrabalho uow, LoteRepository lotes, InsumoRepository insumos,
                              AlimentacaoRepository alimentacoes, LancamentoRepository lancamentos) {
        this.uow = uow;
        this.lotes = lotes;
        this.insumos = insumos;
        this.alimentacoes = alimentacoes;
        this.lancamentos = lancamentos;
    }

    public RegistroAlimentacao registrar(Usuario usuario, long loteId, long insumoId, int quantidadeKg,
                                         LocalDate data, CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.REGISTRAR_ALIMENTACAO);
        Validacao.positiva(quantidadeKg, "quantidade de ração em kg");
        return uow.executar(() -> {
            lancamentos.registrar(codigo, agora);

<<<<<<< HEAD
            Lote lote = Buscas.lote(lotes, loteId);
=======
            Lote lote = lotes.buscarPorId(loteId)
                    .orElseThrow(() -> new NaoEncontradoException("Lote " + loteId + " não encontrado."));
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
            if (!lote.estaAtivo()) {
                throw new LoteEncerradoException(lote.codigo());
            }

<<<<<<< HEAD
            Insumo insumo = Buscas.insumo(insumos, insumoId);
=======
            Insumo insumo = insumos.buscarPorId(insumoId)
                    .orElseThrow(() -> new NaoEncontradoException("Insumo " + insumoId + " não encontrado."));
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
            if (insumo.categoria() != CategoriaInsumo.RACAO) {
                throw new RegraDeNegocioException("Só insumo da categoria RACAO pode ser usado na alimentação (RN10).");
            }

            Quantidade kg = Quantidade.de(quantidadeKg);
            MovimentoEstoque saida = new MovimentoEstoque(null, insumoId, TipoMovimentoEstoque.SAIDA, kg,
                    codigo, OrigemMovimento.ALIMENTACAO, loteId, agora);
            insumo.aplicar(saida);
            insumos.registrarMovimento(saida);
            insumos.atualizar(insumo);

            return alimentacoes.inserir(new RegistroAlimentacao(null, loteId, insumoId, kg, data, codigo));
        });
    }
}
