import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { listarAlertasMortalidade } from '../api/lotes'
import { useCarregar } from '../hooks/useCarregar'
import { ROTULO_PERFIL } from '../sessao/permissoes'
import { useLogado, useSessao } from '../sessao/sessaoContexto'
import {
  Avatar,
  Botao,
  Dialogo,
  Icone,
  Marca,
  Menu,
  MenuItem,
  NavInferior,
  NavPilula,
  type ItemInferior,
  type ItemNav,
} from '../ui'
import { adminDoPerfil, idAtivo, itensDoPerfil, registrosDoPerfil, type IdNav } from './navegacao'
import { RotaAnimada } from './RotaAnimada'

// no celular a barra de baixo comporta 4 itens + o "+". o resto vira link dentro do "+"
const ITENS_NO_CELULAR: IdNav[] = ['inicio', 'lotes', 'alertas', 'producao']

function iniciais(nome: string) {
  const partes = nome.trim().split(/\s+/)
  return ((partes[0]?.[0] ?? '') + (partes.length > 1 ? (partes.at(-1)?.[0] ?? '') : '')).toUpperCase()
}

export function LayoutApp() {
  const { nome, perfil } = useLogado()
  const { sair } = useSessao()
  const navegar = useNavigate()
  const { pathname } = useLocation()
  const [atalhosAbertos, setAtalhosAbertos] = useState(false)
  const [contaAberta, setContaAberta] = useState(false)

  // conta alertas pra mostrar na navbar. busca de novo a cada troca de rota, assim o numero
  // acompanha o que a pessoa acabou de registrar. se falhar, so some o contador
  const { carga } = useCarregar(listarAlertasMortalidade, pathname)
  const abertos = carga.situacao === 'pronto' ? carga.dados.length : 0

  const itens = itensDoPerfil(perfil)
  const ativo = idAtivo(pathname)
  const registros = registrosDoPerfil(perfil)
  const admin = adminDoPerfil(perfil)

  // no desktop o avatar fica dentro da pilula, como no desenho, e abre o menu da conta
  const itensDesktop: ItemNav<IdNav | 'conta'>[] = [
    ...itens.map((i) => ({
      id: i.id,
      rotulo: i.rotulo,
      contador: i.id === 'alertas' && abertos > 0 ? abertos : undefined,
    })),
    { id: 'conta', rotulo: iniciais(nome), compacto: true, acao: true, descricao: 'Menu da conta' },
  ]
  const itensCelular: ItemInferior<IdNav>[] = itens
    .filter((i) => ITENS_NO_CELULAR.includes(i.id))
    .map((i) => ({ id: i.id, rotulo: i.rotulo, icone: i.icone }))
  const outrosItens = itens.filter((i) => !ITENS_NO_CELULAR.includes(i.id))

  function irPara(id: IdNav | 'conta') {
    if (id === 'conta') {
      setContaAberta((a) => !a)
      return
    }
    const destino = itens.find((i) => i.id === id)
    if (destino) navegar(destino.caminho)
  }

  const conteudoConta = (
    <>
      <div className="px-3 py-2.5">
        <p className="font-medium">{nome}</p>
        <p className="text-[12.5px] text-ink-3">{ROTULO_PERFIL[perfil]}</p>
      </div>
      {admin.map((a) => (
        <MenuItem key={a.caminho} icone={a.icone} onSelecionar={() => navegar(a.caminho)}>
          {a.rotulo}
        </MenuItem>
      ))}
      <MenuItem icone="sair" onSelecionar={() => void sair()}>
        Sair
      </MenuItem>
    </>
  )

  return (
    <div className="min-h-dvh">
      <header className="mx-auto grid w-full max-w-[1440px] grid-cols-[1fr_auto_1fr] items-center gap-4 px-6 pt-5 md:px-12 2xl:px-16">
        <Link to="/" className="col-start-1 justify-self-start rounded-lg" aria-label="Granja, ir para o início">
          <Marca />
        </Link>

        <div className="relative col-start-2 hidden md:block">
          <NavPilula itens={itensDesktop} ativo={ativo} onMudar={irPara} />
          {/* o menu nasce na borda de baixo da pilula, alinhado a direita */}
          <div className="absolute right-0 bottom-0">
            <Menu aberto={contaAberta} onMudarAberto={setContaAberta} gatilho={() => null}>
              {conteudoConta}
            </Menu>
          </div>
        </div>

        <div className="col-start-3 flex items-center justify-end gap-2">
          {registros.length > 0 ? (
            <div className="hidden md:block">
              <Menu
                gatilho={(props) => (
                  <Botao variante="acao" icone="mais" {...props}>
                    Registrar
                  </Botao>
                )}
              >
                {registros.map((r) => (
                  <MenuItem key={r.caminho} icone={r.icone} descricao={r.descricao} onSelecionar={() => navegar(r.caminho)}>
                    {r.rotulo}
                  </MenuItem>
                ))}
              </Menu>
            </div>
          ) : null}
          <div className="md:hidden">
            <Menu
              gatilho={(props) => (
                <button
                  type="button"
                  {...props}
                  aria-label="Menu da conta"
                  className="flex size-11 items-center justify-center rounded-full transition-colors hover:bg-trilho"
                >
                  <Avatar iniciais={iniciais(nome)} className="size-9" />
                </button>
              )}
            >
              {conteudoConta}
            </Menu>
          </div>
        </div>
      </header>

      <main>
        <RotaAnimada />
      </main>

      <div className="md:hidden">
        <NavInferior
          itens={itensCelular}
          ativo={ITENS_NO_CELULAR.includes(ativo) ? ativo : 'inicio'}
          onMudar={irPara}
          acao={{ rotulo: 'Registrar e mais opções', onClick: () => setAtalhosAbertos(true) }}
        />
        <Dialogo
          aberto={atalhosAbertos}
          onFechar={() => setAtalhosAbertos(false)}
          titulo="O que você quer fazer?"
          rodape={
            <Botao variante="contorno" onClick={() => setAtalhosAbertos(false)}>
              Fechar
            </Botao>
          }
        >
          <ul className="-mx-2 flex flex-col">
            {[...registros, ...outrosItens.map((i) => ({ rotulo: i.rotulo, caminho: i.caminho, icone: i.icone, descricao: 'Abrir a tela' }))].map((a) => (
              <li key={a.caminho}>
                <Link
                  to={a.caminho}
                  onClick={() => setAtalhosAbertos(false)}
                  className="flex min-h-12 items-center gap-3 rounded-[14px] px-2 py-1.5 text-sm hover:bg-hover"
                >
                  <Icone nome={a.icone} tamanho={20} />
                  <span>
                    {a.rotulo}
                    <small className="block text-[12.5px] leading-tight text-ink-3">{a.descricao}</small>
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </Dialogo>
      </div>
    </div>
  )
}
