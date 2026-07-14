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
import { LuPlus, LuBuilding2, LuCircleCheck, LuPause } from 'react-icons/lu'
import toast from 'react-hot-toast'
import type { SocietyStatus } from '@/types'

const STATUS_OPTIONS = [
  { value: '',          label: 'All Statuses' },
  { value: 'ACTIVE',    label: 'Active' },
  { value: 'INACTIVE',  label: 'Inactive' },
  { value: 'SUSPENDED', label: 'Suspended' },
]

const PLAN_OPTIONS = [
  { value: 'BASIC',        label: 'Basic' },
  { value: 'PROFESSIONAL', label: 'Professional' },
  { value: 'ENTERPRISE',   label: 'Enterprise' },
]

export default function SocietiesPage() {
  const { isSuperAdmin } = useAuth()
  const queryClient = useQueryClient()
  const [page, setPage]   = useState(0)
  const [status, setStatus] = useState<SocietyStatus | ''>('')
  const [showCreate, setShowCreate] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['societies', page, status],
    queryFn:  () => societyApi.list({ page, size: 20, status: status || undefined })
      .then((r) => r.data.data),
    enabled: isSuperAdmin,
    staleTime: 30_000,
  })

  const activateMutation = useMutation({
    mutationFn: (id: string) => societyApi.activate(id),
    onSuccess:  () => { queryClient.invalidateQueries({ queryKey: ['societies'] }); toast.success('Society activated') },
  })

  const deactivateMutation = useMutation({
    mutationFn: (id: string) => societyApi.deactivate(id),
    onSuccess:  () => { queryClient.invalidateQueries({ queryKey: ['societies'] }); toast.success('Society deactivated') },
  })

  if (!isSuperAdmin) {
    return (
      <div className="flex h-64 items-center justify-center">
        <p className="text-gray-500">Only Super Admins can manage societies.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Societies</h1>
          {data && <p className="mt-1 text-sm text-gray-500">{data.totalElements} societies registered</p>}
        </div>
        <Button onClick={() => setShowCreate(true)}>
          <LuPlus className="h-4 w-4" /> Create Society
        </Button>
      </div>

      {/* Filter */}
      <Card className="p-4">
        <div className="flex gap-3">
          <Select
            options={STATUS_OPTIONS}
            value={status}
            onChange={(e) => { setStatus(e.target.value as SocietyStatus | ''); setPage(0) }}
          />
        </div>
      </Card>

      {isLoading ? <PageSpinner /> : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data?.content.map((s) => (
            <Card key={s.id} className="flex flex-col gap-3">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-100">
                    <LuBuilding2 className="h-5 w-5 text-blue-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">{s.name}</h3>
                    <p className="text-xs text-gray-500">{s.code}</p>
                  </div>
                </div>
                <Badge
                  label={s.status}
                  className={
                    s.status === 'ACTIVE'
                      ? 'bg-green-100 text-green-800 border-green-200'
                      : s.status === 'SUSPENDED'
                      ? 'bg-red-100 text-red-800 border-red-200'
                      : 'bg-gray-100 text-gray-700 border-gray-200'
                  }
                />
              </div>

              <div className="text-xs text-gray-500">
                <p>{s.addressLine1}, {s.city}, {s.state}</p>
                <p>{s.contactEmail}</p>
              </div>

              <div className="grid grid-cols-3 gap-2 rounded-lg bg-gray-50 p-3 text-xs text-center">
                <div>
                  <p className="font-semibold text-gray-900">{s.totalUnits}</p>
                  <p className="text-gray-500">Units</p>
                </div>
                <div>
                  <p className="font-semibold text-gray-900">{s.totalMembers ?? 0}</p>
                  <p className="text-gray-500">Members</p>
                </div>
                <div>
                  <Badge label={s.subscriptionPlan} className="bg-blue-50 text-blue-700 border-blue-100" />
                </div>
              </div>

              <div className="flex gap-2">
                {s.status !== 'ACTIVE' ? (
                  <Button
                    size="sm" className="flex-1"
                    loading={activateMutation.isPending}
                    onClick={() => activateMutation.mutate(s.id)}
                  >
                    <LuCircleCheck className="h-3.5 w-3.5" /> Activate
                  </Button>
                ) : (
                  <Button
                    size="sm" variant="danger" className="flex-1"
                    loading={deactivateMutation.isPending}
                    onClick={() => deactivateMutation.mutate(s.id)}
                  >
                    <LuPause className="h-3.5 w-3.5" /> Deactivate
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}

      {data && (
        <Pagination
          page={data.page} totalPages={data.totalPages}
          totalElements={data.totalElements} size={data.size}
          onPageChange={setPage}
        />
      )}

      <CreateSocietyModal
        open={showCreate}
        onClose={() => setShowCreate(false)}
        onSuccess={() => {
          setShowCreate(false)
          queryClient.invalidateQueries({ queryKey: ['societies'] })
        }}
      />
    </div>
  )
}

function CreateSocietyModal({ open, onClose, onSuccess }: {
  open: boolean; onClose: () => void; onSuccess: () => void
}) {
  const [form, setForm] = useState({
    name: '', code: '', description: '', addressLine1: '', addressLine2: '',
    city: '', state: '', pincode: '', country: 'India',
    contactEmail: '', contactPhone: '', totalUnits: '0', totalFloors: '0',
    subscriptionPlan: 'BASIC',
  })

  const { mutate, isPending } = useMutation({
    mutationFn: () => societyApi.create({
      ...form,
      totalUnits:   parseInt(form.totalUnits) || 0,
      totalFloors:  parseInt(form.totalFloors) || 0,
    }),
    onSuccess: () => { toast.success('Society created'); onSuccess() },
    onError:   (err: any) => toast.error(err?.response?.data?.message ?? 'Failed to create society'),
  })

  const set = (k: string) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
      setForm((f) => ({ ...f, [k]: e.target.value }))

  return (
    <Modal
      open={open} onClose={onClose} title="Create New Society" size="xl"
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button loading={isPending} onClick={() => mutate()}>Create Society</Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input label="Society Name" required value={form.name} onChange={set('name')} />
        <Input label="Code" required value={form.code} onChange={set('code')} placeholder="e.g. GV001" />
        <Input label="Contact Email" type="email" required value={form.contactEmail} onChange={set('contactEmail')} />
        <Input label="Contact Phone" required value={form.contactPhone} onChange={set('contactPhone')} />
        <div className="sm:col-span-2">
          <Input label="Address Line 1" required value={form.addressLine1} onChange={set('addressLine1')} />
        </div>
        <Input label="Address Line 2" value={form.addressLine2} onChange={set('addressLine2')} />
        <Input label="City" required value={form.city} onChange={set('city')} />
        <Input label="State" required value={form.state} onChange={set('state')} />
        <Input label="Pincode" required value={form.pincode} onChange={set('pincode')} />
        <Input label="Country" value={form.country} onChange={set('country')} />
        <Input label="Total Units" type="number" value={form.totalUnits} onChange={set('totalUnits')} />
        <Input label="Total Floors" type="number" value={form.totalFloors} onChange={set('totalFloors')} />
        <Select
          label="Subscription Plan"
          options={PLAN_OPTIONS}
          value={form.subscriptionPlan}
          onChange={set('subscriptionPlan')}
        />
      </div>
    </Modal>
  )
}