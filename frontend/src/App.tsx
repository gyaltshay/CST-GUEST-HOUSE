import { Link, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { AvailabilityPage } from './pages/UserPages'

export function App() {
  return <Routes>
    <Route element={<Layout />}>
      <Route index element={<section>
        <h1>CST Guest House</h1>
        <p>Accommodation at the College of Science and Technology.</p>
        <Link to="/availability">Check the guest house service</Link>
      </section>} />
      <Route path="availability" element={<AvailabilityPage />} />
    </Route>
  </Routes>
}
