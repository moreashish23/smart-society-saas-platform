import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { analyticsApi } from '@/api/analytics.api'
import { useAuth } from '@/hooks/useAuth'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { PageSpinner } from '@/components/ui/Spinner'
import { formatHours, formatPercent } from '@/lib/utils'
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer,
} from 'recharts'
import {
  LuTrendingUp, LuTriangleAlert, LuCircleCheck,
  LuClock, LuZap, LuStar, LuRefreshCw,
} from 'react-icons/lu'
import type { TrendPoint } from '@/types'

export default function AnalyticsPage() {
  const { isManagerOrAbove } = useAuth()
  const [trendDays, setTrendDays] = useState(30)

  const { data: dashboard, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['analytics', 'dashboard'],
    queryFn:  () => analyticsApi.getDashboard().then((r) => r.data.data),
    enabled:  isManagerOrAbove,
    staleTime: 5 * 60 * 1000,
  })

  const { data: trendData } = useQuery<TrendPoint[]>({
    queryKey: ['analytics', 'trend', trendDays],
    queryFn:  () => analyticsApi.getTrend(trendDays).then((r) => r.data.data),
    enabled:  isManagerOrAbove,
  })

  if (!isManagerOrAbove) {
    return (
      <div className="flex h-64 items-center justify-center">
        <p className="text-gray-500">Analytics are available to managers and above.</p>
      </div>
    )
  }

  if (isLoading) return <PageSpinner />

  if (!dashboard) return null

  const priorityData = [
    { name: 'Critical', value: dashboard.criticalCount,  fill: '#ef4444' },
    { name: 'High',     value: dashboard.highCount,      fill: '#f97316' },
    { name: 'Medium',   value: dashboard.mediumCount,    fill: '#eab308' },
    { name: 'Low',      value: dashboard.lowCount,       fill: '#22c55e' },
  ]

  const statusData = [
    { name: 'Open',        value: dashboard.openComplaints },
    { name: 'In Progress', value: dashboard.inProgressComplaints },
    { name: 'Pending',     value: dashboard.pendingVerificationComplaints },
    { name: 'Closed',      value: dashboard.closedComplaints },
    { name: 'Reopened',    value: dashboard.reopenedComplaints },
  ]

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Analytics Dashboard</h1>
          <p className="mt-1 text-sm text-gray-500">Real-time society performance metrics</p>
        </div>
        <Button
          variant="outline"
          onClick={() => refetch()}
          loading={isFetching}
        >
          <LuRefreshCw className="h-4 w-4" /> Refresh
        </Button>
      </div>

      {/* KPI Row 1 */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <MetricCard icon={<LuTrendingUp className="h-5 w-5 text-blue-600" />}   label="Total Complaints"   value={dashboard.totalComplaints}                    bg="bg-blue-50" />
        <MetricCard icon={<LuClock className="h-5 w-5 text-orange-600" />}      label="Open"               value={dashboard.openComplaints}                     bg="bg-orange-50" />
        <MetricCard icon={<LuCircleCheck className="h-5 w-5 text-green-600" />} label="Closed"             value={dashboard.closedComplaints}                   bg="bg-green-50" />
        <MetricCard icon={<LuTriangleAlert className="h-5 w-5 text-red-600" />} label="SLA Breaches"       value={dashboard.slaBreaches}                        bg="bg-red-50" />
      </div>

      {/* KPI Row 2 */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <MetricCard icon={<LuZap className="h-5 w-5 text-red-600" />}             label="Critical"            value={dashboard.criticalCount}                     bg="bg-red-50" />
        <MetricCard icon={<LuClock className="h-5 w-5 text-indigo-600" />}        label="Avg Resolution"      value={formatHours(dashboard.avgResolutionHours)}   bg="bg-indigo-50" />
        <MetricCard icon={<LuCircleCheck className="h-5 w-5 text-teal-600" />}    label="SLA Compliance"      value={formatPercent(dashboard.slaComplianceRate)}  bg="bg-teal-50" />
        <MetricCard icon={<LuStar className="h-5 w-5 text-yellow-600" />}         label="Satisfaction Rate"   value={formatPercent(dashboard.residentSatisfactionRate)} bg="bg-yellow-50" />
      </div>

      {/* Charts Row 1 */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Trend Line Chart */}
        <Card className="lg:col-span-2">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="font-semibold text-gray-900">Complaint Trend</h3>
            <div className="flex gap-1">
              {[7, 14, 30, 90].map((d) => (
                <button
                  key={d}
                  onClick={() => setTrendDays(d)}
                  className={`rounded px-2.5 py-1 text-xs font-medium transition-colors ${
                    trendDays === d
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  {d}d
                </button>
              ))}
            </div>
          </div>
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={trendData ?? dashboard.complaintTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} tickFormatter={(d) => d?.slice(5) ?? d} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #e5e7eb' }} />
              <Legend iconSize={12} wrapperStyle={{ fontSize: 12 }} />
              <Line type="monotone" dataKey="created"   stroke="#3b82f6" strokeWidth={2} dot={false} name="Created" />
              <Line type="monotone" dataKey="closed"    stroke="#22c55e" strokeWidth={2} dot={false} name="Closed" />
              <Line type="monotone" dataKey="escalated" stroke="#ef4444" strokeWidth={2} dot={false} name="Escalated" />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        {/* Priority Pie */}
        <Card>
          <h3 className="mb-4 font-semibold text-gray-900">Priority Breakdown</h3>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={priorityData}
                cx="50%"
                cy="50%"
                innerRadius={55}
                outerRadius={80}
                paddingAngle={3}
                dataKey="value"
              >
                {priorityData.map((entry, i) => (
                  <Cell key={i} fill={entry.fill} />
                ))}
              </Pie>
              <Tooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
              <Legend iconSize={12} wrapperStyle={{ fontSize: 11 }} />
            </PieChart>
          </ResponsiveContainer>
        </Card>
      </div>

      {/* Charts Row 2 */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Status Bar Chart */}
        <Card>
          <h3 className="mb-4 font-semibold text-gray-900">Status Distribution</h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={statusData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" horizontal={false} />
              <XAxis type="number" tick={{ fontSize: 11 }} />
              <YAxis dataKey="name" type="category" tick={{ fontSize: 11 }} width={80} />
              <Tooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
              <Bar dataKey="value" name="Count" fill="#3b82f6" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        {/* Top Vendors */}
        {dashboard.topVendors.length > 0 && (
          <Card>
            <h3 className="mb-4 font-semibold text-gray-900">Top Vendors by Rating</h3>
            <div className="space-y-3">
              {dashboard.topVendors.slice(0, 5).map((v, i) => (
                <div key={v.vendorId} className="flex items-center gap-3">
                  <span className="flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full bg-gray-100 text-xs font-bold text-gray-600">
                    {i + 1}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-gray-900">{v.businessName}</p>
                    <div className="mt-0.5 flex items-center gap-2">
                      <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-gray-200">
                        <div
                          className="h-full rounded-full bg-yellow-400"
                          style={{ width: `${(v.rating / 5) * 100}%` }}
                        />
                      </div>
                      <span className="flex-shrink-0 text-xs font-semibold text-yellow-600">
                        ⭐ {Number(v.rating).toFixed(1)}
                      </span>
                    </div>
                  </div>
                  <div className="flex-shrink-0 text-right">
                    <p className="text-sm font-semibold text-gray-900">{v.completedJobs}</p>
                    <p className="text-xs text-gray-500">jobs</p>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        )}
      </div>
    </div>
  )
}

// ── Sub-components ─────────────────────────────────────────────────────────────

interface MetricCardProps {
  icon: React.ReactNode
  label: string
  value: number | string
  bg: string
}

function MetricCard({ icon, label, value, bg }: MetricCardProps) {
  return (
    <Card className="flex items-center gap-4">
      <div className={`flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-xl ${bg}`}>
        {icon}
      </div>
      <div className="min-w-0">
        <p className="text-2xl font-bold text-gray-900">{value}</p>
        <p className="truncate text-xs text-gray-500">{label}</p>
      </div>
    </Card>
  )
}