import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Simple Vite config to fix React loading issues
export default defineConfig({
  plugins: [react()],
  
  build: {
    outDir: 'dist',
    sourcemap: false,
    minify: false, // Disable minification to debug
    target: 'es2015', // Use more compatible target
    rollupOptions: {
      output: {
        manualChunks: undefined,
        // Ensure proper module format
        format: 'es',
        // Prevent tree-shaking React
        preserveModules: false,
      }
    }
  },

  esbuild: {
    // Ensure JSX is handled properly
    jsx: 'transform',
    jsxFactory: 'React.createElement',
    jsxFragment: 'React.Fragment',
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

  // Test configuration
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    exclude: ['**/e2e/**', '**/playwright/**', '**/*.spec.ts', '**/node_modules/**'],
  },
});