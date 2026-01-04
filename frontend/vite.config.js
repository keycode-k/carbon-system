import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/consumer': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/provider': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
