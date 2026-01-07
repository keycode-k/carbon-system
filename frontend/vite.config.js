import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api/development': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      '/api/company': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/trade': {
        target: 'http://localhost:8084',
        changeOrigin: true
      },
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true
      }
    }
  }
})
