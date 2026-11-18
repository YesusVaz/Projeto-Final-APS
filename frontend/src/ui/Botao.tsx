import type { ButtonHTMLAttributes } from 'react'
import { classesBotao, type TamanhoBotao, type VarianteBotao } from './classesBotao'
import { Icone, type NomeIcone } from './Icone'

type BotaoProps = Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'type'> & {
  variante?: VarianteBotao
  tamanho?: TamanhoBotao
  icone?: NomeIcone
  iconeDireita?: NomeIcone
  type?: 'button' | 'submit'
}

// botao so com icone (tamanho="icone") precisa de aria-label
export function Botao({
  variante = 'primario',
  tamanho = 'md',
  icone,
  iconeDireita,
  type = 'button',
  className,
  children,
  ...resto
}: BotaoProps) {
  const miolo = tamanho === 'sm' ? 16 : 18
  return (
    <button type={type} className={classesBotao(variante, tamanho, className)} {...resto}>
      {icone ? <Icone nome={icone} tamanho={miolo} /> : null}
      {children}
      {iconeDireita ? <Icone nome={iconeDireita} tamanho={miolo} /> : null}
    </button>
  )
}
