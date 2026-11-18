import { Route, Routes } from 'react-router'
import { LayoutApp } from './layout/LayoutApp'
import { RotaProtegida } from './sessao/RotaProtegida'
import { Cadastro } from './telas/acesso/Cadastro'
import { Entrar } from './telas/acesso/Entrar'
import { EmBreve } from './telas/EmBreve'
import { Inicio } from './telas/inicio/Inicio'
import { DetalheLote } from './telas/lotes/DetalheLote'
import { Lotes } from './telas/lotes/Lotes'
import { NovoLote } from './telas/lotes/NovoLote'
import { NaoEncontrada } from './telas/NaoEncontrada'

// telas do lote 3: ja existem na rota, so falta o miolo
const EM_BREVE: [caminho: string, titulo: string][] = [
  ['mortalidade', 'Registrar mortalidade'],
  ['alimentacao', 'Registrar alimentação'],
  ['estoque', 'Estoque'],
  ['estoque/entrada', 'Movimentar estoque'],
  ['alertas', 'Alertas'],
  ['sanidade', 'Sanidade'],
  ['sanidade/vacinacao', 'Registrar vacinação'],
  ['producao', 'Produção'],
]

const SO_GESTOR: [caminho: string, titulo: string][] = [
  ['fornecedores', 'Fornecedores'],
  ['usuarios', 'Usuários'],
  ['auditoria', 'Auditoria'],
  ['configuracao', 'Parâmetros'],
]

export function Rotas() {
  return (
    <Routes>
      <Route path="/entrar" element={<Entrar />} />
      <Route path="/cadastro" element={<Cadastro />} />

      <Route element={<RotaProtegida />}>
        <Route element={<LayoutApp />}>
          <Route index element={<Inicio />} />
          <Route path="lotes" element={<Lotes />} />
          <Route element={<RotaProtegida perfis={['gestor', 'tecnico']} />}>
            <Route path="lotes/novo" element={<NovoLote />} />
          </Route>
          <Route path="lotes/:id" element={<DetalheLote />} />
          {EM_BREVE.map(([caminho, titulo]) => (
            <Route key={caminho} path={caminho} element={<EmBreve titulo={titulo} />} />
          ))}
          <Route element={<RotaProtegida perfis={['gestor']} />}>
            {SO_GESTOR.map(([caminho, titulo]) => (
              <Route key={caminho} path={caminho} element={<EmBreve titulo={titulo} />} />
            ))}
          </Route>
          <Route path="*" element={<NaoEncontrada />} />
        </Route>
      </Route>
    </Routes>
  )
}
