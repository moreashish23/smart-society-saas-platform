import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { authApi } from '@/api/auth.api'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { LuBuilding2, LuArrowLeft, LuMail } from 'react-icons/lu'
import toast from 'react-hot-toast'

const schema = z.object({ email: z.string().email('Enter a valid email address') })
type FormData = z.infer<typeof schema>

export default function ForgotPasswordPage() {
  const [sent, setSent]       = useState(false)
  const [loading, setLoading] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: FormData) => {
    setLoading(true)
    try {
      await authApi.forgotPassword(data)
      setSent(true)
    } catch {
      toast.error('Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-600 shadow-lg">
            <LuBuilding2 className="h-7 w-7 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Forgot Password</h1>
          <p className="mt-1 text-sm text-gray-600">
            Enter your email and we'll send a reset link
          </p>
        </div>

        <div className="rounded-2xl bg-white p-8 shadow-xl">
          {sent ? (
            <div className="text-center">
              <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-green-100">
                <LuMail className="h-6 w-6 text-green-600" />
              </div>
              <h3 className="font-semibold text-gray-900">Check your email</h3>
              <p className="mt-2 text-sm text-gray-600">
                If that email is registered, a reset link has been sent. Check your inbox.
              </p>
              <Link to="/login">
                <Button variant="primary" className="mt-6 w-full">
                  Back to login
                </Button>
              </Link>
            </div>
          ) : (
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
              <Input
                label="Email address"
                type="email"
                placeholder="you@example.com"
                required
                error={errors.email?.message}
                {...register('email')}
              />
              <Button type="submit" className="w-full" loading={loading} size="lg">
                Send reset link
              </Button>
            </form>
          )}

          {!sent && (
            <Link
              to="/login"
              className="mt-4 flex items-center justify-center gap-1 text-sm text-gray-600 hover:text-gray-900"
            >
              <LuArrowLeft className="h-4 w-4" /> Back to login
            </Link>
          )}
        </div>
      </div>
    </div>
  )
}