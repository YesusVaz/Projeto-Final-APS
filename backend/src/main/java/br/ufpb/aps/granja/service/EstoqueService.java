package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.MovimentoEstoque;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.InsumoRepository;
import br.ufpb.aps.granja.repository.LancamentoRepository;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.time.LocalDateTime;
import java.util.List;

/** UC07 (cadastrar insumo), UC08 (entrada e saida) e UC09 (alerta de minimo). */
public final class EstoqueService {

    private final UnidadeDeTrabalho uow;
    private final InsumoRepository insumos;
    private final LancamentoRepository lancamentos;

    public EstoqueService(UnidadeDeTrabalho uow, InsumoRepository insumos, LancamentoRepository lancamentos) {
        this.uow = uow;
        this.insumos = insumos;
        this.lancamentos = lancamentos;
    }

    public Insumo cadastrarInsumo(Usuario usuario, String nome, CategoriaInsumo categoria,
                                  String unidade, Quantidade nivelMinimo) {
        Autorizacao.exigir(usuario, Acao.CADASTRAR_INSUMO);
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do insumo é obrigatório.");
        }
        if (unidade == null || unidade.isBlank()) {
            throw new IllegalArgumentException("A unidade do insumo é obrigatória.");
        }
        if (nivelMinimo == null) {
            throw new IllegalArgumentException("O nível mínimo é obrigatório.");
        }
        return uow.executar(() -> insumos.inserir(nome.trim(), categoria, unidade.trim(), nivelMinimo));
    }

    /** UC08: entrada ou saida. Saldo nunca negativo (RN07). Insumo inativo recusa (RN19). */
    public Insumo movimentar(Usuario usuario, long insumoId, TipoMovimentoEstoque tipo, int quantidade,
                             OrigemMovimento origem, CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.MOVIMENTAR_ESTOQUE);
        Validacao.positiva(quantidade, "quantidade do movimento");
        return uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            Insumo insumo = carregar(insumoId);
            MovimentoEstoque movimento = new MovimentoEstoque(null, insumoId, tipo,
                    Quantidade.de(quantidade), codigo, origem, null, agora);
            insumo.aplicar(movimento);
            insumos.registrarMovimento(movimento);
            insumos.atualizar(insumo);
            return insumo;
        });
    }

    public List<Insumo> listar(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        return insumos.listar();
    }

    /** UC09: insumos com saldo igual ou abaixo do minimo. */
    public List<Insumo> alertasDeEstoque(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR_ALERTAS_ESTOQUE);
        return insumos.listar().stream().filter(Insumo::abaixoDoMinimo).toList();
    }

    private Insumo carregar(long insumoId) {
        return Buscas.insumo(insumos, insumoId);
    }
}
