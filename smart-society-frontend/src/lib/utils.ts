import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'
import { format, formatDistanceToNow, parseISO, isAfter } from 'date-fns'
import type {
  ComplaintPriority,
  ComplaintStatus,
  VendorStatus,
  NoticeType,
  UserRole,
} from '@/types'

// ─── Tailwind class merger ────────────────────────────────────────────────────

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// ─── Date formatting ──────────────────────────────────────────────────────────

export function formatDate(dateStr: string): string {
  try {
    return format(parseISO(dateStr), 'dd MMM yyyy')
  } catch {
    return dateStr
  }
}

export function formatDateTime(dateStr: string): string {
  try {
    return format(parseISO(dateStr), 'dd MMM yyyy, hh:mm a')
  } catch {
    return dateStr
  }
}

export function formatRelative(dateStr: string): string {
  try {
    return formatDistanceToNow(parseISO(dateStr), { addSuffix: true })
  } catch {
    return dateStr
  }
}

export function isSlaBreached(slaDeadline: string): boolean {
  try {
    return isAfter(new Date(), parseISO(slaDeadline))
  } catch {
    return false
  }
}

// ─── Badge colour helpers ─────────────────────────────────────────────────────

export function getPriorityColour(priority: ComplaintPriority): string {
  return {
    CRITICAL: 'bg-red-100 text-red-800 border-red-200',
    HIGH:     'bg-orange-100 text-orange-800 border-orange-200',
    MEDIUM:   'bg-yellow-100 text-yellow-800 border-yellow-200',
    LOW:      'bg-green-100 text-green-800 border-green-200',
  }[priority] ?? 'bg-gray-100 text-gray-800'
}

export function getStatusColour(status: ComplaintStatus): string {
  return {
    OPEN:                 'bg-blue-100 text-blue-800 border-blue-200',
    ASSIGNED:             'bg-purple-100 text-purple-800 border-purple-200',
    IN_PROGRESS:          'bg-indigo-100 text-indigo-800 border-indigo-200',
    PENDING_VERIFICATION: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    CLOSED:               'bg-green-100 text-green-800 border-green-200',
    CANCELLED:            'bg-gray-100 text-gray-800 border-gray-200',
    REOPENED:             'bg-red-100 text-red-800 border-red-200',
  }[status] ?? 'bg-gray-100 text-gray-800'
}

export function getVendorStatusColour(status: VendorStatus): string {
  return {
    ACTIVE:           'bg-green-100 text-green-800',
    INACTIVE:         'bg-gray-100 text-gray-800',
    SUSPENDED:        'bg-red-100 text-red-800',
    PENDING_APPROVAL: 'bg-yellow-100 text-yellow-800',
  }[status] ?? 'bg-gray-100 text-gray-800'
}

export function getNoticeTypeColour(type: NoticeType): string {
  return {
    WATER_SHUTDOWN:  'bg-blue-100 text-blue-800',
    MAINTENANCE:     'bg-orange-100 text-orange-800',
    EMERGENCY_ALERT: 'bg-red-100 text-red-800',
    SOCIETY_MEETING: 'bg-purple-100 text-purple-800',
    GENERAL:         'bg-gray-100 text-gray-800',
  }[type] ?? 'bg-gray-100 text-gray-800'
}

// ─── Label helpers ────────────────────────────────────────────────────────────

export function formatCategory(cat: string): string {
  return cat.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
}

export function formatRole(role: UserRole): string {
  return {
    SUPER_ADMIN:       'Super Admin',
    SOCIETY_MANAGER:   'Society Manager',
    COMMITTEE_MEMBER:  'Committee Member',
    RESIDENT:          'Resident',
    MAINTENANCE_STAFF: 'Maintenance Staff',
    VENDOR:            'Vendor',
  }[role] ?? role
}

// ─── Number helpers ───────────────────────────────────────────────────────────

export function formatHours(hours: number): string {
  if (hours < 1) return `${Math.round(hours * 60)}m`
  if (hours < 24) return `${hours.toFixed(1)}h`
  return `${(hours / 24).toFixed(1)}d`
}

export function formatPercent(value: number): string {
  return `${value.toFixed(1)}%`
}

// ─── Local storage helpers ────────────────────────────────────────────────────

export const storage = {
  get: (key: string) => {
    try { return JSON.parse(localStorage.getItem(key) ?? 'null') }
    catch { return null }
  },
  set: (key: string, value: unknown) => {
    try { localStorage.setItem(key, JSON.stringify(value)) }
    catch { /* quota exceeded */ }
  },
  remove: (key: string) => { localStorage.removeItem(key) },
  clear: () => { localStorage.clear() },
}