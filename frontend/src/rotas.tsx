import { Navigate, Route, Routes } from 'react-router'
import { LayoutApp } from './layout/LayoutApp'
import { RotaProtegida } from './sessao/RotaProtegida'
import { Cadastro } from './telas/acesso/Cadastro'
import { Entrar } from './telas/acesso/Entrar'
import { LayoutAcesso } from './telas/acesso/LayoutAcesso'
import { Auditoria } from './telas/admin/Auditoria'
import { Configuracao } from './telas/admin/Configuracao'
import { Fornecedores } from './telas/admin/Fornecedores'
import { Usuarios } from './telas/admin/Usuarios'
import { Alertas } from './telas/alertas/Alertas'
import { Estoque } from './telas/estoque/Estoque'
import { Inicio } from './telas/inicio/Inicio'
import { DetalheLote } from './telas/lotes/DetalheLote'
import { Lotes } from './telas/lotes/Lotes'
import { NaoEncontrada } from './telas/NaoEncontrada'
import { Producao } from './telas/producao/Producao'
import { Alimentacao } from './telas/registros/Alimentacao'
import { Mortalidade } from './telas/registros/Mortalidade'
import { Sanidade } from './telas/sanidade/Sanidade'

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

          <Route path="mortalidade" element={<Mortalidade />} />
          <Route path="alimentacao" element={<Alimentacao />} />
          <Route path="producao" element={<Producao />} />

          <Route path="estoque" element={<Estoque />} />
          <Route path="estoque/entrada" element={<Navigate to="/estoque?entrada=1" replace />} />

          <Route path="alertas" element={<Alertas />} />

          <Route path="sanidade" element={<Sanidade />} />
          <Route path="sanidade/vacinacao" element={<Navigate to="/sanidade?vacinar=1" replace />} />

          <Route element={<RotaProtegida perfis={['gestor']} />}>
            <Route path="fornecedores" element={<Fornecedores />} />
            <Route path="usuarios" element={<Usuarios />} />
            <Route path="auditoria" element={<Auditoria />} />
            <Route path="configuracao" element={<Configuracao />} />
          </Route>

          <Route path="*" element={<NaoEncontrada />} />
        </Route>
      </Route>
    </Routes>
  )
}
