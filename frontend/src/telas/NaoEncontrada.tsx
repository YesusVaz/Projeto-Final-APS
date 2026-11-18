import { Link } from 'react-router'
import { CabecalhoPagina, classesBotao, Pagina } from '../ui'

export function NaoEncontrada() {
  return (
    <Pagina>
      <CabecalhoPagina
        titulo="Página não encontrada"
        descricao="O endereço não existe ou foi movido."
        acoes={
          <Link to="/" className={classesBotao('primario')}>
            Voltar ao início
          </Link>
        }
      />
    </Pagina>
  )
}
