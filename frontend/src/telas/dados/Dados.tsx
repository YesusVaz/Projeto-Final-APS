import { useState } from 'react'
import { motion } from 'motion/react'
import { baixarBackup, criarBackup, CONJUNTOS, exportar, listarBackups, type Backup, type ConjuntoExportavel } from '../../api/dados'
import { dataHora, hoje, primeiroDiaDoMes, tamanhoDeArquivo } from '../../api/formatar'
import { ErroApi } from '../../api/http'
import { useCarregar } from '../../hooks/useCarregar'
import { useLogado, useSessao } from '../../sessao/sessaoContexto'
import { Aviso, Botao, CabecalhoPagina, Campo, Entrada, EsqueletoLista, EstadoVazio, Pagina, propsItem, TituloSecao, useToast } from '../../ui'
import { Carregavel } from '../comum/Carregavel'

// UC28 e UC29: o gestor leva os dados em CSV e guarda copias do banco
export function Dados() {
  const { token } = useLogado()
  const { aoErro } = useSessao()
  const avisar = useToast()
  const [de, setDe] = useState(primeiroDiaDoMes())
  const [ate, setAte] = useState(hoje())
  const [ocupado, setOcupado] = useState<string | null>(null)
  const [erro, setErro] = useState('')
  const backups = useCarregar(listarBackups)

  const periodoInvalido = de > ate

  // uma acao de cada vez: o botao clicado fica ocupado e o resto espera
  async function fazer(chave: string, acao: () => Promise<string>, mensagem: (nome: string) => string) {
    if (ocupado) return
    setOcupado(chave)
    setErro('')
    try {
      const nome = await acao()
      avisar({ texto: mensagem(nome) })
    } catch (falha) {
      if (!aoErro(falha)) setErro(falha instanceof ErroApi ? falha.message : 'Não foi possível concluir. Tente de novo.')
    } finally {
      setOcupado(null)
    }
  }

  const exportarConjunto = (c: ConjuntoExportavel) => fazer(`csv-${c.chave}`, () => exportar(token, c, de, ate), (nome) => `${nome} baixado.`)

  async function fazerBackup() {
    await fazer(
      'backup',
      async () => {
        const novo = await criarBackup(token)
        backups.recarregar()
        return novo.nome
      },
      () => 'Backup feito.',
    )
  }

  return (
    <Pagina>
      <CabecalhoPagina titulo="Dados" descricao="Leve os dados da granja para uma planilha e guarde cópias de segurança." />
      {erro ? (
        <Aviso tom="critico" className="mb-6">
          {erro}
        </Aviso>
      ) : null}

      <div className="grid items-start gap-14 lg:grid-cols-2">
        <motion.section {...propsItem(0)} aria-labelledby="titulo-exportar">
          <TituloSecao id="titulo-exportar" className="mb-1 md:text-lg md:tracking-[-0.015em] md:text-ink">
            Exportar para planilha
          </TituloSecao>
          <p className="mb-5 max-w-[52ch] text-[13.5px] text-ink-3">Arquivos CSV, que o Excel e o Google Planilhas abrem direto.</p>
          <div className="mb-2 flex flex-wrap items-end gap-3">
            <Campo rotulo="De" erro={periodoInvalido ? 'A data inicial vem antes da final.' : undefined}>
              {(c) => <Entrada {...c} type="date" max={ate} value={de} onChange={(e) => setDe(e.target.value)} />}
            </Campo>
            <Campo rotulo="Até">
              {(c) => <Entrada {...c} type="date" min={de} max={hoje()} value={ate} onChange={(e) => setAte(e.target.value)} />}
            </Campo>
          </div>
          <p className="mb-3 text-[13px] text-ink-3">O período vale para alimentação, mortalidade, produção e financeiro.</p>
          <ul>
            {CONJUNTOS.map((c) => (
              <li key={c.chave} className="flex items-center gap-4 border-t border-line-soft py-4 first:border-t-0">
                <span className="min-w-0 flex-1">
                  <b className="font-medium">{c.titulo}</b>
                  <br />
                  <span className="text-[13.5px] text-ink-3">{c.descricao}</span>
                </span>
                <Botao
                  variante="contorno"
                  tamanho="sm"
                  icone="baixar"
                  disabled={ocupado !== null || (c.usaPeriodo && periodoInvalido)}
                  aria-label={`Baixar ${c.titulo} em CSV`}
                  onClick={() => void exportarConjunto(c)}
                >
                  {ocupado === `csv-${c.chave}` ? 'Baixando...' : 'CSV'}
                </Botao>
              </li>
            ))}
          </ul>
        </motion.section>

        <motion.section {...propsItem(1)} aria-labelledby="titulo-backup">
          <div className="mb-1 flex items-center justify-between gap-4">
            <TituloSecao id="titulo-backup" className="md:text-lg md:tracking-[-0.015em] md:text-ink">
              Cópias de segurança
            </TituloSecao>
            <Botao icone="mais" tamanho="sm" disabled={ocupado !== null} onClick={() => void fazerBackup()}>
              {ocupado === 'backup' ? 'Fazendo...' : 'Fazer backup agora'}
            </Botao>
          </div>
          <p className="mb-5 max-w-[52ch] text-[13.5px] text-ink-3">Uma cópia completa do banco de dados, com tudo que está registrado até agora.</p>
          <Carregavel carga={backups.carga} recarregar={backups.recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
            {(lista) =>
              lista.length === 0 ? (
                <EstadoVazio icone="arquivo" titulo="Nenhuma cópia ainda" texto="Faça o primeiro backup pelo botão acima." />
              ) : (
                <ul>
                  {lista.map((b) => (
                    <LinhaBackup key={b.nome} backup={b} ocupado={ocupado !== null} baixando={ocupado === `backup-${b.nome}`} onBaixar={() => void fazer(`backup-${b.nome}`, () => baixarBackup(token, b), (nome) => `${nome} baixado.`)} />
                  ))}
                </ul>
              )
            }
          </Carregavel>
        </motion.section>
      </div>
    </Pagina>
  )
}

type LinhaBackupProps = { backup: Backup; ocupado: boolean; baixando: boolean; onBaixar: () => void }

function LinhaBackup({ backup, ocupado, baixando, onBaixar }: LinhaBackupProps) {
  return (
    <li className="flex items-center gap-4 border-t border-line-soft py-4 first:border-t-0">
      <span className="min-w-0 flex-1">
        <b className="font-medium">{dataHora(backup.criadoEm)}</b>
        <br />
        <span className="font-mono text-[13px] break-all text-ink-3">
          {backup.nome} · {tamanhoDeArquivo(backup.tamanhoBytes)}
        </span>
      </span>
      <Botao variante="contorno" tamanho="sm" icone="baixar" disabled={ocupado} aria-label={`Baixar o backup de ${dataHora(backup.criadoEm)}`} onClick={onBaixar}>
        {baixando ? 'Baixando...' : 'Baixar'}
      </Botao>
    </li>
  )
}
