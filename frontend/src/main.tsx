import './i18n/config'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// Tin-box fonts (#259), bundled with the app (no Google requests)
import '@fontsource/cinzel/600.css';
import '@fontsource/eb-garamond/400.css';
import '@fontsource/eb-garamond/400-italic.css';
import '@fontsource/eb-garamond/600.css';
import './styles/global.css';
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
