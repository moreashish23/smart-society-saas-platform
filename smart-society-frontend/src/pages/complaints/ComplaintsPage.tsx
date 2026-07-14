import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { complaintsApi } from '@/api/complaints.api'
import { useAuth } from '@/hooks/useAuth'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Select } from '@/components/ui/Select'
import { Pagination } from '@/components/ui/Pagination'
import { PageSpinner } from '@/components/ui/Spinner'
import {
  getPriorityColour, getStatusColour,
  formatRelative, formatCategory,
} from '@/lib/utils'
import {
  LuPlus, LuSearch, LuTriangleAlert,
  LuClock, LuChevronRight, LuFilter,
} from 'react-icons/lu'
import type { ComplaintStatus, ComplaintCategory, ComplaintPriority } from '@/types'

const STATUS_OPTIONS = [
  { value: '',                     label: 'All Statuses' },
  { value: 'OPEN',                 label: 'Open' },
  { value: 'ASSIGNED',             label: 'Assigned' },
  { value: 'IN_PROGRESS',          label: 'In Progress' },
  { value: 'PENDING_VERIFICATION', label: 'Pending Verification' },
  { value: 'CLOSED',               label: 'Closed' },
  { value: 'REOPENED',             label: 'Reopened' },
]

const CATEGORY_OPTIONS = [
  { value: '', label: 'All Categories' },
  { value: 'WATER_LEAKAGE', label: 'Water Leakage' },
  { value: 'PLUMBING',      label: 'Plumbing' },
  { value: 'ELECTRICITY',   label: 'Electricity' },
  { value: 'LIFT_ISSUE',    label: 'Lift Issue' },
  { value: 'PARKING',       label: 'Parking' },
  { value: 'SECURITY',      label: 'Security' },
  { value: 'HOUSEKEEPING',  label: 'Housekeeping' },
  { value: 'INTERNET_ISSUE','label': 'Internet Issue' },
  { value: 'NOISE_COMPLAINT','label': 'Noise Complaint' },
  { value: 'OTHER',         label: 'Other' },
]

const PRIORITY_OPTIONS = [
  { value: '',         label: 'All Priorities' },
  { value: 'CRITICAL', label: 'Critical' },
  { value: 'HIGH',     label: 'High' },
  { value: 'MEDIUM',   label: 'Medium' },
  { value: 'LOW',      label: 'Low' },
]

export default function ComplaintsPage() {
  const { isManagerOrAbove, isResident, isVendor, isStaff } = useAuth()

  const [page,     setPage]     = useState(0)
  const [keyword,  setKeyword]  = useState('')
  const [status,   setStatus]   = useState<ComplaintStatus | ''>('')
  const [category, setCategory] = useState<ComplaintCategory | ''>('')
  const [priority, setPriority] = useState<ComplaintPriority | ''>('')

  const queryFn = () => {
    if (isManagerOrAbove) {
      return complaintsApi.list({
        page, size: 20,
        keyword: keyword || undefined,
        status:   status   || undefined,
        category: category || undefined,
        priority: priority || undefined,
      }).then((r) => r.data.data)
    }
    if (isVendor || isStaff) {
      return complaintsApi.getAssigned({ page, size: 20 }).then((r) => r.data.data)
    }
    return complaintsApi.getMy({ page, size: 20 }).then((r) => r.data.data)
  }

  const { data, isLoading } = useQuery({
    queryKey: ['complaints', page, keyword, status, category, priority],
    queryFn,
    staleTime: 30_000,
  })

  const title = isManagerOrAbove
    ? 'All Complaints'
    : (isVendor || isStaff) ? 'Assigned to Me' : 'My Complaints'

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
          {data && (
            <p className="mt-1 text-sm text-gray-500">
              {data.totalElements} complaint{data.totalElements !== 1 ? 's' : ''} found
            </p>
          )}
        </div>
        {isResident && (
          <Link to="/complaints/new">
            <Button>
              <LuPlus className="h-4 w-4" /> New Complaint
            </Button>
          </Link>
        )}
      </div>

      {/* Filters — managers only */}
      {isManagerOrAbove && (
        <Card className="p-4">
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-5">
            <div className="relative lg:col-span-2">
              <LuSearch className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="Search complaints…"
                value={keyword}
                onChange={(e) => { setKeyword(e.target.value); setPage(0) }}
                className="w-full rounded-lg border border-gray-300 py-2 pl-9 pr-3 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
            <Select
              options={STATUS_OPTIONS}
              value={status}
              onChange={(e) => { setStatus(e.target.value as ComplaintStatus | ''); setPage(0) }}
            />
            <Select
              options={CATEGORY_OPTIONS}
              value={category}
              onChange={(e) => { setCategory(e.target.value as ComplaintCategory | ''); setPage(0) }}
            />
            <Select
              options={PRIORITY_OPTIONS}
              value={priority}
              onChange={(e) => { setPriority(e.target.value as ComplaintPriority | ''); setPage(0) }}
            />
          </div>
        </Card>
      )}

      {/* List */}
      {isLoading ? (
        <PageSpinner />
      ) : !data?.content.length ? (
        <Card className="py-16 text-center">
          <LuFilter className="mx-auto mb-3 h-10 w-10 text-gray-300" />
          <p className="font-medium text-gray-500">No complaints found</p>
          {isResident && (
            <Link to="/complaints/new" className="mt-4 inline-block">
              <Button size="sm"><LuPlus className="h-4 w-4" /> Raise a Complaint</Button>
            </Link>
          )}
        </Card>
      ) : (
        <div className="space-y-3">
          {data.content.map((c) => (
            <Link key={c.id} to={`/complaints/${c.id}`}>
              <Card padding={false} className="flex items-center gap-4 p-4 transition-shadow hover:shadow-md">
                {/* Priority indicator */}
                <div className={`h-full w-1.5 flex-shrink-0 rounded-full self-stretch ${
                  c.priority === 'CRITICAL' ? 'bg-red-500' :
                  c.priority === 'HIGH'     ? 'bg-orange-500' :
                  c.priority === 'MEDIUM'   ? 'bg-yellow-500' : 'bg-green-500'
                }`} />

                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <h3 className="truncate font-medium text-gray-900">{c.title}</h3>
                    {c.slaBreached && (
                      <span className="flex items-center gap-1 rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700">
                        <LuTriangleAlert className="h-3 w-3" /> SLA Breached
                      </span>
                    )}
                  </div>
                  <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-gray-500">
                    <Badge label={c.category} className={getPriorityColour(c.priority)} />
                    <span>{formatCategory(c.category)}</span>
                    {c.location && <span>📍 {c.location}</span>}
                    <span className="flex items-center gap-1">
                      <LuClock className="h-3 w-3" /> {formatRelative(c.createdAt)}
                    </span>
                  </div>
                </div>

                <div className="flex flex-shrink-0 flex-col items-end gap-2">
                  <Badge label={c.status} className={getStatusColour(c.status)} />
                  {c.escalationLevel > 0 && (
                    <span className="text-xs text-red-600 font-medium">
                      Escalated L{c.escalationLevel}
                    </span>
                  )}
                </div>

                <LuChevronRight className="h-5 w-5 flex-shrink-0 text-gray-400" />
              </Card>
            </Link>
          ))}
        </div>
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