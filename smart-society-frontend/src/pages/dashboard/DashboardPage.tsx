import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { analyticsApi } from '@/api/analytics.api'
import { useAuth } from '@/hooks/useAuth'
import { Card } from '@/components/ui/Card'
import { PageSpinner } from '@/components/ui/Spinner'
import { Badge } from '@/components/ui/Badge'
import { formatHours, formatPercent } from '@/lib/utils'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, BarChart, Bar, Legend, Cell,
} from 'recharts'
import {
  LuTriangleAlert, LuCircleCheck, LuClock,
  LuTrendingUp, LuStar, LuZap,
} from 'react-icons/lu'

export default function DashboardPage() {
  const { user, isManagerOrAbove, isResident, isVendor, isStaff } = useAuth()

  const { data: dashboard, isLoading } = useQuery({
    queryKey: ['analytics', 'dashboard'],
    queryFn:  () => analyticsApi.getDashboard().then((r) => r.data.data),
    enabled:  isManagerOrAbove,
    staleTime: 5 * 60 * 1000,
    refetchInterval: 5 * 60 * 1000,
  })

  if (isLoading) return <PageSpinner />

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">
          Welcome back, {user?.firstName}!
        </h1>
        <p className="mt-1 text-sm text-gray-600">
          {new Date().toLocaleDateString('en-IN', {
            weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
          })}
        </p>
      </div>

      {/* Manager/Admin dashboard */}
      {isManagerOrAbove && dashboard && (
        <>
          {/* KPI Cards */}
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <KpiCard
              label="Total Complaints"
              value={dashboard.totalComplaints}
              icon={<LuTrendingUp className="h-5 w-5 text-blue-600" />}
              colour="blue"
            />
            <KpiCard
              label="Open"
              value={dashboard.openComplaints}
              icon={<LuClock className="h-5 w-5 text-orange-600" />}
              colour="orange"
            />
            <KpiCard
              label="Closed"
              value={dashboard.closedComplaints}
              icon={<LuCircleCheck className="h-5 w-5 text-green-600" />}
              colour="green"
            />
            <KpiCard
              label="SLA Breaches"
              value={dashboard.slaBreaches}
              icon={<LuTriangleAlert className="h-5 w-5 text-red-600" />}
              colour="red"
            />
          </div>

          {/* Second row */}
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <KpiCard
              label="Critical"
              value={dashboard.criticalCount}
              icon={<LuZap className="h-5 w-5 text-red-600" />}
              colour="red"
            />
            <KpiCard
              label="Avg Resolution"
              value={formatHours(dashboard.avgResolutionHours)}
              icon={<LuClock className="h-5 w-5 text-indigo-600" />}
              colour="indigo"
            />
            <KpiCard
              label="SLA Compliance"
              value={formatPercent(dashboard.slaComplianceRate)}
              icon={<LuCircleCheck className="h-5 w-5 text-teal-600" />}
              colour="teal"
            />
            <KpiCard
              label="Satisfaction Rate"
              value={formatPercent(dashboard.residentSatisfactionRate)}
              icon={<LuStar className="h-5 w-5 text-yellow-600" />}
              colour="yellow"
            />
          </div>

          {/* Charts row */}
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            {/* Trend chart */}
            {dashboard.complaintTrend.length > 0 && (
              <Card>
                <h3 className="mb-4 text-base font-semibold text-gray-900">Complaint Trend (30 days)</h3>
                <ResponsiveContainer width="100%" height={220}>
                  <LineChart data={dashboard.complaintTrend}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="date" tick={{ fontSize: 11 }} tickFormatter={(d) => d.slice(5)} />
                    <YAxis tick={{ fontSize: 11 }} />
                    <Tooltip
                      contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #e5e7eb' }}
                    />
                    <Legend iconSize={12} wrapperStyle={{ fontSize: 12 }} />
                    <Line type="monotone" dataKey="created"  stroke="#3b82f6" strokeWidth={2} dot={false} name="Created" />
                    <Line type="monotone" dataKey="closed"   stroke="#22c55e" strokeWidth={2} dot={false} name="Closed" />
                    <Line type="monotone" dataKey="escalated" stroke="#ef4444" strokeWidth={2} dot={false} name="Escalated" />
                  </LineChart>
                </ResponsiveContainer>
              </Card>
            )}

            {/* Priority breakdown */}
            <Card>
              <h3 className="mb-4 text-base font-semibold text-gray-900">Priority Breakdown</h3>
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={[
                  { name: 'Critical', count: dashboard.criticalCount },
                  { name: 'High',     count: dashboard.highCount     },
                  { name: 'Medium',   count: dashboard.mediumCount   },
                  { name: 'Low',      count: dashboard.lowCount      },
                ]}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
                  <Tooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                  <Bar dataKey="count" name="Complaints" radius={[4, 4, 0, 0]}>
                    {['#ef4444', '#f97316', '#eab308', '#22c55e'].map((colour, i) => (
                      <Cell key={i} fill={colour} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </div>

          {/* Top vendors */}
          {dashboard.topVendors.length > 0 && (
            <Card>
              <h3 className="mb-4 text-base font-semibold text-gray-900">Top Vendors by Rating</h3>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200 text-left text-xs font-medium uppercase tracking-wide text-gray-500">
                      <th className="pb-3 pr-4">Vendor</th>
                      <th className="pb-3 pr-4">Category</th>
                      <th className="pb-3 pr-4 text-center">Jobs</th>
                      <th className="pb-3 pr-4 text-center">Completed</th>
                      <th className="pb-3 pr-4 text-center">Completion %</th>
                      <th className="pb-3 text-center">Rating</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {dashboard.topVendors.map((v) => (
                      <tr key={v.vendorId} className="hover:bg-gray-50">
                        <td className="py-3 pr-4 font-medium text-gray-900">{v.businessName}</td>
                        <td className="py-3 pr-4">
                          <Badge
                            label={v.serviceCategory}
                            className="bg-blue-50 text-blue-700 border-blue-100"
                          />
                        </td>
                        <td className="py-3 pr-4 text-center">{v.totalJobs}</td>
                        <td className="py-3 pr-4 text-center">{v.completedJobs}</td>
                        <td className="py-3 pr-4 text-center">{formatPercent(v.completionRate)}</td>
                        <td className="py-3 text-center">
                          <span className="flex items-center justify-center gap-1 font-semibold text-yellow-600">
                            <LuStar className="h-3.5 w-3.5 fill-yellow-400 text-yellow-400" />
                            {Number(v.rating).toFixed(1)}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}
        </>
      )}

      {/* Resident dashboard */}
      {isResident && (
        <div className="space-y-4">
          <Card>
            <h3 className="mb-2 font-semibold text-gray-900">My Complaint Summary</h3>
            <p className="text-sm text-gray-600">
              Track your complaints and view their current status below.
            </p>
          </Card>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
            <QuickAction to="/complaints" label="My Complaints" icon="📋" colour="blue" />
            <QuickAction to="/complaints/new" label="Raise Complaint" icon="➕" colour="green" />
            <QuickAction to="/notices" label="Notice Board" icon="📢" colour="orange" />
          </div>
        </div>
      )}

      {/* Vendor dashboard */}
      {(isVendor || isStaff) && (
        <div className="space-y-4">
          <Card>
            <h3 className="mb-2 font-semibold text-gray-900">My Assigned Jobs</h3>
            <p className="text-sm text-gray-600">View and manage your assigned complaints.</p>
          </Card>
          <div className="grid grid-cols-2 gap-4">
            <QuickAction to="/complaints" label="Assigned Jobs" icon="🔧" colour="blue" />
            <QuickAction to="/vendors/me" label="My Profile" icon="👤" colour="purple" />
          </div>
        </div>
      )}
    </div>
  )
}

// ─── Sub-components ───────────────────────────────────────────────────────────

interface KpiCardProps {
  label: string
  value: number | string
  icon: React.ReactNode
  colour: string
}

function KpiCard({ label, value, icon, colour }: KpiCardProps) {
  const bg: Record<string, string> = {
    blue: 'bg-blue-50',   orange: 'bg-orange-50', green: 'bg-green-50',
    red:  'bg-red-50',    indigo: 'bg-indigo-50', teal:  'bg-teal-50',
    yellow: 'bg-yellow-50',
  }
  return (
    <Card className="flex items-center gap-4">
      <div className={`flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-xl ${bg[colour] ?? 'bg-gray-50'}`}>
        {icon}
      </div>
      <div>
        <p className="text-2xl font-bold text-gray-900">{value}</p>
        <p className="text-xs text-gray-500">{label}</p>
      </div>
    </Card>
  )
}

interface QuickActionProps { to: string; label: string; icon: string; colour: string }
function QuickAction({ to, label, icon, colour }: QuickActionProps) {
  const colours: Record<string, string> = {
    blue:   'bg-blue-50 hover:bg-blue-100 border-blue-200',
    green:  'bg-green-50 hover:bg-green-100 border-green-200',
    orange: 'bg-orange-50 hover:bg-orange-100 border-orange-200',
    purple: 'bg-purple-50 hover:bg-purple-100 border-purple-200',
  }
  return (
    <Link to={to} className={`flex flex-col items-center gap-2 rounded-xl border p-6 text-center transition-colors ${colours[colour] ?? ''}`}>
      <span className="text-3xl">{icon}</span>
      <span className="text-sm font-medium text-gray-700">{label}</span>
    </Link>
  )
}