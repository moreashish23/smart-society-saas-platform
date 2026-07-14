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
import { Pagination } from '@/components/ui/Pagination'
import { PageSpinner } from '@/components/ui/Spinner'
import { formatDate, formatRole } from '@/lib/utils'
import { LuPlus, LuUsers, LuTrash2 } from 'react-icons/lu'
import toast from 'react-hot-toast'
import type { MemberStatus, UserRole } from '@/types'

const ROLE_OPTIONS = [
  { value: 'SOCIETY_MANAGER',   label: 'Society Manager' },
  { value: 'COMMITTEE_MEMBER',  label: 'Committee Member' },
  { value: 'RESIDENT',          label: 'Resident' },
  { value: 'MAINTENANCE_STAFF', label: 'Maintenance Staff' },
  { value: 'VENDOR',            label: 'Vendor' },
]

const STATUS_OPTIONS = [
  { value: '',         label: 'All Statuses' },
  { value: 'ACTIVE',   label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'PENDING',  label: 'Pending' },
]

export default function MembersPage() {
  const { isManagerOrAbove, societyId } = useAuth()
  const queryClient = useQueryClient()
  const [page,   setPage]   = useState(0)
  const [status, setStatus] = useState<MemberStatus | ''>('')
  const [role,   setRole]   = useState('')
  const [showAdd, setShowAdd] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['members', societyId, page, status, role],
    queryFn:  () => societyApi.getMembers(societyId!, {
      page, size: 20,
      status: status || undefined,
      role:   role   || undefined,
    }).then((r) => r.data.data),
    enabled: !!societyId && isManagerOrAbove,
    staleTime: 30_000,
  })

  const removeMutation = useMutation({
    mutationFn: (memberId: string) => societyApi.removeMember(societyId!, memberId),
    onSuccess:  () => { queryClient.invalidateQueries({ queryKey: ['members'] }); toast.success('Member removed') },
    onError:    () => toast.error('Failed to remove member'),
  })

  if (!isManagerOrAbove) {
    return (
      <div className="flex h-64 items-center justify-center">
        <p className="text-gray-500">Member management is available to managers and above.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Society Members</h1>
          {data && <p className="mt-1 text-sm text-gray-500">{data.totalElements} members</p>}
        </div>
        <Button onClick={() => setShowAdd(true)}>
          <LuPlus className="h-4 w-4" /> Add Member
        </Button>
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="grid grid-cols-2 gap-3">
          <Select
            options={[{ value: '', label: 'All Roles' }, ...ROLE_OPTIONS]}
            value={role}
            onChange={(e) => { setRole(e.target.value); setPage(0) }}
          />
          <Select
            options={STATUS_OPTIONS}
            value={status}
            onChange={(e) => { setStatus(e.target.value as MemberStatus | ''); setPage(0) }}
          />
        </div>
      </Card>

      {isLoading ? <PageSpinner /> : !data?.content.length ? (
        <Card className="py-16 text-center">
          <LuUsers className="mx-auto mb-3 h-10 w-10 text-gray-300" />
          <p className="text-gray-500">No members found</p>
        </Card>
      ) : (
        <Card padding={false}>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b border-gray-200 bg-gray-50">
                <tr>
                  {['User ID', 'Role', 'Flat', 'Status', 'Joined', 'Actions'].map((h) => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data.content.map((m) => (
                  <tr key={m.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <span className="font-mono text-xs text-gray-600">{m.userId.slice(0, 8)}…</span>
                    </td>
                    <td className="px-4 py-3">
                      <Badge
                        label={formatRole(m.role as UserRole)}
                        className="bg-blue-50 text-blue-700 border-blue-100"
                      />
                    </td>
                    <td className="px-4 py-3 text-gray-700">
                      {m.flatNumber ?? '—'}
                      {m.block && ` (${m.block})`}
                    </td>
                    <td className="px-4 py-3">
                      <Badge
                        label={m.status}
                        className={
                          m.status === 'ACTIVE'
                            ? 'bg-green-100 text-green-800 border-green-200'
                            : m.status === 'PENDING'
                            ? 'bg-yellow-100 text-yellow-800 border-yellow-200'
                            : 'bg-gray-100 text-gray-700 border-gray-200'
                        }
                      />
                    </td>
                    <td className="px-4 py-3 text-xs text-gray-500">
                      {formatDate(m.joinedAt)}
                    </td>
                    <td className="px-4 py-3">
                      <button
                        onClick={() => {
                          if (confirm('Remove this member from the society?')) {
                            removeMutation.mutate(m.id)
                          }
                        }}
                        className="rounded p-1.5 text-red-500 hover:bg-red-50"
                        title="Remove member"
                      >
                        <LuTrash2 className="h-4 w-4" />
                      </button>
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
          page={data.page} totalPages={data.totalPages}
          totalElements={data.totalElements} size={data.size}
          onPageChange={setPage}
        />
      )}

      <AddMemberModal
        open={showAdd}
        onClose={() => setShowAdd(false)}
        societyId={societyId!}
        onSuccess={() => {
          setShowAdd(false)
          queryClient.invalidateQueries({ queryKey: ['members'] })
        }}
      />
    </div>
  )
}

function AddMemberModal({ open, onClose, societyId, onSuccess }: {
  open: boolean; onClose: () => void; societyId: string; onSuccess: () => void
}) {
  const [form, setForm] = useState({ userId: '', role: 'RESIDENT', flatNumber: '', block: '', floor: '' })

  const { mutate, isPending } = useMutation({
    mutationFn: () => societyApi.addMember(societyId, {
      userId:     form.userId,
      role:       form.role,
      flatNumber: form.flatNumber || undefined,
      block:      form.block      || undefined,
      floor:      form.floor ? parseInt(form.floor) : undefined,
    }),
    onSuccess: () => { toast.success('Member added'); onSuccess() },
    onError:   (err: any) => toast.error(err?.response?.data?.message ?? 'Failed to add member'),
  })

  const set = (k: string) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
      setForm((f) => ({ ...f, [k]: e.target.value }))

  return (
    <Modal
      open={open} onClose={onClose} title="Add Member"
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button loading={isPending} onClick={() => mutate()}>Add Member</Button>
        </>
      }
    >
      <div className="space-y-4">
        <Input label="User ID" required value={form.userId} onChange={set('userId')} placeholder="UUID from auth-service" />
        <Select label="Role" required options={ROLE_OPTIONS} value={form.role} onChange={set('role')} />
        <div className="grid grid-cols-3 gap-3">
          <Input label="Flat Number" value={form.flatNumber} onChange={set('flatNumber')} placeholder="e.g. A-301" />
          <Input label="Block" value={form.block} onChange={set('block')} placeholder="e.g. A" />
          <Input label="Floor" type="number" value={form.floor} onChange={set('floor')} placeholder="3" />
        </div>
      </div>
    </Modal>
  )
}