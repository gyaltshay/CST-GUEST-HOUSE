import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { App } from './App'
import { api } from './services/api'

afterEach(() => vi.restoreAllMocks())

describe('Sprint 2 foundation', () => {
  it('renders the home page and its availability link', () => {
    render(<MemoryRouter><App /></MemoryRouter>)
    expect(screen.getByRole('heading', { name: 'CST Guest House' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Room availability' })).toHaveAttribute('href', '/availability')
  })

  it('shows loading and then the backend health status', async () => {
    vi.spyOn(api, 'get').mockResolvedValue({ data: { status: 'UP' } })
    render(<MemoryRouter initialEntries={['/availability']}><App /></MemoryRouter>)
    expect(screen.getByRole('status')).toHaveTextContent('Checking')
    expect(await screen.findByText('Guest house service: UP')).toBeInTheDocument()
  })

  it('shows a retry option when the backend cannot be reached', async () => {
    vi.spyOn(api, 'get').mockRejectedValue(new Error('Unavailable'))
    render(<MemoryRouter initialEntries={['/availability']}><App /></MemoryRouter>)
    expect(await screen.findByRole('alert')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument()
  })
})
