import { fileURLToPath, URL } from 'node:url'
import path from 'path'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, ''),
        configure: (proxy, options) => {
          // 代理配置
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // 保留原始cookie
            proxyReq.setHeader('Cookie', req.headers.cookie || '');
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            // 保留响应cookie
            const cookies = proxyRes.headers['set-cookie'];
            if (cookies) {
              proxyRes.headers['set-cookie'] = cookies.map(cookie =>
                cookie.replace('SameSite=Lax', 'SameSite=None; Secure')
              );
            }
          });
        }
      },
      '/public': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/prod-api/public': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
