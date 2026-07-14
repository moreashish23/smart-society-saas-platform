import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { notificationsApi } from '@/api/analytics.api'
import { useAuth } from '@/hooks/useAuth'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Pagination } from '@/components/ui/Pagination'
import { PageSpinner } from '@/components/ui/Spinner'
import { formatRelative } from '@/lib/utils'
import { LuCheckCheck, LuInbox } from 'react-icons/lu'
import toast from 'react-hot-toast'

const TYPE_COLOURS: Record<string, string> = {
  COMPLAINT_CREATED:             'bg-blue-100 text-blue-800 border-blue-200',
  COMPLAINT_ASSIGNED:            'bg-purple-100 text-purple-800 border-purple-200',
  COMPLAINT_ACCEPTED:            'bg-indigo-100 text-indigo-800 border-indigo-200',
  COMPLAINT_WORK_STARTED:        'bg-orange-100 text-orange-800 border-orange-200',
  COMPLAINT_WORK_COMPLETED:      'bg-teal-100 text-teal-800 border-teal-200',
  COMPLAINT_PENDING_VERIFICATION:'bg-yellow-100 text-yellow-800 border-yellow-200',
  COMPLAINT_RESOLVED:            'bg-green-100 text-green-800 border-green-200',
  COMPLAINT_REOPENED:            'bg-red-100 text-red-800 border-red-200',
  COMPLAINT_ESCALATED:           'bg-red-100 text-red-800 border-red-200',
  COMPLAINT_CANCELLED:           'bg-gray-100 text-gray-700 border-gray-200',
  NOTICE_PUBLISHED:              'bg-blue-100 text-blue-800 border-blue-200',
  VENDOR_APPROVED:               'bg-green-100 text-green-800 border-green-200',
  GENERAL:                       'bg-gray-100 text-gray-700 border-gray-200',
}

export default function NotificationsPage() {
  const { user } = useAuth()
  const queryClient         = useQueryClient()
  const [page, setPage]     = useState(0)
  const [unreadOnly, setUnreadOnly] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['notifications', 'page', page, unreadOnly],
    queryFn:  () =>
      notificationsApi.list({ page, size: 20, unreadOnly }).then((r) => r.data.data),
    enabled: !!user,
    staleTime: 30_000,
  })

  const { data: countData } = useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn:  () => notificationsApi.getUnreadCount().then((r) => r.data.data),
    enabled:  !!user,
    refetchInterval: 30_000,
  })

  const markReadMutation = useMutation({
    mutationFn: (id: string) => notificationsApi.markRead(id),
    onSuccess:  () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  })

  const markAllReadMutation = useMutation({
    mutationFn: () => notificationsApi.markAllRead(),
    onSuccess:  () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      toast.success('All notifications marked as read')
    },
  })

  const unreadCount = countData?.unreadCount ?? 0

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
          {unreadCount > 0 && (
            <span className="flex h-6 min-w-6 items-center justify-center rounded-full bg-red-500 px-1.5 text-xs font-bold text-white">
              {unreadCount}
            </span>
          )}
        </div>
        <div className="flex items-center gap-2">
          {unreadCount > 0 && (
            <Button
              variant="outline"
              size="sm"
              loading={markAllReadMutation.isPending}
              onClick={() => markAllReadMutation.mutate()}
            >
              <LuCheckCheck className="h-4 w-4" /> Mark all read
            </Button>
          )}
        </div>
      </div>

      {/* Filter toggle */}
      <div className="flex gap-1 rounded-lg bg-gray-100 p-1 w-fit">
        {[false, true].map((val) => (
          <button
            key={String(val)}
            onClick={() => { setUnreadOnly(val); setPage(0) }}
            className={`rounded-md px-4 py-1.5 text-sm font-medium transition-colors ${
              unreadOnly === val
                ? 'bg-white text-gray-900 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            {val ? `Unread (${unreadCount})` : 'All'}
          </button>
        ))}
      </div>

      {/* List */}
      {isLoading ? (
        <PageSpinner />
      ) : !data?.content.length ? (
        <Card className="py-16 text-center">
          <LuInbox className="mx-auto mb-3 h-10 w-10 text-gray-300" />
          <p className="font-medium text-gray-500">
            {unreadOnly ? 'No unread notifications' : 'No notifications yet'}
          </p>
        </Card>
      ) : (
        <div className="space-y-2">
          {data.content.map((n) => (
            <div
              key={n.id}
              onClick={() => { if (!n.read) markReadMutation.mutate(n.id) }}
              className={`flex cursor-pointer items-start gap-4 rounded-xl border p-4 transition-colors hover:bg-gray-50 ${
                !n.read
                  ? 'border-blue-200 bg-blue-50'
                  : 'border-gray-200 bg-white'
              }`}
            >
              {/* Dot indicator */}
              <div className="mt-1.5 flex-shrink-0">
                {!n.read ? (
                  <div className="h-2.5 w-2.5 rounded-full bg-blue-500" />
                ) : (
                  <div className="h-2.5 w-2.5 rounded-full bg-gray-300" />
                )}
              </div>

              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div className="min-w-0 flex-1">
                    <p className={`text-sm font-medium ${!n.read ? 'text-gray-900' : 'text-gray-700'}`}>
                      {n.title}
                    </p>
                    <p className="mt-0.5 text-sm text-gray-600 line-clamp-2">{n.message}</p>
                  </div>
                  <div className="flex flex-shrink-0 flex-col items-end gap-1">
                    <Badge
                      label={n.type.replace(/_/g, ' ')}
                      className={TYPE_COLOURS[n.type] ?? 'bg-gray-100 text-gray-700'}
                    />
                    <span className="text-xs text-gray-400">{formatRelative(n.createdAt)}</span>
                  </div>
                </div>
              </div>
            </div>
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