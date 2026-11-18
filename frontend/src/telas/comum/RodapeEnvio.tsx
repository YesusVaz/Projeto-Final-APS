import { Link } from 'react-router'
import { Botao, classesBotao } from '../../ui'

type RodapeEnvioProps = {
  voltarPara?: string
  rotulo: string
  rotuloEnviando: string
  enviando: boolean
  desabilitado?: boolean
}

// botoes do pe do formulario. no celular ficam presos embaixo da tela, ao alcance do polegar.
// a pagina precisa de pb-24 no formulario pra o ultimo campo nao ficar escondido atras deles
export function RodapeEnvio({ voltarPara = '/', rotulo, rotuloEnviando, enviando, desabilitado }: RodapeEnvioProps) {
  return (
    <div className="fixed inset-x-0 bottom-0 z-10 flex justify-end gap-2.5 border-t border-line-soft bg-paper/95 p-4 backdrop-blur md:static md:border-0 md:bg-transparent md:p-0">
      <Link to={voltarPara} className={classesBotao('fantasma', 'md', 'min-h-11')}>
        Cancelar
      </Link>
      <Botao type="submit" disabled={enviando || desabilitado} className="min-h-11">
        {enviando ? rotuloEnviando : rotulo}
      </Botao>
    </div>
  )
}
