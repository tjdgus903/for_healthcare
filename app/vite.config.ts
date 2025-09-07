import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    proxy: {
      // 모든 /api 요청은 백엔드(Spring Boot, 8080)로 프록시
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '')
      }
    }
  }
});