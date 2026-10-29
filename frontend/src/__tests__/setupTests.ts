// Runs before every test file (see "setupFiles" in vite.config.ts).

// Adds DOM checks such as expect(element).toBeInTheDocument()
import '@testing-library/jest-dom/vitest'
import { afterEach } from 'vitest'
import { cleanup } from '@testing-library/react'

// Loads the translations, so components using t() show the Swedish texts
// in tests too, the same as in the app (see main.tsx)
import '../i18n/config'

// Remove whatever a test rendered, so tests don't affect each other
afterEach(() => cleanup())
