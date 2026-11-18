import { Link } from 'react-router'
import { CabecalhoPagina, classesBotao, EstadoVazio, Pagina } from '../ui'

// telas do lote 3 ja registradas nas rotas, so pra o menu e os links nao cairem em 404
export function EmBreve({ titulo }: { titulo: string }) {
  return (
    <Pagina>
      <CabecalhoPagina titulo={titulo} />
      <EstadoVazio
        icone="calendario"
        titulo="Em breve"
        texto="Esta tela ainda está sendo feita."
        acao={
          <Link to="/" className={classesBotao('contorno')}>
            Voltar ao início
          </Link>
        }
      />
    </Pagina>
  )
}
