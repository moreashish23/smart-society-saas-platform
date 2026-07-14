import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { complaintsApi } from '@/api/complaints.api'
import { vendorsApi } from '@/api/vendors.api'
import { useAuth } from '@/hooks/useAuth'
import { Card, CardHeader, CardTitle } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Modal } from '@/components/ui/Modal'
import { PageSpinner } from '@/components/ui/Spinner'
import {
  getPriorityColour, getStatusColour,
  formatDateTime, formatRelative, formatCategory,
} from '@/lib/utils'
import {
  LuArrowLeft, LuTriangleAlert, LuClock,
  LuUser, LuMapPin, LuMessageSquare,
  LuCircleCheck, LuRefreshCw, LuBan,
} from 'react-icons/lu'
import toast from 'react-hot-toast'

export default function ComplaintDetailPage() {
  const { id }            = useParams<{ id: string }>()
  const navigate          = useNavigate()
  const queryClient       = useQueryClient()
  const {
    isManagerOrAbove, isManager, isResident,
    isVendor, isStaff, user,
  } = useAuth()

  const [showAssignModal,   setShowAssignModal]   = useState(false)
  const [showResolveModal,  setShowResolveModal]  = useState(false)
  const [showVerifyModal,   setShowVerifyModal]   = useState(false)
  const [comment,           setComment]           = useState('')
  const [assignVendorId,    setAssignVendorId]    = useState('')
  const [assignNote,        setAssignNote]        = useState('')
  const [resolutionNote,    setResolutionNote]    = useState('')

  // ── Fetch complaint ───────────────────────────────────────────────────────

  const { data: complaint, isLoading } = useQuery({
    queryKey: ['complaints', id],
    queryFn:  () => complaintsApi.getById(id!).then((r) => r.data.data),
    enabled:  !!id,
  })

  // ── Fetch vendors (for assign modal) ─────────────────────────────────────

  const { data: vendorsData } = useQuery({
    queryKey: ['vendors', 'active'],
    queryFn:  () => vendorsApi.list({ status: 'ACTIVE', size: 100 }).then((r) => r.data.data),
    enabled:  showAssignModal,
  })

  // ── Mutations ─────────────────────────────────────────────────────────────

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['complaints', id] })

  const assignMutation = useMutation({
    mutationFn: () => complaintsApi.assign(id!, { assignedToId: assignVendorId, note: assignNote }),
    onSuccess: () => { invalidate(); setShowAssignModal(false); toast.success('Complaint assigned') },
    onError:   () => toast.error('Failed to assign complaint'),
  })

  const acceptMutation      = useMutation({ mutationFn: () => complaintsApi.accept(id!),      onSuccess: () => { invalidate(); toast.success('Complaint accepted') } })
  const startWorkMutation   = useMutation({ mutationFn: () => complaintsApi.startWork(id!),   onSuccess: () => { invalidate(); toast.success('Work started') } })
  const completeWorkMutation = useMutation({ mutationFn: () => complaintsApi.completeWork(id!), onSuccess: () => { invalidate(); toast.success('Work completed') } })

  const resolveMutation = useMutation({
    mutationFn: () => complaintsApi.resolve(id!, { resolutionNote }),
    onSuccess: () => { invalidate(); setShowResolveModal(false); toast.success('Marked as resolved — awaiting resident confirmation') },
    onError:   () => toast.error('Failed to resolve'),
  })

  const verifyMutation = useMutation({
    mutationFn: (resolved: boolean) => complaintsApi.verify(id!, resolved),
    onSuccess: (_, resolved) => {
      invalidate()
      setShowVerifyModal(false)
      toast.success(resolved ? 'Complaint closed. Thank you!' : 'Complaint reopened')
    },
    onError: () => toast.error('Failed to verify'),
  })

  const cancelMutation = useMutation({
    mutationFn: () => complaintsApi.cancel(id!),
    onSuccess: () => { invalidate(); toast.success('Complaint cancelled') },
    onError:   () => toast.error('Failed to cancel'),
  })

  const commentMutation = useMutation({
    mutationFn: () => complaintsApi.addComment(id!, { content: comment }),
    onSuccess: () => { invalidate(); setComment(''); toast.success('Comment added') },
    onError:   () => toast.error('Failed to add comment'),
  })

  if (isLoading) return <PageSpinner />
  if (!complaint) return <p className="p-8 text-center text-gray-500">Complaint not found</p>

  const c = complaint
  const isOwner       = c.residentId === user?.id
  const isAssignedTo  = c.assignedToId === user?.id
  const canAssign     = isManagerOrAbove && ['OPEN', 'REOPENED'].includes(c.status)
  const canResolve    = isManager && ['ASSIGNED', 'IN_PROGRESS'].includes(c.status)
  const canVerify     = isResident && isOwner && c.status === 'PENDING_VERIFICATION'
  const canAccept     = (isVendor || isStaff) && isAssignedTo && c.status === 'ASSIGNED'
  const canStartWork  = (isVendor || isStaff) && isAssignedTo && c.status === 'IN_PROGRESS'
  const canComplete   = (isVendor || isStaff) && isAssignedTo && c.status === 'IN_PROGRESS'
  const canCancel     = (isOwner || isManagerOrAbove) && !['CLOSED', 'CANCELLED'].includes(c.status)

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      {/* Back */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-900"
      >
        <LuArrowLeft className="h-4 w-4" /> Back to complaints
      </button>

      {/* Header card */}
      <Card>
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-xl font-bold text-gray-900">{c.title}</h1>
              {c.slaBreached && (
                <span className="flex items-center gap-1 rounded-full bg-red-100 px-2 py-0.5 text-xs font-semibold text-red-700">
                  <LuTriangleAlert className="h-3 w-3" /> SLA Breached
                </span>
              )}
            </div>

            <div className="mt-2 flex flex-wrap gap-2">
              <Badge label={c.status}   className={getStatusColour(c.status)} />
              <Badge label={c.priority} className={getPriorityColour(c.priority)} />
              <Badge label={c.category} className="bg-gray-100 text-gray-700 border-gray-200" />
              {c.escalationLevel > 0 && (
                <Badge
                  label={`Escalated L${c.escalationLevel}`}
                  className="bg-red-100 text-red-800 border-red-200"
                />
              )}
            </div>

            <p className="mt-3 text-sm text-gray-700 leading-relaxed">{c.description}</p>

            <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500">
              {c.location && (
                <span className="flex items-center gap-1">
                  <LuMapPin className="h-3 w-3" /> {c.location}
                </span>
              )}
              <span className="flex items-center gap-1">
                <LuClock className="h-3 w-3" />
                SLA deadline: {formatDateTime(c.slaDeadline)}
              </span>
              <span className="flex items-center gap-1">
                <LuUser className="h-3 w-3" />
                Submitted {formatRelative(c.createdAt)}
              </span>
              {c.reopenCount > 0 && (
                <span className="text-orange-600 font-medium">
                  Reopened {c.reopenCount}×
                </span>
              )}
            </div>
          </div>

          {/* Actions */}
          <div className="flex flex-shrink-0 flex-col gap-2">
            {canAssign && (
              <Button size="sm" onClick={() => setShowAssignModal(true)}>
                Assign
              </Button>
            )}
            {canAccept && (
              <Button size="sm" onClick={() => acceptMutation.mutate()} loading={acceptMutation.isPending}>
                Accept Job
              </Button>
            )}
            {canStartWork && (
              <Button size="sm" variant="secondary" onClick={() => startWorkMutation.mutate()} loading={startWorkMutation.isPending}>
                Start Work
              </Button>
            )}
            {canComplete && (
              <Button size="sm" variant="secondary" onClick={() => completeWorkMutation.mutate()} loading={completeWorkMutation.isPending}>
                Complete Work
              </Button>
            )}
            {canResolve && (
              <Button size="sm" onClick={() => setShowResolveModal(true)}>
                <LuCircleCheck className="h-4 w-4" /> Mark Resolved
              </Button>
            )}
            {canVerify && (
              <Button
                size="sm"
                className="bg-yellow-500 hover:bg-yellow-600 text-white"
                onClick={() => setShowVerifyModal(true)}
              >
                Verify Resolution
              </Button>
            )}
            {canCancel && (
              <Button
                size="sm"
                variant="danger"
                onClick={() => { if (confirm('Cancel this complaint?')) cancelMutation.mutate() }}
                loading={cancelMutation.isPending}
              >
                <LuBan className="h-4 w-4" /> Cancel
              </Button>
            )}
          </div>
        </div>
      </Card>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
        {/* Timeline — left 3 cols */}
        <div className="space-y-4 lg:col-span-3">
          <Card>
            <CardHeader>
              <CardTitle>Timeline</CardTitle>
            </CardHeader>
            <ol className="relative space-y-4 border-l-2 border-gray-200 pl-5">
              {c.timeline.map((t) => (
                <li key={t.id} className="relative">
                  <div className="absolute -left-[23px] flex h-4 w-4 items-center justify-center rounded-full border-2 border-blue-500 bg-white" />
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {t.action.replace(/_/g, ' ')}
                    </p>
                    {t.note && (
                      <p className="mt-0.5 text-xs text-gray-600">{t.note}</p>
                    )}
                    <p className="mt-0.5 text-xs text-gray-400">{formatDateTime(t.createdAt)}</p>
                  </div>
                </li>
              ))}
            </ol>
          </Card>

          {/* Comments */}
          <Card>
            <CardHeader>
              <CardTitle>Comments ({c.comments.length})</CardTitle>
            </CardHeader>

            {c.comments.length === 0 ? (
              <p className="text-sm text-gray-500">No comments yet.</p>
            ) : (
              <div className="space-y-4">
                {c.comments.map((cm) => (
                  <div key={cm.id} className="flex gap-3">
                    <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-xs font-semibold text-blue-700">
                      {cm.authorRole?.[0] ?? 'U'}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-medium text-gray-700">
                          {cm.authorRole?.replace(/_/g, ' ')}
                        </span>
                        <span className="text-xs text-gray-400">{formatRelative(cm.createdAt)}</span>
                      </div>
                      <p className="mt-0.5 text-sm text-gray-800">{cm.content}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Add comment */}
            {!['CLOSED', 'CANCELLED'].includes(c.status) && (
              <div className="mt-4 flex gap-2">
                <textarea
                  rows={2}
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Add a comment…"
                  className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                />
                <Button
                  size="sm"
                  disabled={!comment.trim()}
                  loading={commentMutation.isPending}
                  onClick={() => commentMutation.mutate()}
                >
                  <LuMessageSquare className="h-4 w-4" />
                </Button>
              </div>
            )}
          </Card>
        </div>

        {/* Sidebar — right 2 cols */}
        <div className="space-y-4 lg:col-span-2">
          <Card>
            <CardTitle className="mb-3">Details</CardTitle>
            <dl className="space-y-2 text-sm">
              <div className="flex justify-between">
                <dt className="text-gray-500">Status</dt>
                <dd><Badge label={c.status} className={getStatusColour(c.status)} /></dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-gray-500">Priority</dt>
                <dd><Badge label={c.priority} className={getPriorityColour(c.priority)} /></dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-gray-500">Category</dt>
                <dd className="text-gray-900">{formatCategory(c.category)}</dd>
              </div>
              {c.assignedToId && (
                <div className="flex justify-between">
                  <dt className="text-gray-500">Assigned to</dt>
                  <dd className="text-gray-900 text-xs font-mono">{c.assignedToId.slice(0, 8)}…</dd>
                </div>
              )}
              {c.resolutionNote && (
                <div className="pt-2 border-t border-gray-100">
                  <dt className="text-gray-500 mb-1">Resolution Note</dt>
                  <dd className="text-gray-800 text-xs leading-relaxed">{c.resolutionNote}</dd>
                </div>
              )}
            </dl>
          </Card>
        </div>
      </div>

      {/* ── Assign Modal ─────────────────────────────────────────────────── */}
      <Modal
        open={showAssignModal}
        onClose={() => setShowAssignModal(false)}
        title="Assign Complaint"
        footer={
          <>
            <Button variant="outline" onClick={() => setShowAssignModal(false)}>Cancel</Button>
            <Button
              disabled={!assignVendorId}
              loading={assignMutation.isPending}
              onClick={() => assignMutation.mutate()}
            >
              Assign
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Select Vendor / Staff <span className="text-red-500">*</span>
            </label>
            <select
              value={assignVendorId}
              onChange={(e) => setAssignVendorId(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Choose vendor or staff…</option>
              {vendorsData?.content.map((v) => (
                <option key={v.id} value={v.userId}>
                  {v.businessName} — {v.serviceCategory.replace(/_/g, ' ')} (⭐ {Number(v.rating).toFixed(1)})
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Note (optional)</label>
            <textarea
              rows={3}
              value={assignNote}
              onChange={(e) => setAssignNote(e.target.value)}
              placeholder="Any instructions for the vendor…"
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
            />
          </div>
        </div>
      </Modal>

      {/* ── Resolve Modal ────────────────────────────────────────────────── */}
      <Modal
        open={showResolveModal}
        onClose={() => setShowResolveModal(false)}
        title="Mark Complaint as Resolved"
        footer={
          <>
            <Button variant="outline" onClick={() => setShowResolveModal(false)}>Cancel</Button>
            <Button
              disabled={!resolutionNote.trim()}
              loading={resolveMutation.isPending}
              onClick={() => resolveMutation.mutate()}
            >
              Confirm Resolved
            </Button>
          </>
        }
      >
        <div className="space-y-3">
          <p className="text-sm text-gray-600">
            The resident will be notified to confirm whether the issue has been resolved.
            If they reject, the complaint will be automatically reopened.
          </p>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Resolution Note <span className="text-red-500">*</span>
            </label>
            <textarea
              rows={4}
              value={resolutionNote}
              onChange={(e) => setResolutionNote(e.target.value)}
              placeholder="Describe what was done to resolve this complaint…"
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
            />
          </div>
        </div>
      </Modal>

      {/* ── Resident Verification Modal ──────────────────────────────────── */}
      <Modal
        open={showVerifyModal}
        onClose={() => setShowVerifyModal(false)}
        title="Has Your Issue Been Resolved?"
        size="sm"
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            The maintenance team has marked your complaint as resolved.
            Please confirm if the issue has been fixed.
          </p>
          {c.resolutionNote && (
            <div className="rounded-lg bg-gray-50 p-3 text-sm text-gray-700">
              <p className="font-medium mb-1">Resolution note:</p>
              <p>{c.resolutionNote}</p>
            </div>
          )}
          <div className="flex gap-3 pt-2">
            <Button
              className="flex-1 bg-green-600 hover:bg-green-700"
              loading={verifyMutation.isPending}
              onClick={() => verifyMutation.mutate(true)}
            >
              <LuCircleCheck className="h-4 w-4" /> Yes, Resolved
            </Button>
            <Button
              variant="danger"
              className="flex-1"
              loading={verifyMutation.isPending}
              onClick={() => verifyMutation.mutate(false)}
            >
              <LuRefreshCw className="h-4 w-4" /> No, Reopen
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}