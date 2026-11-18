import { Link } from 'react-router'

// "Inicio" ou "Estoque" pequeno acima do titulo, levando de volta pra tela de onde a pessoa veio
export function Trilha({ para, texto }: { para: string; texto: string }) {
  return (
    <Link to={para} className="rounded text-accent hover:underline">
      {texto}
    </Link>
  )
}
