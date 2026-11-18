import { motion } from 'motion/react'
import { useState, type FormEvent } from 'react'
import { cadastrarFornecedor, listarFornecedores } from '../../api/fornecedores'
import { useCarregar } from '../../hooks/useCarregar'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Botao, CabecalhoPagina, Campo, Entrada, EsqueletoLista, EstadoVazio, Icone, Pagina, Painel, propsItem, useToast } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { documentoPlausivel, EMAIL_VALIDO, formatarDocumento } from '../comum/validacao'
import { useEnvio } from '../lotes/useEnvio'

type Erros = Partial<Record<'nome' | 'documento' | 'email', string>>

export function Fornecedores() {
  const { token } = useLogado()
  const avisar = useToast()
  const { carga, recarregar } = useCarregar(listarFornecedores)
  const { enviando, erro, enviar } = useEnvio()
  const [aberto, setAberto] = useState(false)
  const [nome, setNome] = useState('')
  const [documento, setDocumento] = useState('')
  const [telefone, setTelefone] = useState('')
  const [email, setEmail] = useState('')
  const [erros, setErros] = useState<Erros>({})

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos: Erros = {}
    if (!nome.trim()) novos.nome = 'Informe o nome do fornecedor.'
    if (!documentoPlausivel(documento)) novos.documento = 'Informe um CPF (11 dígitos) ou um CNPJ (14 dígitos).'
    if (email.trim() && !EMAIL_VALIDO.test(email.trim())) novos.email = 'Informe um e-mail válido.'
    setErros(novos)
    if (Object.keys(novos).length > 0) return
    const ok = await enviar(async () => {
      const f = await cadastrarFornecedor(token, {
        nome: nome.trim(),
        documento: documento.trim(),
        telefone: telefone.trim() || undefined,
        email: email.trim() || undefined,
      })
      avisar({ texto: `${f.nome} cadastrado.` })
    })
    if (ok) {
      setNome('')
      setDocumento('')
      setTelefone('')
      setEmail('')
      setAberto(false)
      recarregar()
    }
  }

  return (
    <Pagina>
      <CabecalhoPagina
        titulo="Fornecedores"
        descricao="Quem fornece ração, vacinas e medicamentos."
        acoes={
          <Botao onClick={() => setAberto(true)}>
            <Icone nome="mais" tamanho={16} />
            Novo fornecedor
          </Botao>
        }
      />
      <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={4} />}>
        {(fornecedores) =>
          fornecedores.length === 0 ? (
            <EstadoVazio icone="caminhao" titulo="Ainda não há fornecedores" texto="Cadastre o primeiro fornecedor com o botão acima." />
          ) : (
            <ul>
              {fornecedores.map((f, i) => (
                <motion.li key={f.id} {...propsItem(i)} className="grid items-center gap-x-6 gap-y-1 border-t border-line-soft py-4 first:border-t-0 md:grid-cols-[minmax(0,1.3fr)_minmax(0,1fr)_minmax(0,1fr)]">
                  <div className="min-w-0">
                    <p className="truncate font-medium">{f.nome}</p>
                    <p className="font-mono text-[13px] text-ink-3">
                      {f.tipoDocumento} {formatarDocumento(f.documento)}
                    </p>
                  </div>
                  <p className="text-[14px] text-ink-2">{f.telefone ?? '—'}</p>
                  <p className="truncate text-[14px] text-ink-2">{f.email ?? '—'}</p>
                </motion.li>
              ))}
            </ul>
          )
        }
      </Carregavel>

      <Painel
        aberto={aberto}
        onFechar={() => setAberto(false)}
        titulo="Novo fornecedor"
        descricao="O documento não pode repetir um já cadastrado."
        rodape={
          <>
            <Botao variante="fantasma" onClick={() => setAberto(false)} className="min-h-11">
              Cancelar
            </Botao>
            <Botao type="submit" form="form-fornecedor" disabled={enviando} className="min-h-11">
              {enviando ? 'Salvando...' : 'Cadastrar'}
            </Botao>
          </>
        }
      >
        <form id="form-fornecedor" onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
          {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
          <Campo rotulo="Nome" erro={erros.nome}>
            {(c) => <Entrada {...c} value={nome} onChange={(e) => setNome(e.target.value)} maxLength={100} />}
          </Campo>
          <Campo rotulo="CPF ou CNPJ" erro={erros.documento}>
            {(c) => <Entrada {...c} mono inputMode="numeric" value={documento} onChange={(e) => setDocumento(e.target.value)} maxLength={18} />}
          </Campo>
          <Campo rotulo="Telefone" dica="Opcional.">
            {(c) => <Entrada {...c} type="tel" value={telefone} onChange={(e) => setTelefone(e.target.value)} maxLength={20} />}
          </Campo>
          <Campo rotulo="E-mail" dica="Opcional." erro={erros.email}>
            {(c) => <Entrada {...c} type="email" value={email} onChange={(e) => setEmail(e.target.value)} maxLength={254} />}
          </Campo>
        </form>
      </Painel>
    </Pagina>
  )
}
