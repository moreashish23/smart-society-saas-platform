import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { societyApi } from '@/api/society.api'
import { useAuth } from '@/hooks/useAuth'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Modal } from '@/components/ui/Modal'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { PageSpinner } from '@/components/ui/Spinner'
import { getNoticeTypeColour, formatDateTime, formatRelative } from '@/lib/utils'
import {
  LuPlus, LuStickyNote, LuBell, LuArchive,
  LuGlobe, LuTriangleAlert,
} from 'react-icons/lu'
import toast from 'react-hot-toast'
import type { NoticeType } from '@/types'

const TYPE_OPTIONS = [
  { value: 'GENERAL',         label: 'General' },
  { value: 'WATER_SHUTDOWN',  label: 'Water Shutdown' },
  { value: 'MAINTENANCE',     label: 'Maintenance' },
  { value: 'EMERGENCY_ALERT', label: 'Emergency Alert' },
  { value: 'SOCIETY_MEETING', label: 'Society Meeting' },
]

const TYPE_ICONS: Record<string, React.ReactNode> = {
  WATER_SHUTDOWN:  <LuBell className="h-4 w-4 text-blue-600" />,
  MAINTENANCE:     <LuTriangleAlert className="h-4 w-4 text-orange-600" />,
  EMERGENCY_ALERT: <LuTriangleAlert className="h-4 w-4 text-red-600" />,
  SOCIETY_MEETING: <LuGlobe className="h-4 w-4 text-purple-600" />,
  GENERAL:         <LuStickyNote className="h-4 w-4 text-gray-600" />,
}

export default function NoticesPage() {
  const { isManagerOrAbove, societyId } = useAuth()
  const queryClient = useQueryClient()
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [activeTab, setActiveTab]             = useState<'active' | 'all'>('active')

  // Active notices (all members)
  const { data: activeNotices, isLoading: loadingActive } = useQuery({
    queryKey: ['notices', 'active', societyId],
    queryFn:  () => societyApi.getActiveNotices(societyId!).then((r) => r.data.data),
    enabled:  !!societyId && activeTab === 'active',
  })

  // All notices (managers)
  const { data: allNotices, isLoading: loadingAll } = useQuery({
    queryKey: ['notices', 'all', societyId],
    queryFn:  () => societyApi.getNotices(societyId!, { size: 50 }).then((r) => r.data.data),
    enabled:  !!societyId && isManagerOrAbove && activeTab === 'all',
  })

  const publishMutation = useMutation({
    mutationFn: (noticeId: string) => societyApi.publishNotice(societyId!, noticeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notices'] })
      toast.success('Notice published')
    },
    onError: () => toast.error('Failed to publish notice'),
  })

  const archiveMutation = useMutation({
    mutationFn: (noticeId: string) => societyApi.archiveNotice(societyId!, noticeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notices'] })
      toast.success('Notice archived')
    },
    onError: () => toast.error('Failed to archive notice'),
  })

  const notices = activeTab === 'active'
    ? (activeNotices ?? [])
    : (allNotices?.content ?? [])

  const isLoading = activeTab === 'active' ? loadingActive : loadingAll

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Notice Board</h1>
          <p className="mt-1 text-sm text-gray-500">
            Society announcements and alerts
          </p>
        </div>
        {isManagerOrAbove && (
          <Button onClick={() => setShowCreateModal(true)}>
            <LuPlus className="h-4 w-4" /> Post Notice
          </Button>
        )}
      </div>

      {/* Tabs — managers see all/active toggle */}
      {isManagerOrAbove && (
        <div className="flex gap-1 rounded-lg bg-gray-100 p-1 w-fit">
          {(['active', 'all'] as const).map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`rounded-md px-4 py-1.5 text-sm font-medium capitalize transition-colors ${
                activeTab === tab
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              {tab === 'active' ? 'Active' : 'All Notices'}
            </button>
          ))}
        </div>
      )}

      {/* Notices list */}
      {isLoading ? (
        <PageSpinner />
      ) : notices.length === 0 ? (
        <Card className="py-16 text-center">
          <LuStickyNote className="mx-auto mb-3 h-10 w-10 text-gray-300" />
          <p className="font-medium text-gray-500">No notices to display</p>
        </Card>
      ) : (
        <div className="space-y-4">
          {notices.map((notice) => (
            <Card key={notice.id} className={notice.priority ? 'border-l-4 border-l-red-500' : ''}>
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-start gap-3 min-w-0 flex-1">
                  <div className="mt-0.5 flex-shrink-0">
                    {TYPE_ICONS[notice.noticeType] ?? <LuStickyNote className="h-4 w-4 text-gray-500" />}
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="font-semibold text-gray-900">{notice.title}</h3>
                      {notice.priority && (
                        <span className="rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700">
                          📌 Pinned
                        </span>
                      )}
                      <Badge
                        label={notice.noticeType.replace(/_/g, ' ')}
                        className={getNoticeTypeColour(notice.noticeType)}
                      />
                      {isManagerOrAbove && (
                        <Badge
                          label={notice.status}
                          className={
                            notice.status === 'PUBLISHED'
                              ? 'bg-green-100 text-green-800 border-green-200'
                              : notice.status === 'DRAFT'
                              ? 'bg-yellow-100 text-yellow-800 border-yellow-200'
                              : 'bg-gray-100 text-gray-700 border-gray-200'
                          }
                        />
                      )}
                    </div>

                    <p className="mt-2 text-sm text-gray-700 leading-relaxed whitespace-pre-line">
                      {notice.content}
                    </p>

                    <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500">
                      {notice.publishedAt && (
                        <span>Published {formatRelative(notice.publishedAt)}</span>
                      )}
                      {notice.expiresAt && (
                        <span>Expires {formatDateTime(notice.expiresAt)}</span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Manager actions */}
                {isManagerOrAbove && (
                  <div className="flex flex-shrink-0 flex-col gap-2">
                    {notice.status === 'DRAFT' && (
                      <Button
                        size="sm"
                        loading={publishMutation.isPending}
                        onClick={() => publishMutation.mutate(notice.id)}
                      >
                        <LuGlobe className="h-3.5 w-3.5" /> Publish
                      </Button>
                    )}
                    {notice.status === 'PUBLISHED' && (
                      <Button
                        size="sm"
                        variant="secondary"
                        loading={archiveMutation.isPending}
                        onClick={() => archiveMutation.mutate(notice.id)}
                      >
                        <LuArchive className="h-3.5 w-3.5" /> Archive
                      </Button>
                    )}
                  </div>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Create notice modal */}
      <CreateNoticeModal
        open={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        societyId={societyId!}
        onSuccess={() => {
          setShowCreateModal(false)
          queryClient.invalidateQueries({ queryKey: ['notices'] })
        }}
      />
    </div>
  )
}

// ── Create Notice Modal ───────────────────────────────────────────────────────

function CreateNoticeModal({ open, onClose, societyId, onSuccess }: {
  open: boolean; onClose: () => void; societyId: string; onSuccess: () => void
}) {
  const [form, setForm] = useState({
    title: '', content: '', noticeType: 'GENERAL' as NoticeType,
    priority: false, publishImmediately: true, expiresAt: '',
  })

  const { mutate, isPending } = useMutation({
    mutationFn: () => societyApi.createNotice(societyId, {
      title:              form.title,
      content:            form.content,
      noticeType:         form.noticeType,
      priority:           form.priority,
      publishImmediately: form.publishImmediately,
      expiresAt:          form.expiresAt || undefined,
    }),
    onSuccess: () => { toast.success('Notice created'); onSuccess() },
    onError:   () => toast.error('Failed to create notice'),
  })

  const set = (k: string) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) =>
      setForm((f) => ({ ...f, [k]: e.target.value }))

  return (
    <Modal
      open={open} onClose={onClose} title="Post New Notice" size="lg"
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button loading={isPending} onClick={() => mutate()}>
            {form.publishImmediately ? 'Publish Now' : 'Save as Draft'}
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        <Input
          label="Title" required
          value={form.title} onChange={set('title')}
          placeholder="Notice title"
        />
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-gray-700">
            Content <span className="text-red-500">*</span>
          </label>
          <textarea
            rows={5}
            value={form.content}
            onChange={set('content')}
            placeholder="Write the notice content here…"
            className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <Select
            label="Notice Type" required
            options={TYPE_OPTIONS}
            value={form.noticeType}
            onChange={set('noticeType')}
          />
          <Input
            label="Expires At (optional)"
            type="datetime-local"
            value={form.expiresAt}
            onChange={set('expiresAt')}
          />
        </div>
        <div className="flex items-center gap-6">
          <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
            <input
              type="checkbox"
              checked={form.priority}
              onChange={(e) => setForm((f) => ({ ...f, priority: e.target.checked }))}
              className="h-4 w-4 rounded border-gray-300 text-blue-600"
            />
            Pin this notice
          </label>
          <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
            <input
              type="checkbox"
              checked={form.publishImmediately}
              onChange={(e) => setForm((f) => ({ ...f, publishImmediately: e.target.checked }))}
              className="h-4 w-4 rounded border-gray-300 text-blue-600"
            />
            Publish immediately
          </label>
        </div>
      </div>
    </Modal>
  )
}