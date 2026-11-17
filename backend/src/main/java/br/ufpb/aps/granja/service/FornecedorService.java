package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.Documento;
import br.ufpb.aps.granja.model.Fornecedor;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.FornecedorJaExisteException;
import br.ufpb.aps.granja.repository.FornecedorRepository;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.time.LocalDateTime;
import java.util.List;

/** UC10: cadastro de fornecedor. RN14: CPF ou CNPJ valido e unico (checado aqui e no UNIQUE do banco). */
public final class FornecedorService {

    private final UnidadeDeTrabalho uow;
    private final FornecedorRepository fornecedores;
    private final BarramentoEventos eventos;

    public FornecedorService(UnidadeDeTrabalho uow, FornecedorRepository fornecedores, BarramentoEventos eventos) {
        this.uow = uow;
        this.fornecedores = fornecedores;
        this.eventos = eventos;
    }

    public Fornecedor cadastrar(Usuario usuario, String nome, String documento, String telefone, String email,
                                LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.CADASTRAR_FORNECEDOR);
        Fornecedor novo = new Fornecedor(null, nome, Documento.de(documento), telefone, email);
        Fornecedor gravado = uow.executar(() -> {
            if (fornecedores.buscarPorDocumento(novo.documento()).isPresent()) {
                throw new FornecedorJaExisteException(novo.documento().numero());
            }
            return fornecedores.inserir(novo);
        });
        eventos.publicar(new OperacaoRealizada("FORNECEDOR", "Fornecedor " + gravado.nome() + " ("
                + gravado.documento().numero() + ") cadastrado por " + usuario.login(), agora));
        return gravado;
    }

    public List<Fornecedor> listar(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        return fornecedores.listar();
    }
}
