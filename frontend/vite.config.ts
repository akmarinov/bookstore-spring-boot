import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Simple Vite config to fix React loading issues
export default defineConfig({
  plugins: [react()],
  
  build: {
    outDir: 'dist',
    sourcemap: false,
    target: 'es2020',
    rollupOptions: {
      output: {
        manualChunks: undefined, // Disable manual chunking to avoid React loading issues
      }
    }
  },

  server: {
    port: 5173,
    host: 'localhost',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },

  define: {
    __DEV__: false,
    __PROD__: true,
  },
});