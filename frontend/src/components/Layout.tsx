import { Link, Outlet } from 'react-router-dom'

export function Layout() {
  return <>
    <header className="public-header">
      <div className="container">
        <Link className="brand" to="/">
          <img src="/images/logo.jpg" alt="" />
          <span>CST Guest House</span>
        </Link>
        <nav aria-label="Main navigation">
          <Link to="/">Home</Link>{' · '}
          <Link to="/availability">Room availability</Link>
        </nav>
      </div>
    </header>
    <main className="container"><Outlet /></main>
  </>
}
