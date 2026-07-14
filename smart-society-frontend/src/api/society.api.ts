import api from './axios'
import type {
  ApiResponse, PagedResponse,
  Society, SocietyMember, Notice,
  SocietyStatus, MemberStatus, NoticeType, NoticeStatus,
} from '@/types'

export interface CreateSocietyRequest {
  name: string; code: string; description?: string
  addressLine1: string; addressLine2?: string
  city: string; state: string; pincode: string; country?: string
  contactEmail: string; contactPhone: string
  totalUnits?: number; totalFloors?: number
  subscriptionPlan?: string; logoUrl?: string
}

export interface AddMemberRequest {
  userId: string; role: string
  flatNumber?: string; block?: string; floor?: number
}

export interface CreateNoticeRequest {
  title: string; content: string; noticeType: NoticeType
  priority?: boolean; expiresAt?: string; publishImmediately?: boolean
}

export const societyApi = {
  create: (data: CreateSocietyRequest) =>
    api.post<ApiResponse<Society>>('/societies', data),

  list: (params: { status?: SocietyStatus; name?: string; page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<Society>>>('/societies', { params }),

  getById: (id: string) =>
    api.get<ApiResponse<Society>>(`/societies/${id}`),

  update: (id: string, data: Partial<CreateSocietyRequest>) =>
    api.put<ApiResponse<Society>>(`/societies/${id}`, data),

  activate: (id: string) =>
    api.patch<ApiResponse<Society>>(`/societies/${id}/activate`),

  deactivate: (id: string) =>
    api.patch<ApiResponse<Society>>(`/societies/${id}/deactivate`),

  // Members
  addMember: (societyId: string, data: AddMemberRequest) =>
    api.post<ApiResponse<SocietyMember>>(`/societies/${societyId}/members`, data),

  getMembers: (societyId: string, params: { role?: string; status?: MemberStatus; page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<SocietyMember>>>(`/societies/${societyId}/members`, { params }),

  removeMember: (societyId: string, memberId: string) =>
    api.delete<ApiResponse<void>>(`/societies/${societyId}/members/${memberId}`),

  updateMemberRole: (societyId: string, memberId: string, newRole: string) =>
    api.patch<ApiResponse<SocietyMember>>(`/societies/${societyId}/members/${memberId}/role`, null, {
      params: { newRole },
    }),

  // Notices
  createNotice: (societyId: string, data: CreateNoticeRequest) =>
    api.post<ApiResponse<Notice>>(`/societies/${societyId}/notices`, data),

  getNotices: (societyId: string, params: { status?: NoticeStatus; type?: NoticeType; page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<Notice>>>(`/societies/${societyId}/notices`, { params }),

  getActiveNotices: (societyId: string) =>
    api.get<ApiResponse<Notice[]>>(`/societies/${societyId}/notices/active`),

  publishNotice: (societyId: string, noticeId: string) =>
    api.patch<ApiResponse<Notice>>(`/societies/${societyId}/notices/${noticeId}/publish`),

  archiveNotice: (societyId: string, noticeId: string) =>
    api.patch<ApiResponse<Notice>>(`/societies/${societyId}/notices/${noticeId}/archive`),

  deleteNotice: (societyId: string, noticeId: string) =>
    api.delete<ApiResponse<void>>(`/societies/${societyId}/notices/${noticeId}`),
}