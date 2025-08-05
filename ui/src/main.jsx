import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { AuthUserProvider } from './components/Services/AuthUserProvider.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthUserProvider>
    <App />
    </AuthUserProvider>
  </StrictMode>
)
