import { useState, type ReactNode } from 'react'
import {
  Abas,
  Aviso,
  Botao,
  Cab,
  Caixa,
  Campo,
  CabecalhoPagina,
  Cartao,
  Cel,
  Contador,
  Dialogo,
  Entrada,
  Estatistica,
  Fichas,
  Interruptor,
  ItemLista,
  Linha,
  Marca,
  Menu,
  MenuItem,
  NavInferior,
  NavPilula,
  Pagina,
  Painel,
  Progresso,
  Selecao,
  Selo,
  Tabela,
  TabelaCabeca,
  TabelaCorpo,
  Avatar,
  useToast,
  type ItemInferior,
  type ItemNav,
} from '../ui'

type Aba = 'ocorrencias' | 'vacinacoes' | 'tratamentos'
type Ficha = 'todos' | 'mortalidade' | 'estoque'
type Secao = 'inicio' | 'lotes' | 'estoque' | 'sanidade' | 'producao' | 'alertas' | 'conta'
type Inferior = 'inicio' | 'lotes' | 'alertas' | 'estoque'

const NAV: ItemNav<Secao>[] = [
  { id: 'inicio', rotulo: 'Início' },
  { id: 'lotes', rotulo: 'Lotes' },
  { id: 'estoque', rotulo: 'Estoque' },
  { id: 'sanidade', rotulo: 'Sanidade' },
  { id: 'producao', rotulo: 'Produção' },
  { id: 'alertas', rotulo: 'Alertas', contador: 7 },
  { id: 'conta', rotulo: 'GE', compacto: true, descricao: 'Conta e administração' },
]

const INFERIOR: ItemInferior<Inferior>[] = [
  { id: 'inicio', rotulo: 'Início', icone: 'inicio' },
  { id: 'lotes', rotulo: 'Lotes', icone: 'lotes' },
  { id: 'alertas', rotulo: 'Alertas', icone: 'alertas' },
  { id: 'estoque', rotulo: 'Estoque', icone: 'estoque' },
]

function Bloco({ nome, children }: { nome: string; children: ReactNode }) {
  return (
    <section className="border-t border-line py-7">
      <h2 className="mb-5 text-lg font-medium tracking-[-0.015em]">{nome}</h2>
      {children}
    </section>
  )
}

// pagina de conferencia dos componentes. abre em /#vitrine e nao entra no menu do sistema
export function Vitrine() {
  const avisar = useToast()
  const [secao, setSecao] = useState<Secao>('lotes')
  const [inferior, setInferior] = useState<Inferior>('inicio')
  const [aba, setAba] = useState<Aba>('ocorrencias')
  const [ficha, setFicha] = useState<Ficha>('todos')
  const [ativo, setAtivo] = useState(true)
  const [marcado, setMarcado] = useState(true)
  const [qtd, setQtd] = useState(40)
  const [painel, setPainel] = useState(false)
  const [dialogo, setDialogo] = useState(false)

  return (
    <Pagina>
      <header className="flex flex-wrap items-center justify-between gap-3 pt-5">
        <div className="min-w-30 flex-1">
          <Marca />
        </div>
        <NavPilula itens={NAV} ativo={secao} onMudar={setSecao} />
        <div className="flex min-w-30 flex-1 justify-end">
          <Menu
            gatilho={(p) => (
              <Botao variante="acao" icone="mais" {...p}>
                Registrar
              </Botao>
            )}
          >
            <MenuItem icone="baixa" descricao="Baixas do dia">
              Mortalidade
            </MenuItem>
            <MenuItem icone="racao" descricao="Ração e consumo">
              Alimentação
            </MenuItem>
            <MenuItem icone="vacina" descricao="Doses aplicadas">
              Vacinação
            </MenuItem>
          </Menu>
        </div>
      </header>

      <CabecalhoPagina trilha="Granja · design" titulo="Componentes" descricao="Conferência dos componentes de src/ui. Abra com /#vitrine." />

      <Bloco nome="Botão">
        <div className="flex flex-wrap items-center gap-2.5">
          <Botao>Primário</Botao>
          <Botao variante="acao" icone="mais">
            Registrar
          </Botao>
          <Botao variante="contorno">Secundário</Botao>
          <Botao variante="fantasma">Cancelar</Botao>
          <Botao variante="perigo">Excluir</Botao>
          <Botao tamanho="lg">Grande</Botao>
          <Botao tamanho="sm" variante="contorno">
            Pequeno
          </Botao>
          <Botao tamanho="icone" variante="contorno" icone="buscar" aria-label="Buscar" />
          <Botao disabled>Desativado</Botao>
        </div>
      </Bloco>

      <Bloco nome="Campos">
        <div className="grid max-w-3xl gap-5 sm:grid-cols-2">
          <Campo rotulo="Usuário">{(c) => <Entrada {...c} placeholder="seu.usuario" />}</Campo>
          <Campo rotulo="Senha" erro="As senhas não são iguais.">
            {(c) => <Entrada {...c} type="password" defaultValue="12345678" />}
          </Campo>
          <Campo rotulo="Lote">
            {(c) => (
              <Selecao {...c}>
                <option>LOT-2026-003 · Frango de corte</option>
                <option>LOT-2026-002 · Poedeira</option>
              </Selecao>
            )}
          </Campo>
          <Campo rotulo="Código do lote" dica="Gerado pelo sistema.">
            {(c) => <Entrada {...c} mono disabled defaultValue="LOT-2026-004" />}
          </Campo>
          <Campo rotulo="Aves mortas" className="sm:col-span-2">
            {(c) => <Contador {...c} valor={qtd} onMudar={setQtd} rotuloMenos="Menos uma" rotuloMais="Mais uma" />}
          </Campo>
        </div>
      </Bloco>

      <Bloco nome="Seleção">
        <div className="flex flex-wrap items-center gap-8">
          <Caixa marcado={marcado} onMudar={setMarcado} rotulo="Lembrar neste aparelho" />
          <div className="flex items-center gap-3">
            <Interruptor marcado={ativo} onMudar={setAtivo} rotulo="Usuário ativo" />
            {ativo ? 'Ativo' : 'Inativo'}
          </div>
        </div>
      </Bloco>

      <Bloco nome="Status">
        <div className="flex flex-wrap gap-2.5">
          <Selo nivel="normal">Normal</Selo>
          <Selo nivel="atencao">Atenção</Selo>
          <Selo nivel="critico">Crítico</Selo>
          <Selo nivel="info">Ativo</Selo>
          <Selo>Encerrado</Selo>
        </div>
      </Bloco>

      <Bloco nome="Abas e filtros">
        <div className="flex flex-col items-start gap-4">
          <Abas
            rotulo="Sanidade"
            valor={aba}
            onMudar={setAba}
            opcoes={[
              { id: 'ocorrencias', rotulo: 'Ocorrências' },
              { id: 'vacinacoes', rotulo: 'Vacinações' },
              { id: 'tratamentos', rotulo: 'Tratamentos' },
            ]}
          />
          <Fichas
            rotulo="Filtro de alertas"
            valor={ficha}
            onMudar={setFicha}
            opcoes={[
              { id: 'todos', rotulo: 'Todos', contador: 7 },
              { id: 'mortalidade', rotulo: 'Mortalidade', contador: 2 },
              { id: 'estoque', rotulo: 'Estoque', contador: 2 },
            ]}
          />
        </div>
      </Bloco>

      <Bloco nome="Avisos">
        <div className="flex max-w-xl flex-col gap-2.5">
          <Aviso tom="critico" icone="cadeado" destaque="Conta bloqueada por 15 minutos.">
            Houve 5 senhas erradas seguidas.
          </Aviso>
          <Aviso tom="atencao">Estoque de ração fica abaixo do mínimo.</Aviso>
          <Aviso tom="ok">Registro salvo no histórico do lote.</Aviso>
          <Aviso tom="info">A idade do lote é contada a partir da chegada.</Aviso>
        </div>
        <div className="mt-5 flex gap-2.5">
          <Botao variante="contorno" onClick={() => avisar({ texto: 'Mortalidade registrada', acao: { rotulo: 'Desfazer', aoClicar: () => {} } })}>
            Mostrar toast
          </Botao>
          <Botao variante="contorno" onClick={() => setPainel(true)}>
            Abrir painel
          </Botao>
          <Botao variante="contorno" onClick={() => setDialogo(true)}>
            Abrir diálogo
          </Botao>
        </div>
      </Bloco>

      <Bloco nome="Nível, tabela e lista">
        <div className="grid gap-12 lg:grid-cols-2">
          <div className="flex flex-col gap-5">
            <div>
              <div className="mb-2 flex justify-between">
                <span>Ração inicial</span>
                <span className="font-mono text-ink-3">620 / 400 kg</span>
              </div>
              <Progresso valor={100} rotulo="Ração inicial" />
            </div>
            <div>
              <div className="mb-2 flex justify-between">
                <span>Ração de crescimento</span>
                <span className="font-mono text-ink-3">280 / 300 kg</span>
              </div>
              <Progresso valor={93} tom="atencao" indice={1} rotulo="Ração de crescimento" />
            </div>
            <div>
              <div className="mb-2 flex justify-between">
                <span>Vacina Gumboro</span>
                <span className="font-mono text-ink-3">1.200 / 6.000 doses</span>
              </div>
              <Progresso valor={20} tom="critico" indice={2} rotulo="Vacina Gumboro" />
            </div>
          </div>
          <Tabela>
            <TabelaCabeca>
              <tr>
                <Cab>Código</Cab>
                <Cab numerica>Saldo</Cab>
                <Cab>Situação</Cab>
              </tr>
            </TabelaCabeca>
            <TabelaCorpo>
              <Linha>
                <Cel className="font-mono font-medium">LOT-2026-003</Cel>
                <Cel numerica>2.380</Cel>
                <Cel>
                  <Selo nivel="atencao">Atenção</Selo>
                </Cel>
              </Linha>
              <Linha>
                <Cel className="font-mono font-medium">LOT-2026-001</Cel>
                <Cel numerica>4.820</Cel>
                <Cel>
                  <Selo nivel="normal">Normal</Selo>
                </Cel>
              </Linha>
            </TabelaCorpo>
          </Tabela>
        </div>
        <div className="mt-8 max-w-md">
          <ItemLista href="#vitrine">
            <Avatar iniciais="MS" />
            <span className="flex-1">
              <b className="font-medium">Maria Souza</b>
              <br />
              <span className="text-[13px] text-ink-3">Funcionário</span>
            </span>
          </ItemLista>
          <ItemLista>
            <Avatar iniciais="RL" />
            <span className="flex-1">
              <b className="font-medium">Rafael Lima</b>
              <br />
              <span className="text-[13px] text-ink-3">Técnico</span>
            </span>
          </ItemLista>
        </div>
      </Bloco>

      <Bloco nome="Resumo e cartão">
        <div className="grid items-start gap-12 md:grid-cols-[1fr_1fr_1fr_1.2fr]">
          <Estatistica valor="10.350" rotulo="Aves vivas" descricao="em 3 lotes ativos" />
          <Estatistica valor="280" rotulo="Mortes hoje" descricao="2,6% das aves" indice={1} />
          <Estatistica valor="2.140 kg" rotulo="Ração em estoque" descricao="1 insumo abaixo do mínimo" indice={2} />
          <Cartao>
            <h3 className="text-lg font-medium">Precisa de atenção</h3>
            <p className="mt-1 text-ink-3">Cartão só onde há decisão.</p>
          </Cartao>
        </div>
      </Bloco>

      <Painel
        aberto={painel}
        onFechar={() => setPainel(false)}
        titulo="Novo lote"
        descricao="O código é criado pelo sistema."
        rodape={
          <>
            <Botao variante="fantasma" onClick={() => setPainel(false)}>
              Cancelar
            </Botao>
            <Botao onClick={() => setPainel(false)}>Criar lote</Botao>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          <Campo rotulo="Espécie">
            {(c) => (
              <Selecao {...c}>
                <option>Frango de corte</option>
                <option>Poedeira</option>
              </Selecao>
            )}
          </Campo>
          <Campo rotulo="Quantidade de aves">{(c) => <Entrada {...c} mono type="number" defaultValue={2000} />}</Campo>
        </div>
      </Painel>

      <Dialogo
        aberto={dialogo}
        onFechar={() => setDialogo(false)}
        titulo="Inativar Rafael Lima?"
        descricao="Ele perde o acesso agora. O histórico dele continua no sistema."
        rodape={
          <>
            <Botao variante="fantasma" onClick={() => setDialogo(false)}>
              Cancelar
            </Botao>
            <Botao variante="perigo" onClick={() => setDialogo(false)}>
              Inativar
            </Botao>
          </>
        }
      />

      <NavInferior itens={INFERIOR} ativo={inferior} onMudar={setInferior} acao={{ rotulo: 'Registrar', onClick: () => avisar({ texto: 'Abre o menu de registro' }) }} />
    </Pagina>
  )
}
