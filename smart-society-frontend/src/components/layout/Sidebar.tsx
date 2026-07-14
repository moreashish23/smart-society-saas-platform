import { NavLink } from 'react-router-dom'
import { cn } from '@/lib/utils'
import { useAuth } from '@/hooks/useAuth'
import {
  LuLayoutDashboard, LuMessageSquare, LuBuilding2,
  LuUsers, LuStore, LuBell, LuChartColumn,
  LuScrollText, LuStickyNote, LuChevronLeft,
  LuChevronRight, LuMenu, LuX,
} from 'react-icons/lu'
import { useState, useEffect } from 'react'

interface NavItem {
  to: string
  label: string
  icon: React.ReactNode
  roles: string[]
}

const NAV_ITEMS: NavItem[] = [
  { to: '/dashboard',     label: 'Dashboard',     icon: <LuLayoutDashboard />, roles: ['SUPER_ADMIN','SOCIETY_MANAGER','COMMITTEE_MEMBER','RESIDENT','VENDOR','MAINTENANCE_STAFF'] },
  { to: '/complaints',    label: 'Complaints',    icon: <LuMessageSquare />,   roles: ['SUPER_ADMIN','SOCIETY_MANAGER','COMMITTEE_MEMBER','RESIDENT','VENDOR','MAINTENANCE_STAFF'] },
  { to: '/societies',     label: 'Societies',     icon: <LuBuilding2 />,       roles: ['SUPER_ADMIN'] },
  { to: '/members',       label: 'Members',       icon: <LuUsers />,           roles: ['SUPER_ADMIN','SOCIETY_MANAGER','COMMITTEE_MEMBER'] },
  { to: '/vendors',       label: 'Vendors',       icon: <LuStore />,           roles: ['SUPER_ADMIN','SOCIETY_MANAGER','COMMITTEE_MEMBER','RESIDENT'] },
  { to: '/notices',       label: 'Notice Board',  icon: <LuStickyNote />,      roles: ['SUPER_ADMIN','SOCIETY_MANAGER','COMMITTEE_MEMBER','RESIDENT','MAINTENANCE_STAFF'] },
  { to: '/notifications', label: 'Notifications', icon: <LuBell />,            roles: ['SUPER_ADMIN','SOCIETY_MANAGER','COMMITTEE_MEMBER','RESIDENT','VENDOR','MAINTENANCE_STAFF'] },
  { to: '/analytics',     label: 'Analytics',     icon: <LuChartColumn />,       roles: ['SUPER_ADMIN','SOCIETY_MANAGER','COMMITTEE_MEMBER'] },
  { to: '/audit',         label: 'Audit Logs',    icon: <LuScrollText />,      roles: ['SUPER_ADMIN','SOCIETY_MANAGER'] },
]

function NavItems({ collapsed, onNavClick }: { collapsed: boolean; onNavClick?: () => void }) {
  const { role } = useAuth()
  const items = NAV_ITEMS.filter(i => role && i.roles.includes(role))

  return (
    <nav className="flex-1 overflow-y-auto px-2 py-3">
      <ul className="space-y-0.5">
        {items.map((item) => (
          <li key={item.to}>
            <NavLink
              to={item.to}
              onClick={onNavClick}
              className={({ isActive }) => cn(
                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all',
                isActive ? 'bg-blue-600 text-white shadow-sm' : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900',
                collapsed && 'justify-center px-0',
              )}
              title={collapsed ? item.label : undefined}
            >
              <span className="text-[18px] flex-shrink-0">{item.icon}</span>
              {!collapsed && <span className="truncate">{item.label}</span>}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}

export function Sidebar() {
  const [collapsed, setCollapsed]   = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)

  useEffect(() => {
    const close = () => { if (window.innerWidth >= 768) setMobileOpen(false) }
    window.addEventListener('resize', close)
    return () => window.removeEventListener('resize', close)
  }, [])

  const Logo = ({ showText }: { showText: boolean }) => (
    <div className={cn('flex items-center gap-2', !showText && 'justify-center')}>
      <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-blue-600">
        <LuBuilding2 className="h-4 w-4 text-white" />
      </div>
      {showText && <span className="truncate font-bold text-gray-900">SmartSociety</span>}
    </div>
  )

  return (
    <>
      {/* Mobile trigger */}
      <button
        onClick={() => setMobileOpen(true)}
        className="fixed left-4 top-[18px] z-50 flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 bg-white shadow-sm md:hidden"
      >
        <LuMenu className="h-4 w-4 text-gray-600" />
      </button>

      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 z-40 bg-black/50 md:hidden" onClick={() => setMobileOpen(false)} />
      )}

      {/* Mobile drawer */}
      <aside className={cn(
        'fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-gray-200 bg-white transition-transform duration-300 md:hidden',
        mobileOpen ? 'translate-x-0' : '-translate-x-full',
      )}>
        <div className="flex h-16 items-center justify-between border-b border-gray-200 px-4">
          <Logo showText />
          <button onClick={() => setMobileOpen(false)} className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100">
            <LuX className="h-4 w-4" />
          </button>
        </div>
        <NavItems collapsed={false} onNavClick={() => setMobileOpen(false)} />
      </aside>

      {/* Desktop sidebar */}
      <aside className={cn(
        'hidden md:flex flex-col border-r border-gray-200 bg-white flex-shrink-0 transition-all duration-300',
        collapsed ? 'w-[60px]' : 'w-60',
      )}>
        <div className="flex h-16 flex-shrink-0 items-center justify-between border-b border-gray-200 px-3">
          <Logo showText={!collapsed} />
          {!collapsed && (
            <button onClick={() => setCollapsed(true)} className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100">
              <LuChevronLeft className="h-4 w-4" />
            </button>
          )}
        </div>
        <NavItems collapsed={collapsed} />
        {collapsed && (
          <div className="border-t border-gray-200 p-2">
            <button
              onClick={() => setCollapsed(false)}
              className="flex w-full items-center justify-center rounded-lg p-2 text-gray-400 hover:bg-gray-100"
            >
              <LuChevronRight className="h-4 w-4" />
            </button>
          </div>
        )}
      </aside>
    </>
  )
}