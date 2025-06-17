import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig(({ command, mode }) => {
  // Load env file based on `mode` in the current working directory.
  const env = loadEnv(mode, process.cwd(), '')
  
  const isDev = mode === 'development'
  const isProd = mode === 'production'
  const isTest = command === 'serve' && mode === 'test'

  return {
    plugins: [
      react(),
    ],
    
    // Path resolution
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
        '@components': resolve(__dirname, 'src/components'),
        '@pages': resolve(__dirname, 'src/pages'),
        '@hooks': resolve(__dirname, 'src/hooks'),
        '@api': resolve(__dirname, 'src/api'),
        '@types': resolve(__dirname, 'src/types'),
        '@test': resolve(__dirname, 'src/test'),
      },
    },

    // Development server configuration
    server: {
      port: parseInt(env.VITE_PORT) || 5173,
      host: env.VITE_HOST || 'localhost',
      open: isDev,
      cors: true,
      proxy: {
        '/api': {
          target: env.VITE_API_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        },
      },
    },

    // Preview server configuration
    preview: {
      port: parseInt(env.VITE_PREVIEW_PORT) || 4173,
      host: env.VITE_HOST || 'localhost',
    },

    // Build configuration
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: isDev || isTest,
      minify: isProd ? 'esbuild' : false,
      target: 'es2020',
      
      // Production optimizations
      rollupOptions: {
        output: {
          // Simplified chunk splitting - disable manual chunks for now to fix React issue
          manualChunks: undefined,
          
          // Asset file naming
          chunkFileNames: isProd ? 'assets/js/[name]-[hash].js' : '[name].js',
          entryFileNames: isProd ? 'assets/js/[name]-[hash].js' : '[name].js',
          assetFileNames: (assetInfo) => {
            if (!assetInfo.name) return 'assets/[name]-[hash][extname]'
            
            const info = assetInfo.name.split('.')
            const extType = info[info.length - 1]
            
            if (/png|jpe?g|svg|gif|tiff|bmp|ico/i.test(extType)) {
              return 'assets/img/[name]-[hash][extname]'
            }
            if (/woff2?|eot|ttf|otf/i.test(extType)) {
              return 'assets/fonts/[name]-[hash][extname]'
            }
            if (/css/i.test(extType)) {
              return 'assets/css/[name]-[hash][extname]'
            }
            return 'assets/[name]-[hash][extname]'
          },
        },
        
        // External dependencies (for CDN usage if needed)
        external: isProd && env.VITE_USE_CDN === 'true' ? [
          // 'react',
          // 'react-dom',
        ] : [],
      },
      
      // Compression and optimization
      cssCodeSplit: true,
      cssMinify: isProd,
      reportCompressedSize: isProd,
      chunkSizeWarningLimit: 1000,
      
      // Asset optimization
      assetsInlineLimit: 4096, // 4KB
    },

    // CSS configuration
    css: {
      devSourcemap: isDev,
      preprocessorOptions: {
        scss: {
          additionalData: `@import "@/styles/variables.scss";`
        }
      },
      modules: {
        localsConvention: 'camelCase',
      },
    },

    // Environment variables
    define: {
      __APP_VERSION__: JSON.stringify(process.env.npm_package_version || '1.0.0'),
      __BUILD_DATE__: JSON.stringify(new Date().toISOString()),
      __DEV__: isDev,
      __PROD__: isProd,
      __TEST__: isTest,
    },

    // Optimization
    optimizeDeps: {
      include: [
        'react',
        'react-dom',
        'react-router-dom',
        '@mui/material',
        '@mui/icons-material',
        'axios',
        'react-hook-form',
      ],
      exclude: ['@vitejs/plugin-react'],
    },

    // Testing configuration
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: ['./src/test/setup.ts'],
      css: true,
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json', 'html', 'lcov'],
        exclude: [
          'node_modules/',
          'src/test/',
          'src/**/*.test.{ts,tsx}',
          'src/**/*.spec.{ts,tsx}',
          'src/vite-env.d.ts',
          'src/main.tsx',
          'dist/',
          'coverage/',
          '**/*.config.{js,ts}',
          '**/.*rc.{js,ts}',
        ],
        thresholds: {
          global: {
            branches: isProd ? 80 : 70,
            functions: isProd ? 80 : 70,
            lines: isProd ? 80 : 70,
            statements: isProd ? 80 : 70,
          },
        },
        all: true,
        reportsDirectory: './coverage',
      },
      // Performance testing
      pool: 'threads',
      poolOptions: {
        threads: {
          singleThread: false,
        },
      },
    },

    // Logging
    logLevel: isDev ? 'info' : 'warn',
    clearScreen: false,
  }
})
