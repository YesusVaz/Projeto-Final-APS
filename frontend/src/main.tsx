import { MotionConfig } from 'motion/react'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router'
import './index.css'
import { Rotas } from './rotas.tsx'
import { SessaoProvider } from './sessao/SessaoProvider.tsx'
import { Vitrine } from './telas/Vitrine.tsx'
import { ToastProvider } from './ui'

// /#vitrine abre a conferencia dos componentes. o resto do sistema roda pelo roteador
const vitrine = window.location.hash === '#vitrine'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MotionConfig reducedMotion="user">
      <ToastProvider>
        {vitrine ? (
          <Vitrine />
        ) : (
          <BrowserRouter>
            <SessaoProvider>
              <Rotas />
            </SessaoProvider>
          </BrowserRouter>
        )}
      </ToastProvider>
    </MotionConfig>
  </StrictMode>,
)