import { useResource } from '../hooks/useResource'

export function AvailabilityPage() {
  const { data, loading, error, reload } = useResource<{ status: string }>('/health')

  return <section>
    <h1>Room availability</h1>
    <p>Date-based room search is planned for Sprint 3.</p>
    {loading && <p role="status">Checking the guest house service…</p>}
    {error && <p role="alert">{error}</p>}
    {data && <p role="status">Guest house service: {data.status}</p>}
    {error && <button type="button" onClick={reload}>Try again</button>}
  </section>
}
