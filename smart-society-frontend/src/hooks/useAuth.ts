import { useAppSelector, useAppDispatch } from '@/store'
import {
  selectCurrentUser,
  selectIsAuthenticated,
  selectIsLoading,
  selectUserRole,
  selectUserSocietyId,
  logoutThunk,
  clearCredentials,
} from '@/store/slices/authSlice'
import type { UserRole } from '@/types'

export function useAuth() {
  const dispatch        = useAppDispatch()
  const user            = useAppSelector(selectCurrentUser)
  const isAuthenticated = useAppSelector(selectIsAuthenticated)
  const isLoading       = useAppSelector(selectIsLoading)
  const role            = useAppSelector(selectUserRole)
  const societyId       = useAppSelector(selectUserSocietyId)

  const logout = async () => {
    await dispatch(logoutThunk())
    dispatch(clearCredentials())
  }

  const hasRole = (...roles: UserRole[]) =>
    role ? roles.includes(role) : false

  const isManagerOrAbove = hasRole(
    'SUPER_ADMIN', 'SOCIETY_MANAGER', 'COMMITTEE_MEMBER',
  )

  const isSuperAdmin  = role === 'SUPER_ADMIN'
  const isManager     = role === 'SOCIETY_MANAGER'
  const isCommittee   = role === 'COMMITTEE_MEMBER'
  const isResident    = role === 'RESIDENT'
  const isVendor      = role === 'VENDOR'
  const isStaff       = role === 'MAINTENANCE_STAFF'

  return {
    user,
    isAuthenticated,
    isLoading,
    role,
    societyId,
    logout,
    hasRole,
    isManagerOrAbove,
    isSuperAdmin,
    isManager,
    isCommittee,
    isResident,
    isVendor,
    isStaff,
  }
}