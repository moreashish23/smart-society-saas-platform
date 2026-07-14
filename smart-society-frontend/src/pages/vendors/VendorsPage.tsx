import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { vendorsApi } from '@/api/vendors.api'
import { useAuth } from '@/hooks/useAuth'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Select } from '@/components/ui/Select'
import { Modal } from '@/components/ui/Modal'
import { Input } from '@/components/ui/Input'
import { Pagination } from '@/components/ui/Pagination'
import { PageSpinner } from '@/components/ui/Spinner'
import { getVendorStatusColour } from '@/lib/utils'
import {
  LuPlus, LuSearch, LuStar, LuCircleCheck,
  LuPause, LuPlay, LuStore,
} from 'react-icons/lu'
import toast from 'react-hot-toast'
import type { VendorStatus, ServiceCategory } from '@/types'

const STATUS_OPTIONS = [
  { value: '',                label: 'All Statuses' },
  { value: 'ACTIVE',          label: 'Active' },
  { value: 'PENDING_APPROVAL', label: 'Pending Approval' },
  { value: 'SUSPENDED',       label: 'Suspended' },
  { value: 'INACTIVE',        label: 'Inactive' },
]

const CATEGORY_OPTIONS = [
  { value: '', label: 'All Categories' },
  { value: 'PLUMBING',            label: 'Plumbing' },
  { value: 'ELECTRICITY',         label: 'Electricity' },
  { value: 'LIFT_MAINTENANCE',    label: 'Lift Maintenance' },
  { value: 'HOUSEKEEPING',        label: 'Housekeeping' },
  { value: 'SECURITY',            label: 'Security' },
  { value: 'CARPENTRY',           label: 'Carpentry' },
  { value: 'PAINTING',            label: 'Painting' },
  { value: 'PEST_CONTROL',        label: 'Pest Control' },
  { value: 'INTERNET',            label: 'Internet' },
  { value: 'GENERAL_MAINTENANCE', label: 'General Maintenance' },
  { value: 'OTHER',               label: 'Other' },
]

export default function VendorsPage() {
  const { isManagerOrAbove } = useAuth()
  const queryClient = useQueryClient()

  const [page,     setPage]     = useState(0)
  const [keyword,  setKeyword]  = useState('')
  const [status,   setStatus]   = useState<VendorStatus | ''>('')
  const [category, setCategory] = useState<ServiceCategory | ''>('')
  const [showAddModal, setShowAddModal] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['vendors', page, keyword, status, category],
    queryFn:  () => vendorsApi.list({
      page, size: 20,
      keyword:  keyword  || undefined,
      status:   status   || undefined,
      category: category || undefined,
    }).then((r) => r.data.data),
    staleTime: 30_000,
  })

  const approveMutation = useMutation({
    mutationFn: (id: string) => vendorsApi.approve(id),
    onSuccess:  () => { queryClient.invalidateQueries({ queryKey: ['vendors'] }); toast.success('Vendor approved') },
    onError:    () => toast.error('Failed to approve vendor'),
  })

  const suspendMutation = useMutation({
    mutationFn: (id: string) => vendorsApi.suspend(id),
    onSuccess:  () => { queryClient.invalidateQueries({ queryKey: ['vendors'] }); toast.success('Vendor suspended') },
    onError:    () => toast.error('Failed to suspend vendor'),
  })

  const activateMutation = useMutation({
    mutationFn: (id: string) => vendorsApi.activate(id),
    onSuccess:  () => { queryClient.invalidateQueries({ queryKey: ['vendors'] }); toast.success('Vendor activated') },
    onError:    () => toast.error('Failed to activate vendor'),
  })

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Vendors</h1>
          {data && (
            <p className="mt-1 text-sm text-gray-500">{data.totalElements} vendors registered</p>
          )}
        </div>
        {isManagerOrAbove && (
          <Button onClick={() => setShowAddModal(true)}>
            <LuPlus className="h-4 w-4" /> Register Vendor
          </Button>
        )}
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <div className="relative sm:col-span-2">
            <LuSearch className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Search vendors…"
              value={keyword}
              onChange={(e) => { setKeyword(e.target.value); setPage(0) }}
              className="w-full rounded-lg border border-gray-300 py-2 pl-9 pr-3 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>
          <Select
            options={STATUS_OPTIONS}
            value={status}
            onChange={(e) => { setStatus(e.target.value as VendorStatus | ''); setPage(0) }}
          />
          <Select
            options={CATEGORY_OPTIONS}
            value={category}
            onChange={(e) => { setCategory(e.target.value as ServiceCategory | ''); setPage(0) }}
          />
        </div>
      </Card>

      {/* Vendor grid */}
      {isLoading ? (
        <PageSpinner />
      ) : !data?.content.length ? (
        <Card className="py-16 text-center">
          <LuStore className="mx-auto mb-3 h-10 w-10 text-gray-300" />
          <p className="text-gray-500">No vendors found</p>
        </Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.content.map((v) => (
            <Card key={v.id} className="flex flex-col gap-3">
              {/* Top row */}
              <div className="flex items-start justify-between">
                <div className="min-w-0 flex-1">
                  <h3 className="truncate font-semibold text-gray-900">{v.businessName}</h3>
                  <p className="text-xs text-gray-500">{v.contactPerson}</p>
                </div>
                <Badge label={v.status} className={getVendorStatusColour(v.status)} />
              </div>

              {/* Category + rating */}
              <div className="flex items-center justify-between">
                <Badge
                  label={v.serviceCategory.replace(/_/g, ' ')}
                  className="bg-blue-50 text-blue-700 border-blue-100"
                />
                <span className="flex items-center gap-1 text-sm font-semibold text-yellow-600">
                  <LuStar className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                  {Number(v.rating).toFixed(1)}
                </span>
              </div>

              {/* Stats */}
              <div className="grid grid-cols-2 gap-2 rounded-lg bg-gray-50 p-3 text-xs">
                <div>
                  <p className="text-gray-500">Total Jobs</p>
                  <p className="font-semibold text-gray-900">{v.totalJobs}</p>
                </div>
                <div>
                  <p className="text-gray-500">Completed</p>
                  <p className="font-semibold text-gray-900">{v.completedJobs}</p>
                </div>
              </div>

              {/* Contact */}
              <p className="text-xs text-gray-500">{v.contactEmail}</p>

              {/* Actions */}
              {isManagerOrAbove && (
                <div className="flex gap-2 pt-1">
                  {v.status === 'PENDING_APPROVAL' && (
                    <Button
                      size="sm"
                      className="flex-1"
                      loading={approveMutation.isPending}
                      onClick={() => approveMutation.mutate(v.id)}
                    >
                      <LuCircleCheck className="h-3.5 w-3.5" /> Approve
                    </Button>
                  )}
                  {v.status === 'ACTIVE' && (
                    <Button
                      size="sm"
                      variant="danger"
                      className="flex-1"
                      loading={suspendMutation.isPending}
                      onClick={() => suspendMutation.mutate(v.id)}
                    >
                      <LuPause className="h-3.5 w-3.5" /> Suspend
                    </Button>
                  )}
                  {v.status === 'SUSPENDED' && (
                    <Button
                      size="sm"
                      variant="secondary"
                      className="flex-1"
                      loading={activateMutation.isPending}
                      onClick={() => activateMutation.mutate(v.id)}
                    >
                      <LuPlay className="h-3.5 w-3.5" /> Activate
                    </Button>
                  )}
                </div>
              )}
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

      {/* Register vendor modal */}
      <RegisterVendorModal
        open={showAddModal}
        onClose={() => setShowAddModal(false)}
        onSuccess={() => {
          setShowAddModal(false)
          queryClient.invalidateQueries({ queryKey: ['vendors'] })
        }}
      />
    </div>
  )
}

// ── Register Vendor Modal ─────────────────────────────────────────────────────

function RegisterVendorModal({ open, onClose, onSuccess }: {
  open: boolean; onClose: () => void; onSuccess: () => void
}) {
  const [form, setForm] = useState({
    userId: '', businessName: '', contactPerson: '', contactEmail: '',
    contactPhone: '', serviceCategory: '', description: '', address: '',
  })

  const { mutate, isPending } = useMutation({
    mutationFn: () => vendorsApi.create({
      userId:          form.userId,
      businessName:    form.businessName,
      contactPerson:   form.contactPerson,
      contactEmail:    form.contactEmail,
      contactPhone:    form.contactPhone,
      serviceCategory: form.serviceCategory as ServiceCategory,
      description:     form.description,
      address:         form.address,
    }),
    onSuccess: () => { toast.success('Vendor registered'); onSuccess() },
    onError:   () => toast.error('Failed to register vendor'),
  })

  const set = (k: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }))

  return (
    <Modal
      open={open} onClose={onClose}
      title="Register New Vendor"
      size="lg"
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button loading={isPending} onClick={() => mutate()}>Register Vendor</Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input label="User ID" required value={form.userId} onChange={set('userId')} placeholder="User UUID from auth-service" />
        <Input label="Business Name" required value={form.businessName} onChange={set('businessName')} />
        <Input label="Contact Person" required value={form.contactPerson} onChange={set('contactPerson')} />
        <Input label="Contact Email" type="email" required value={form.contactEmail} onChange={set('contactEmail')} />
        <Input label="Contact Phone" required value={form.contactPhone} onChange={set('contactPhone')} />
        <Select
          label="Service Category"
          required
          options={CATEGORY_OPTIONS.filter((o) => o.value)}
          placeholder="Select category"
          value={form.serviceCategory}
          onChange={set('serviceCategory')}
        />
        <div className="sm:col-span-2">
          <Input label="Address" value={form.address} onChange={set('address')} />
        </div>
        <div className="sm:col-span-2 flex flex-col gap-1">
          <label className="text-sm font-medium text-gray-700">Description</label>
          <textarea
            rows={3}
            value={form.description}
            onChange={set('description')}
            className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
            placeholder="Services offered, experience, etc."
          />
        </div>
      </div>
    </Modal>
  )
}