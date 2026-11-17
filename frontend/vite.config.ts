import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// o backend nao manda cabecalho de CORS, entao o front chama /api
// e o proxy do vite repassa a chamada pro backend
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': {
        target: process.env.GRANJA_API ?? 'http://127.0.0.1:8080',
      },
    },
  },
})
