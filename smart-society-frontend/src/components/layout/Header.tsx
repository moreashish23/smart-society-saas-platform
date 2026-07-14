import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { useNotifications } from '@/hooks/useNotifications'
import { formatRole, formatRelative } from '@/lib/utils'
import { LuBell, LuLogOut, LuUser, LuSettings, LuChevronDown } from 'react-icons/lu'
import { Button } from '@/components/ui/Button'

export function Header() {
  const { user, logout, isManager, isSuperAdmin } = useAuth()
  const { unreadCount, notifications, markAllRead } = useNotifications()
  const navigate = useNavigate()

  const [showUserMenu, setShowUserMenu]     = useState(false)
  const [showNotifPanel, setShowNotifPanel] = useState(false)

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-4 pl-14 md:px-6 md:pl-6">
      {/* Page breadcrumb / title area — left */}
      <div />

      {/* Right controls */}
      <div className="flex items-center gap-3">

        {/* Notification bell */}
        <div className="relative">
          <button
            onClick={() => { setShowNotifPanel(!showNotifPanel); setShowUserMenu(false) }}
            className="relative rounded-lg p-2 text-gray-500 hover:bg-gray-100"
          >
            <LuBell className="h-5 w-5" />
            {unreadCount > 0 && (
              <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>

          {showNotifPanel && (
            <div className="absolute right-0 top-12 z-50 w-96 rounded-xl border border-gray-200 bg-white shadow-xl">
              <div className="flex items-center justify-between border-b border-gray-200 px-4 py-3">
                <h3 className="font-semibold text-gray-900">Notifications</h3>
                {unreadCount > 0 && (
                  <Button variant="ghost" size="sm" onClick={markAllRead}>
                    Mark all read
                  </Button>
                )}
              </div>
              <div className="max-h-80 overflow-y-auto divide-y divide-gray-100">
                {notifications.length === 0 ? (
                  <p className="py-8 text-center text-sm text-gray-500">No notifications</p>
                ) : (
                  notifications.slice(0, 10).map((n) => (
                    <div
                      key={n.id}
                      className={`px-4 py-3 hover:bg-gray-50 ${!n.read ? 'bg-blue-50' : ''}`}
                    >
                      <p className="text-sm font-medium text-gray-900">{n.title}</p>
                      <p className="mt-0.5 text-xs text-gray-600 line-clamp-2">{n.message}</p>
                      <p className="mt-1 text-xs text-gray-400">{formatRelative(n.createdAt)}</p>
                    </div>
                  ))
                )}
              </div>
              <div className="border-t border-gray-200 p-2">
                <Link
                  to="/notifications"
                  className="block rounded-lg px-3 py-2 text-center text-sm text-blue-600 hover:bg-blue-50"
                  onClick={() => setShowNotifPanel(false)}
                >
                  View all notifications
                </Link>
              </div>
            </div>
          )}
        </div>

        {/* User menu */}
        <div className="relative">
          <button
            onClick={() => { setShowUserMenu(!showUserMenu); setShowNotifPanel(false) }}
            className="flex items-center gap-2 rounded-lg px-3 py-2 hover:bg-gray-100"
          >
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-700">
              {user?.firstName?.[0]}{user?.lastName?.[0]}
            </div>
            <div className="hidden text-left md:block">
              <p className="text-sm font-medium text-gray-900">{user?.firstName} {user?.lastName}</p>
              <p className="text-xs text-gray-500">{user?.role ? formatRole(user.role) : ''}</p>
            </div>
            <LuChevronDown className="h-4 w-4 text-gray-400" />
          </button>

          {showUserMenu && (
            <div className="absolute right-0 top-12 z-50 w-56 rounded-xl border border-gray-200 bg-white shadow-xl">
              <div className="border-b border-gray-100 px-4 py-3">
                <p className="text-sm font-medium text-gray-900">{user?.firstName} {user?.lastName}</p>
                <p className="text-xs text-gray-500">{user?.email}</p>
              </div>
              <div className="p-1">
                <Link
                  to="/profile"
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  onClick={() => setShowUserMenu(false)}
                >
                  <LuUser className="h-4 w-4" /> My Profile
                </Link>
                <Link
                  to="/change-password"
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  onClick={() => setShowUserMenu(false)}
                >
                  <LuSettings className="h-4 w-4" /> Change Password
                </Link>
                <button
                  onClick={handleLogout}
                  className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-red-600 hover:bg-red-50"
                >
                  <LuLogOut className="h-4 w-4" /> Sign Out
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Click-outside handler */}
      {(showUserMenu || showNotifPanel) && (
        <div
          className="fixed inset-0 z-40"
          onClick={() => { setShowUserMenu(false); setShowNotifPanel(false) }}
        />
      )}
    </header>
  )
}