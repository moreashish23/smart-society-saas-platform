import api from './axios'
import type { ApiResponse, PagedResponse, Dashboard, TrendPoint, AuditLog, Notification, AuditAction } from '@/types'

export interface ComplaintStatsResponse {
  societyId: string
  from: string
  to: string
  totalComplaints: number
  openComplaints: number
  closedComplaints: number
  slaBreaches: number
  avgResolutionHours: number
  slaComplianceRate: number
  dailyStats: DailyStat[]
}

export interface DailyStat {
  date: string
  totalComplaints: number
  openComplaints: number
  closedComplaints: number
  slaBreaches: number
  avgResolutionHours: number
}

export interface VendorPerformanceResponse {
  vendorId: string
  businessName: string
  serviceCategory: string
  statMonth: string
  jobsAssigned: number
  jobsCompleted: number
  jobsCancelled: number
  avgRating: number
  completionRate: number
}

export const analyticsApi = {
  getDashboard: () =>
    api.get<ApiResponse<Dashboard>>('/analytics/dashboard'),

  getComplaintStats: (params: { from?: string; to?: string } = {}) =>
    api.get<ApiResponse<ComplaintStatsResponse>>('/analytics/complaints', { params }),

  getTrend: (days = 30) =>
    api.get<ApiResponse<TrendPoint[]>>('/analytics/complaints/trend', { params: { days } }),

  getVendorPerformance: (params: { month?: string } = {}) =>
    api.get<ApiResponse<VendorPerformanceResponse[]>>('/analytics/vendors/performance', { params }),
}

export const notificationsApi = {
  list: (params: { unreadOnly?: boolean; page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<Notification>>>('/notifications', { params }),

  getUnreadCount: () =>
    api.get<ApiResponse<{ unreadCount: number }>>('/notifications/unread-count'),

  markRead: (id: string) =>
    api.patch<ApiResponse<Notification>>(`/notifications/${id}/read`),

  markAllRead: () =>
    api.patch<ApiResponse<{ markedRead: number }>>('/notifications/read-all'),
}

export const auditApi = {
  list: (params: {
    userId?: string; action?: AuditAction; entityType?: string
    entityId?: string; from?: string; to?: string; page?: number; size?: number
  } = {}) =>
    api.get<ApiResponse<PagedResponse<AuditLog>>>('/audit', { params }),

  getById: (id: string) =>
    api.get<ApiResponse<AuditLog>>(`/audit/${id}`),

  getEntityTrail: (entityType: string, entityId: string, params: { page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<AuditLog>>>(`/audit/entity/${entityType}/${entityId}`, { params }),

  getUserLogs: (userId: string, params: { page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<AuditLog>>>(`/audit/user/${userId}`, { params }),
}