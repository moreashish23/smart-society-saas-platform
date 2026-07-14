import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { complaintsApi } from '@/api/complaints.api'
import { Card } from '@/components/ui/Card'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Button } from '@/components/ui/Button'
import { LuArrowLeft } from 'react-icons/lu'
import toast from 'react-hot-toast'
import type { ComplaintCategory, ComplaintPriority } from '@/types'

const schema = z.object({
  title:       z.string().min(5,  'Title must be at least 5 characters').max(255),
  description: z.string().min(20, 'Description must be at least 20 characters'),
  category:    z.string().min(1,  'Category is required'),
  priority:    z.string().optional(),
  location:    z.string().max(255).optional(),
})

type FormData = z.infer<typeof schema>

const CATEGORIES = [
  { value: 'WATER_LEAKAGE',  label: 'Water Leakage' },
  { value: 'PLUMBING',       label: 'Plumbing' },
  { value: 'ELECTRICITY',    label: 'Electricity' },
  { value: 'LIFT_ISSUE',     label: 'Lift Issue' },
  { value: 'PARKING',        label: 'Parking' },
  { value: 'SECURITY',       label: 'Security' },
  { value: 'HOUSEKEEPING',   label: 'Housekeeping' },
  { value: 'INTERNET_ISSUE', label: 'Internet Issue' },
  { value: 'NOISE_COMPLAINT', label: 'Noise Complaint' },
  { value: 'OTHER',          label: 'Other' },
]

const PRIORITIES = [
  { value: 'LOW',      label: 'Low — Non-urgent, can wait' },
  { value: 'MEDIUM',   label: 'Medium — Needs attention soon' },
  { value: 'HIGH',     label: 'High — Urgent issue' },
  { value: 'CRITICAL', label: 'Critical — Immediate action needed' },
]

export default function NewComplaintPage() {
  const navigate     = useNavigate()
  const queryClient  = useQueryClient()

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { priority: 'MEDIUM' },
  })

  const { mutate, isPending } = useMutation({
    mutationFn: (data: FormData) =>
      complaintsApi.create({
        title:       data.title,
        description: data.description,
        category:    data.category as ComplaintCategory,
        priority:    data.priority as ComplaintPriority,
        location:    data.location,
      }),
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ['complaints'] })
      toast.success('Complaint submitted successfully!')
      navigate(`/complaints/${res.data.data.id}`)
    },
    onError: () => {
      toast.error('Failed to submit complaint. Please try again.')
    },
  })

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate(-1)}
          className="rounded-lg p-2 text-gray-500 hover:bg-gray-100"
        >
          <LuArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Raise a Complaint</h1>
          <p className="text-sm text-gray-500">Describe your issue and we'll get it resolved</p>
        </div>
      </div>

      <Card>
        <form
          onSubmit={handleSubmit((d) => mutate(d))}
          className="space-y-5"
          noValidate
        >
          <Input
            label="Title"
            placeholder="Brief title of your complaint"
            required
            error={errors.title?.message}
            {...register('title')}
          />

          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700">
              Description <span className="text-red-500">*</span>
            </label>
            <textarea
              rows={5}
              placeholder="Describe the issue in detail — include when it started, how it's affecting you, and any steps already taken."
              className={`w-full rounded-lg border px-3 py-2 text-sm shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none ${
                errors.description ? 'border-red-400 bg-red-50' : 'border-gray-300'
              }`}
              {...register('description')}
            />
            {errors.description && (
              <p className="text-xs text-red-600">{errors.description.message}</p>
            )}
          </div>

          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
            <Select
              label="Category"
              required
              options={CATEGORIES}
              placeholder="Select category"
              error={errors.category?.message}
              {...register('category')}
            />
            <Select
              label="Priority"
              options={PRIORITIES}
              error={errors.priority?.message}
              {...register('priority')}
            />
          </div>

          <Input
            label="Location"
            placeholder="e.g. Block A, Floor 3, Flat 301"
            hint="Helps maintenance staff locate the issue faster"
            error={errors.location?.message}
            {...register('location')}
          />

          {/* SLA info box */}
          <div className="rounded-lg bg-blue-50 border border-blue-200 p-4 text-sm text-blue-800">
            <p className="font-medium mb-1">Resolution SLA</p>
            <ul className="space-y-0.5 text-xs text-blue-700">
              <li>🔴 Critical — resolved within <strong>4 hours</strong></li>
              <li>🟠 High — resolved within <strong>24 hours</strong></li>
              <li>🟡 Medium — resolved within <strong>48 hours</strong></li>
              <li>🟢 Low — resolved within <strong>72 hours</strong></li>
            </ul>
          </div>

          <div className="flex gap-3 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => navigate(-1)}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button
              type="submit"
              loading={isPending}
              className="flex-1"
            >
              Submit Complaint
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}