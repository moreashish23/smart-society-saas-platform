import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { authApi } from '@/api/auth.api'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Button } from '@/components/ui/Button'
import { LuBuilding2 } from 'react-icons/lu'
import toast from 'react-hot-toast'

const schema = z.object({
  firstName:  z.string().min(2, 'First name must be at least 2 characters'),
  lastName:   z.string().min(2, 'Last name must be at least 2 characters'),
  email:      z.string().email('Enter a valid email address'),
  password:   z.string().min(8, 'Password must be at least 8 characters')
                .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/,
                  'Must contain uppercase, lowercase, number and special character'),
  phone:      z.string().regex(/^[+]?[0-9]{10,15}$/, 'Enter valid phone number').optional().or(z.literal('')),
  role:       z.string().min(1, 'Role is required'),
  societyId:  z.string().optional(),
  flatNumber: z.string().max(20).optional(),
})

type FormData = z.infer<typeof schema>

const ROLE_OPTIONS = [
  { value: 'RESIDENT',          label: 'Resident' },
  { value: 'SOCIETY_MANAGER',   label: 'Society Manager' },
  { value: 'COMMITTEE_MEMBER',  label: 'Committee Member' },
  { value: 'MAINTENANCE_STAFF', label: 'Maintenance Staff' },
  { value: 'VENDOR',            label: 'Vendor' },
]

export default function RegisterPage() {
  const navigate = useNavigate()

  const { register, handleSubmit, watch, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { role: 'RESIDENT' },
  })

  const selectedRole = watch('role')
  const needsSociety = selectedRole !== 'SUPER_ADMIN'

  const { mutate, isPending } = useMutation({
    mutationFn: (data: FormData) =>
      authApi.register({
        firstName:  data.firstName,
        lastName:   data.lastName,
        email:      data.email,
        password:   data.password,
        phone:      data.phone || undefined,
        role:       data.role,
        societyId:  data.societyId || undefined,
        flatNumber: data.flatNumber || undefined,
      }),
    onSuccess: () => {
      toast.success('Account created! Please sign in.')
      navigate('/login')
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message ?? 'Registration failed. Please try again.')
    },
  })

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="w-full max-w-lg">
        {/* Logo */}
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-600 shadow-lg">
            <LuBuilding2 className="h-7 w-7 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Create Account</h1>
          <p className="mt-1 text-sm text-gray-600">Join your society's management platform</p>
        </div>

        <div className="rounded-2xl bg-white p-8 shadow-xl">
          <form onSubmit={handleSubmit((d) => mutate(d))} className="space-y-4" noValidate>
            <div className="grid grid-cols-2 gap-4">
              <Input
                label="First Name" required
                error={errors.firstName?.message}
                {...register('firstName')}
              />
              <Input
                label="Last Name" required
                error={errors.lastName?.message}
                {...register('lastName')}
              />
            </div>

            <Input
              label="Email address" type="email" required
              placeholder="you@example.com"
              error={errors.email?.message}
              {...register('email')}
            />

            <Input
              label="Password" type="password" required
              placeholder="Min 8 chars, upper+lower+digit+special"
              error={errors.password?.message}
              {...register('password')}
            />

            <Input
              label="Phone number"
              placeholder="+91XXXXXXXXXX"
              error={errors.phone?.message}
              {...register('phone')}
            />

            <Select
              label="Role" required
              options={ROLE_OPTIONS}
              error={errors.role?.message}
              {...register('role')}
            />

            {needsSociety && (
              <Input
                label="Society ID"
                placeholder="UUID of your society"
                hint="Ask your society manager for this ID"
                error={errors.societyId?.message}
                {...register('societyId')}
              />
            )}

            {selectedRole === 'RESIDENT' && (
              <Input
                label="Flat Number"
                placeholder="e.g. A-301"
                error={errors.flatNumber?.message}
                {...register('flatNumber')}
              />
            )}

            <Button type="submit" className="w-full" loading={isPending} size="lg">
              Create Account
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-blue-600 hover:text-blue-700 hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}