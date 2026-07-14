import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { authApi } from '@/api/auth.api'
import { Card } from '@/components/ui/Card'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { LuArrowLeft, LuShieldCheck } from 'react-icons/lu'
import toast from 'react-hot-toast'

const schema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword:     z.string()
      .min(8, 'Password must be at least 8 characters')
      .regex(
        /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/,
        'Must contain uppercase, lowercase, digit and special character',
      ),
    confirmPassword: z.string().min(1, 'Please confirm your new password'),
  })
  .refine((d) => d.newPassword === d.confirmPassword, {
    message:  'Passwords do not match',
    path:     ['confirmPassword'],
  })

type FormData = z.infer<typeof schema>

export default function ChangePasswordPage() {
  const navigate = useNavigate()

  const { register, handleSubmit, formState: { errors }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const { mutate, isPending } = useMutation({
    mutationFn: (data: FormData) =>
      authApi.changePassword({
        currentPassword: data.currentPassword,
        newPassword:     data.newPassword,
        confirmPassword: data.confirmPassword,
      }),
    onSuccess: () => {
      toast.success('Password changed successfully. Please sign in again.')
      reset()
      navigate('/login')
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message ?? 'Failed to change password.')
    },
  })

  return (
    <div className="mx-auto max-w-lg space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate(-1)}
          className="rounded-lg p-2 text-gray-500 hover:bg-gray-100"
        >
          <LuArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Change Password</h1>
          <p className="text-sm text-gray-500">All sessions will be signed out after change</p>
        </div>
      </div>

      <Card>
        <div className="mb-6 flex items-center gap-3 rounded-lg bg-blue-50 p-4">
          <LuShieldCheck className="h-5 w-5 flex-shrink-0 text-blue-600" />
          <p className="text-sm text-blue-800">
            After changing your password, you will be signed out of all devices and
            redirected to the login page.
          </p>
        </div>

        <form onSubmit={handleSubmit((d) => mutate(d))} className="space-y-5" noValidate>
          <Input
            label="Current Password"
            type="password"
            required
            placeholder="Your current password"
            error={errors.currentPassword?.message}
            {...register('currentPassword')}
          />

          <Input
            label="New Password"
            type="password"
            required
            placeholder="At least 8 chars with upper, lower, digit, special"
            error={errors.newPassword?.message}
            {...register('newPassword')}
          />

          <Input
            label="Confirm New Password"
            type="password"
            required
            placeholder="Repeat your new password"
            error={errors.confirmPassword?.message}
            {...register('confirmPassword')}
          />

          <div className="flex gap-3 pt-2">
            <Button
              type="button"
              variant="outline"
              className="flex-1"
              onClick={() => navigate(-1)}
            >
              Cancel
            </Button>
            <Button type="submit" loading={isPending} className="flex-1">
              Change Password
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}