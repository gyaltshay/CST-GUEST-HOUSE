import { render, screen, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, expect, it, vi } from 'vitest'
import { App } from './App'
import { api } from './services/api'

afterEach(() => vi.restoreAllMocks())

it('renders the public home and its stay link', () => {
  vi.spyOn(api, 'get').mockResolvedValue({ data: { registrationEnabled: true } })
  render(<MemoryRouter><App /></MemoryRouter>)
  expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('A thoughtful stay')
  expect(within(screen.getByRole('main')).getByRole('link', { name: 'Plan your stay' })).toHaveAttribute('href', '/app/availability')
})

it('asks an unauthenticated visitor to sign in before viewing availability', async () => {
  vi.spyOn(api, 'get').mockResolvedValue({ data: { registrationEnabled: true } })
  render(<MemoryRouter initialEntries={['/app/availability']}><App /></MemoryRouter>)
  expect(await screen.findByRole('button', { name: /sign in/i })).toBeInTheDocument()
})
