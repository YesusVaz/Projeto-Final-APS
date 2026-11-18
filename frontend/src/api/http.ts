export type Perfil = 'gestor' | 'tecnico' | 'funcionario'

// a API manda o perfil em maiusculo (nome do enum do backend). o front trabalha em minusculo
export function perfilDaApi(valor: string): Perfil {
  return valor.toLowerCase() as Perfil
}

export function perfilParaApi(perfil: Perfil): string {
  return perfil.toUpperCase()
}

// erro que a API devolveu. o status fica guardado pra tela saber o que fazer
export class ErroApi extends Error {
  readonly status: number

  constructor(status: number, mensagem: string) {
    super(mensagem)
    this.status = status
  }
}

type Opcoes = {
  metodo?: 'GET' | 'POST' | 'PUT'
  token?: string
  corpo?: unknown
}

// baixa um arquivo da API (CSV, backup). o nome vem do cabecalho Content-Disposition
export async function baixar(caminho: string, token: string): Promise<{ blob: Blob; nome: string }> {
  let resposta: Response
  try {
    resposta = await fetch(`/api${caminho}`, { headers: { Authorization: `Bearer ${token}` } })
  } catch {
    throw new ErroApi(0, 'Não foi possível falar com a API. Confira se o backend está rodando.')
  }
  if (!resposta.ok) {
    const dados = (await resposta.json().catch(() => null)) as { erro?: string } | null
    throw new ErroApi(resposta.status, dados?.erro ?? `A API respondeu com status ${resposta.status}.`)
  }
  const nome = /filename="([^"]+)"/.exec(resposta.headers.get('Content-Disposition') ?? '')?.[1] ?? 'arquivo'
  return { blob: await resposta.blob(), nome }
}

// entrega o arquivo ao navegador, como se a pessoa tivesse clicado num link de download
export function salvarComo(blob: Blob, nome: string) {
  const endereco = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = endereco
  link.download = nome
  document.body.append(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(endereco), 1000)
}

// uma chamada so pra API. se o status nao for 2xx, a mensagem do backend vira o texto do erro
export async function chamar<T>(caminho: string, opcoes: Opcoes = {}): Promise<T> {
  const cabecalhos: Record<string, string> = {}
  if (opcoes.token) {
    cabecalhos.Authorization = `Bearer ${opcoes.token}`
  }
  if (opcoes.corpo !== undefined) {
    cabecalhos['Content-Type'] = 'application/json'
  }

  let resposta: Response
  try {
    resposta = await fetch(`/api${caminho}`, {
      method: opcoes.metodo ?? 'GET',
      headers: cabecalhos,
      body: opcoes.corpo !== undefined ? JSON.stringify(opcoes.corpo) : undefined,
    })
  } catch {
    throw new ErroApi(0, 'Não foi possível falar com a API. Confira se o backend está rodando.')
  }

  const dados = (await resposta.json().catch(() => null)) as { erro?: string } | null
  if (!resposta.ok) {
    throw new ErroApi(resposta.status, dados?.erro ?? `A API respondeu com status ${resposta.status}.`)
  }
  return dados as T
}
