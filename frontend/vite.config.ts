/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      pwaAssets: {
        preset: 'minimal-2023',
        image: 'public/logo.svg',
        overrideManifestIcons: true,
      },
      manifest: {
        name: 'Hunter Exam Trainer',
        short_name: 'Hunter Exam',
        description: 'Practice app for the Swedish Hunter Theory Exam (Jägarexamen)',
        theme_color: '#863bff',
        background_color: '#ffffff',
        display: 'standalone',
        start_url: '/',
      },
    }),
  ],
  test: {
    environment: 'jsdom',
    setupFiles: './src/__tests__/setupTests.ts',
  },
})
