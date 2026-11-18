import { Navigate, Route, Routes, useSearchParams } from 'react-router'
import { LayoutApp } from './layout/LayoutApp'
import { RotaProtegida } from './sessao/RotaProtegida'
import { Cadastro } from './telas/acesso/Cadastro'
import { Entrar } from './telas/acesso/Entrar'
import { LayoutAcesso } from './telas/acesso/LayoutAcesso'
import { Auditoria } from './telas/admin/Auditoria'
import { Dados } from './telas/dados/Dados'
import { Relatorios } from './telas/relatorios/Relatorios'
import { Suporte } from './telas/suporte/Suporte'
import { Configuracao } from './telas/admin/Configuracao'
import { Fornecedores } from './telas/admin/Fornecedores'
import { Usuarios } from './telas/admin/Usuarios'
import { Alertas } from './telas/alertas/Alertas'
import { Financeiro } from './telas/financeiro/Financeiro'
import { Estoque } from './telas/estoque/Estoque'
import { Inicio } from './telas/inicio/Inicio'
import { DetalheLote } from './telas/lotes/DetalheLote'
import { Lotes } from './telas/lotes/Lotes'
import { NaoEncontrada } from './telas/NaoEncontrada'
import { Producao } from './telas/producao/Producao'
import { Sanidade } from './telas/sanidade/Sanidade'

// mortalidade e alimentacao viraram paineis sobre a lista de lotes. o endereco antigo continua valendo
function ParaPainelDeLotes({ painel }: { painel: 'mortalidade' | 'alimentacao' }) {
  const [params] = useSearchParams()
  const lote = params.get('lote')
  return <Navigate to={`/lotes?${painel}=1${lote ? `&lote=${lote}` : ''}`} replace />
}

export function Rotas() {
  return (
    <Routes>
      <Route element={<LayoutAcesso />}>
        <Route path="/entrar" element={<Entrar />} />
        <Route path="/cadastro" element={<Cadastro />} />
      </Route>

      <Route element={<RotaProtegida />}>
        <Route element={<LayoutApp />}>
          <Route index element={<Inicio />} />

          <Route path="lotes" element={<Lotes />} />
          <Route element={<RotaProtegida perfis={['gestor', 'tecnico']} />}>
            <Route path="lotes/novo" element={<Navigate to="/lotes?novo=1" replace />} />
          </Route>
          <Route path="lotes/:id" element={<DetalheLote />} />

          <Route path="mortalidade" element={<ParaPainelDeLotes painel="mortalidade" />} />
          <Route path="alimentacao" element={<ParaPainelDeLotes painel="alimentacao" />} />
          <Route path="producao" element={<Producao />} />

          <Route path="estoque" element={<Estoque />} />
          <Route path="estoque/entrada" element={<Navigate to="/estoque?entrada=1" replace />} />

          <Route path="alertas" element={<Alertas />} />

          <Route path="sanidade" element={<Sanidade />} />
          <Route path="sanidade/vacinacao" element={<Navigate to="/sanidade?vacinar=1" replace />} />

          <Route path="configuracao" element={<Configuracao />} />
          <Route path="suporte" element={<Suporte />} />

          <Route element={<RotaProtegida perfis={['gestor']} />}>
            <Route path="fornecedores" element={<Fornecedores />} />
            <Route path="financeiro" element={<Financeiro />} />
            <Route path="relatorios" element={<Relatorios />} />
            <Route path="dados" element={<Dados />} />
            <Route path="usuarios" element={<Usuarios />} />
            <Route path="auditoria" element={<Auditoria />} />
          </Route>

          <Route path="*" element={<NaoEncontrada />} />
        </Route>
      </Route>
    </Routes>
  )
}
