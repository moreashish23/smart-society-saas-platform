import api from './axios'
import type {
  ApiResponse, PagedResponse, Complaint, ComplaintComment,
  ComplaintStatus, ComplaintCategory, ComplaintPriority,
} from '@/types'

export interface CreateComplaintRequest {
  title: string
  description: string
  category: ComplaintCategory
  priority?: ComplaintPriority
  location?: string
}

export interface AssignComplaintRequest {
  assignedToId: string
  note?: string
}

export interface ResolveComplaintRequest {
  resolutionNote: string
}

export interface AddCommentRequest { content: string }

export interface ComplaintSearchParams {
  status?: ComplaintStatus
  category?: ComplaintCategory
  priority?: ComplaintPriority
  keyword?: string
  page?: number
  size?: number
}

export const complaintsApi = {
  create: (data: CreateComplaintRequest) =>
    api.post<ApiResponse<Complaint>>('/complaints', data),

  list: (params: ComplaintSearchParams = {}) =>
    api.get<ApiResponse<PagedResponse<Complaint>>>('/complaints', { params }),

  getMy: (params: { page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<Complaint>>>('/complaints/my', { params }),

  getAssigned: (params: { page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<Complaint>>>('/complaints/assigned', { params }),

  getById: (id: string) =>
    api.get<ApiResponse<Complaint>>(`/complaints/${id}`),

  assign: (id: string, data: AssignComplaintRequest) =>
    api.patch<ApiResponse<Complaint>>(`/complaints/${id}/assign`, data),

  accept: (id: string) =>
    api.patch<ApiResponse<Complaint>>(`/complaints/${id}/accept`),

  startWork: (id: string) =>
    api.patch<ApiResponse<Complaint>>(`/complaints/${id}/start-work`),

  completeWork: (id: string) =>
    api.patch<ApiResponse<Complaint>>(`/complaints/${id}/complete-work`),

  resolve: (id: string, data: ResolveComplaintRequest) =>
    api.patch<ApiResponse<Complaint>>(`/complaints/${id}/resolve`, data),

  verify: (id: string, resolved: boolean) =>
    api.post<ApiResponse<Complaint>>(`/complaints/${id}/verify?resolved=${resolved}`),

  cancel: (id: string) =>
    api.patch<ApiResponse<Complaint>>(`/complaints/${id}/cancel`),

  addComment: (id: string, data: AddCommentRequest) =>
    api.post<ApiResponse<ComplaintComment>>(`/complaints/${id}/comments`, data),

  getComments: (id: string) =>
    api.get<ApiResponse<ComplaintComment[]>>(`/complaints/${id}/comments`),
}