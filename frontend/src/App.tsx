import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout, Protected, PublicLayout, ScrollToTop } from './components/Layout'
import { AuthProvider } from './context/AuthContext'
import { AuthPage } from './pages/AuthPages'
import { AboutPage, ContactPage, FacilitiesPage, HomePage, NotFoundPage, RoomsPage } from './pages/PublicPages'
import { AvailabilityPage, BookingDetailsPage, MyBookingsPage, NewBookingPage, ProfilePage, RoomDetailsPage, UserDashboard } from './pages/UserPages'
import { FacilityDetailsPage, FacilityListPage, NewFacilityPage } from './pages/FacilityPages'

export function App() {
  return <AuthProvider>
    <ScrollToTop />
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<HomePage />} />
        <Route path="rooms" element={<RoomsPage />} />
        <Route path="facilities" element={<FacilitiesPage />} />
        <Route path="about" element={<AboutPage />} />
        <Route path="contact" element={<ContactPage />} />
        <Route path="login" element={<AuthPage />} />
        <Route path="register" element={<AuthPage register />} />
        <Route path="forbidden" element={<NotFoundPage forbidden />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
      <Route element={<Protected />}>
        <Route path="availability" element={<Navigate to="/app/availability" replace />} />
        <Route path="app" element={<AppLayout />}>
          <Route index element={<UserDashboard />} />
          <Route path="availability" element={<AvailabilityPage />} />
          <Route path="rooms/:id" element={<RoomDetailsPage />} />
          <Route path="bookings" element={<MyBookingsPage />} />
          <Route path="bookings/new" element={<NewBookingPage />} />
          <Route path="bookings/:id" element={<BookingDetailsPage />} />
          <Route path="profile" element={<ProfilePage />} />
          <Route path="facilities" element={<FacilityListPage />} />
          <Route path="facilities/new" element={<NewFacilityPage />} />
          <Route path="facilities/:id" element={<FacilityDetailsPage />} />
        </Route>
      </Route>
      <Route element={<Protected admin />}>
        <Route path="admin" element={<Navigate to="/app" replace />} />
      </Route>
    </Routes>
  </AuthProvider>
}
