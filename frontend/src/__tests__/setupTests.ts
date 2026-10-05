// Runs before every test file (see "setupFiles" in vite.config.ts).

// Adds DOM checks such as expect(element).toBeInTheDocument()
import '@testing-library/jest-dom/vitest'
import { afterEach } from 'vitest'
import { cleanup } from '@testing-library/react'

// Remove whatever a test rendered, so tests don't affect each other
afterEach(() => cleanup())
