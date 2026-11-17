import { useState } from 'react'
import { sair, type Sessao } from './api/sessao'
import { Navegacao, type OpcaoNav } from './componentes/Navegacao'
import { Cadastro } from './telas/Cadastro'
import { Entrar } from './telas/Entrar'

type Tela = 'entrar' | 'cadastro'

// as duas abas sao publicas. o que muda com o gestor logado e so o que aparece dentro do cadastro
const OPCOES: OpcaoNav<Tela>[] = [
  { id: 'entrar', rotulo: 'Entrar' },
  { id: 'cadastro', rotulo: 'Cadastro de usuário' },
]

export default function App() {
  const [tela, setTela] = useState<Tela>('entrar')
  const [sessao, setSessao] = useState<Sessao | null>(null)

  async function encerrarSessao() {
    if (sessao) {
      await sair(sessao.token)
    }
    setSessao(null)
    setTela('entrar')
  }

  return (
    <div className="mx-auto flex min-h-screen w-full max-w-[1280px] flex-col px-6 pb-10 md:px-12">
      <Navegacao opcoes={OPCOES} ativo={tela} onMudar={setTela} />

      <main className="flex-1">
        {tela === 'entrar' ? (
          <Entrar sessao={sessao} onEntrar={setSessao} onSair={encerrarSessao} />
        ) : (
          <Cadastro
            sessao={sessao}
            onIrEntrar={() => setTela('entrar')}
            onSessaoInvalida={() => setSessao(null)}
          />
        )}
      </main>
    </div>
  )
}
