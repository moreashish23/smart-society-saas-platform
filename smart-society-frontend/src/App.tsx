import { Routes, Route, Navigate } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { ProtectedRoute } from '@/components/common/ProtectedRoute'

// ── Auth pages (public) ───────────────────────────────────────────────────────
import LoginPage         from '@/pages/auth/LoginPage'
import RegisterPage      from '@/pages/auth/RegisterPage'
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage'
import ResetPasswordPage  from '@/pages/auth/ResetPasswordPage'

// ── App pages (protected) ─────────────────────────────────────────────────────
import DashboardPage      from '@/pages/dashboard/DashboardPage'
import ComplaintsPage     from '@/pages/complaints/ComplaintsPage'
import NewComplaintPage   from '@/pages/complaints/NewComplaintPage'
import ComplaintDetailPage from '@/pages/complaints/ComplaintDetailPage'
import VendorsPage        from '@/pages/vendors/VendorsPage'
import NoticesPage        from '@/pages/notices/NoticesPage'
import NotificationsPage  from '@/pages/notifications/NotificationsPage'
import AnalyticsPage      from '@/pages/analytics/AnalyticsPage'
import AuditPage          from '@/pages/audit/AuditPage'
import SocietiesPage      from '@/pages/society/SocietiesPage'
import MembersPage        from '@/pages/society/MembersPage'
import ChangePasswordPage from '@/pages/auth/ChangePasswordPage'

export default function App() {
  return (
    <Routes>
      {/* ── Public routes ──────────────────────────────────────────────── */}
      <Route path="/login"           element={<LoginPage />} />
      <Route path="/register"        element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password"  element={<ResetPasswordPage />} />

      {/* ── Protected routes — all authenticated users ─────────────────── */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>

          {/* Dashboard */}
          <Route path="/dashboard"        element={<DashboardPage />} />

          {/* Complaints — all roles */}
          <Route path="/complaints"       element={<ComplaintsPage />} />
          <Route path="/complaints/new"   element={
            <ProtectedRoute allowedRoles={['RESIDENT']}>
              <NewComplaintPage />
            </ProtectedRoute>
          } />
          <Route path="/complaints/:id"   element={<ComplaintDetailPage />} />

          {/* Vendors — all roles can view, managers can manage */}
          <Route path="/vendors"          element={<VendorsPage />} />

          {/* Notice board — all roles */}
          <Route path="/notices"          element={<NoticesPage />} />

          {/* Notifications — all roles */}
          <Route path="/notifications"    element={<NotificationsPage />} />

          {/* Analytics — managers and above */}
          <Route path="/analytics"        element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'SOCIETY_MANAGER', 'COMMITTEE_MEMBER']}>
              <AnalyticsPage />
            </ProtectedRoute>
          } />

          {/* Audit — managers and above */}
          <Route path="/audit"            element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'SOCIETY_MANAGER']}>
              <AuditPage />
            </ProtectedRoute>
          } />

          {/* Society management — Super Admin only */}
          <Route path="/societies"        element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <SocietiesPage />
            </ProtectedRoute>
          } />

          {/* Members — managers and above */}
          <Route path="/members"          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'SOCIETY_MANAGER', 'COMMITTEE_MEMBER']}>
              <MembersPage />
            </ProtectedRoute>
          } />

          {/* Profile / account */}
          <Route path="/change-password"  element={<ChangePasswordPage />} />
          <Route path="/profile"          element={<Navigate to="/change-password" replace />} />

          {/* Catch-all redirect */}
          <Route path="/"                 element={<Navigate to="/dashboard" replace />} />
          <Route path="*"                 element={<Navigate to="/dashboard" replace />} />
        </Route>
      </Route>

      {/* Root redirect */}
      <Route path="/"  element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}