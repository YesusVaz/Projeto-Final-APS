import { Link, Navigate, Outlet, useLocation } from 'react-router'
import type { Perfil } from '../api/http'
import { CabecalhoPagina, classesBotao, Pagina } from '../ui'
import { useSessao } from './sessaoContexto'

type RotaProtegidaProps = {
  // sem a lista, qualquer perfil logado entra
  perfis?: Perfil[]
}

// exige sessao. com `perfis`, exige tambem um deles. sem permissao mostra "Sem acesso" na tela
// em vez de redirecionar calado
export function RotaProtegida({ perfis }: RotaProtegidaProps) {
  const { sessao, aviso } = useSessao()
  const local = useLocation()

  if (!sessao) {
    return <Navigate to="/entrar" replace state={{ de: local.pathname, aviso }} />
  }
  if (perfis && !perfis.includes(sessao.perfil)) {
    return <SemAcesso />
  }
  return <Outlet />
}

function SemAcesso() {
  return (
    <Pagina>
      <CabecalhoPagina
        titulo="Sem acesso"
        descricao="Seu perfil não tem permissão para abrir esta tela. Se isso for um engano, fale com o gestor."
        acoes={
          <Link to="/" className={classesBotao('primario')}>
            Voltar ao início
          </Link>
        }
      />
    </Pagina>
  )
}
