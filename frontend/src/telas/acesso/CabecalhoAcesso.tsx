// titulo e frase de cada tela de acesso. o h1 e focavel pra o leitor de tela
export function CabecalhoAcesso({ titulo, descricao }: { titulo: string; descricao: string }) {
  return (
    <>
      <h1 data-titulo-pagina tabIndex={-1} className="text-[40px] leading-[1.08] font-medium tracking-[-0.035em] outline-none">
        {titulo}
      </h1>
      <p className="mt-2.5 text-base text-ink-3">{descricao}</p>
    </>
  )
}
