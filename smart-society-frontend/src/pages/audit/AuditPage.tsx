import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { auditApi } from '@/api/analytics.api'
import { useAuth } from '@/hooks/useAuth'
import { Card } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Select } from '@/components/ui/Select'
import { Pagination } from '@/components/ui/Pagination'
import { PageSpinner } from '@/components/ui/Spinner'
import { formatDateTime } from '@/lib/utils'
import { LuScrollText } from 'react-icons/lu'
import type { AuditAction } from '@/types'

const ACTION_OPTIONS = [
  { value: '',                  label: 'All Actions' },
  { value: 'LOGIN',             label: 'Login' },
  { value: 'LOGOUT',            label: 'Logout' },
  { value: 'REGISTER',          label: 'Register' },
  { value: 'COMPLAINT_CREATE',  label: 'Complaint Created' },
  { value: 'COMPLAINT_ASSIGN',  label: 'Complaint Assigned' },
  { value: 'COMPLAINT_RESOLVE', label: 'Complaint Resolved' },
  { value: 'COMPLAINT_CLOSE',   label: 'Complaint Closed' },
  { value: 'COMPLAINT_REOPEN',  label: 'Complaint Reopened' },
  { value: 'SOCIETY_CREATE',    label: 'Society Created' },
  { value: 'VENDOR_REGISTER',   label: 'Vendor Registered' },
  { value: 'VENDOR_APPROVE',    label: 'Vendor Approved' },
  { value: 'NOTICE_CREATE',     label: 'Notice Created' },
  { value: 'NOTICE_PUBLISH',    label: 'Notice Published' },
  { value: 'PASSWORD_CHANGE',   label: 'Password Changed' },
  { value: 'PASSWORD_RESET',    label: 'Password Reset' },
]

const ENTITY_OPTIONS = [
  { value: '',          label: 'All Entities' },
  { value: 'USER',      label: 'User' },
  { value: 'COMPLAINT', label: 'Complaint' },
  { value: 'SOCIETY',   label: 'Society' },
  { value: 'VENDOR',    label: 'Vendor' },
  { value: 'NOTICE',    label: 'Notice' },
]

const ACTION_COLOUR: Record<string, string> = {
  LOGIN:             'bg-green-100 text-green-800 border-green-200',
  LOGOUT:            'bg-gray-100 text-gray-700 border-gray-200',
  LOGOUT_ALL:        'bg-gray-100 text-gray-700 border-gray-200',
  REGISTER:          'bg-blue-100 text-blue-800 border-blue-200',
  COMPLAINT_CREATE:  'bg-blue-100 text-blue-800 border-blue-200',
  COMPLAINT_ASSIGN:  'bg-purple-100 text-purple-800 border-purple-200',
  COMPLAINT_RESOLVE: 'bg-teal-100 text-teal-800 border-teal-200',
  COMPLAINT_CLOSE:   'bg-green-100 text-green-800 border-green-200',
  COMPLAINT_REOPEN:  'bg-red-100 text-red-800 border-red-200',
  COMPLAINT_ESCALATE:'bg-red-100 text-red-800 border-red-200',
  VENDOR_APPROVE:    'bg-green-100 text-green-800 border-green-200',
  VENDOR_SUSPEND:    'bg-orange-100 text-orange-800 border-orange-200',
  NOTICE_PUBLISH:    'bg-indigo-100 text-indigo-800 border-indigo-200',
  PASSWORD_CHANGE:   'bg-yellow-100 text-yellow-800 border-yellow-200',
  FAILED_LOGIN:      'bg-red-100 text-red-800 border-red-200',
}

export default function AuditPage() {
  const { isManagerOrAbove } = useAuth()
  const [page,       setPage]       = useState(0)
  const [action,     setAction]     = useState<AuditAction | ''>('')
  const [entityType, setEntityType] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['audit', page, action, entityType],
    queryFn:  () => auditApi.list({
      page, size: 50,
      action:     action     || undefined,
      entityType: entityType || undefined,
    }).then((r) => r.data.data),
    enabled:  isManagerOrAbove,
    staleTime: 60_000,
  })

  if (!isManagerOrAbove) {
    return (
      <div className="flex h-64 items-center justify-center">
        <p className="text-gray-500">Audit logs are visible to managers and above.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Audit Logs</h1>
        <p className="mt-1 text-sm text-gray-500">
          Immutable record of all platform actions
        </p>
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          <Select
            options={ACTION_OPTIONS}
            value={action}
            onChange={(e) => { setAction(e.target.value as AuditAction | ''); setPage(0) }}
          />
          <Select
            options={ENTITY_OPTIONS}
            value={entityType}
            onChange={(e) => { setEntityType(e.target.value); setPage(0) }}
          />
          <div className="flex items-center gap-2 text-sm text-gray-500">
            {data && (
              <span className="flex items-center gap-1">
                <LuScrollText className="h-4 w-4" />
                {data.totalElements.toLocaleString()} log entries
              </span>
            )}
          </div>
        </div>
      </Card>

      {/* Table */}
      {isLoading ? (
        <PageSpinner />
      ) : !data?.content.length ? (
        <Card className="py-16 text-center">
          <LuScrollText className="mx-auto mb-3 h-10 w-10 text-gray-300" />
          <p className="text-gray-500">No audit logs found</p>
        </Card>
      ) : (
        <Card padding={false}>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b border-gray-200 bg-gray-50">
                <tr>
                  {['Action', 'Entity', 'User ID', 'Description', 'IP Address', 'Timestamp'].map((h) => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data.content.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-4 py-3">
                      <Badge
                        label={log.action.replace(/_/g, ' ')}
                        className={ACTION_COLOUR[log.action] ?? 'bg-gray-100 text-gray-700 border-gray-200'}
                      />
                    </td>
                    <td className="px-4 py-3 text-gray-700">
                      {log.entityType ? (
                        <div>
                          <p className="font-medium">{log.entityType}</p>
                          {log.entityId && (
                            <p className="font-mono text-xs text-gray-400">
                              {log.entityId.slice(0, 8)}…
                            </p>
                          )}
                        </div>
                      ) : (
                        <span className="text-gray-400">—</span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      {log.userId ? (
                        <span className="font-mono text-xs text-gray-600">
                          {log.userId.slice(0, 8)}…
                        </span>
                      ) : (
                        <span className="text-gray-400">—</span>
                      )}
                    </td>
                    <td className="max-w-xs px-4 py-3 text-gray-700">
                      <p className="truncate">{log.description ?? '—'}</p>
                    </td>
                    <td className="px-4 py-3 font-mono text-xs text-gray-500">
                      {log.ipAddress ?? '—'}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-xs text-gray-500">
                      {formatDateTime(log.createdAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {data && (
        <Pagination
          page={data.page}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          size={data.size}
          onPageChange={setPage}
        />
      )}
    </div>
  )
}