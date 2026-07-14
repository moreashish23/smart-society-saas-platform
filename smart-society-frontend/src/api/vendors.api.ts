import api from './axios'
import type {
  ApiResponse, PagedResponse,
  Vendor, VendorJob, VendorRating,
  VendorStatus, ServiceCategory, JobStatus,
} from '@/types'

export interface CreateVendorRequest {
  userId: string
  businessName: string
  contactPerson: string
  contactEmail: string
  contactPhone: string
  serviceCategory: ServiceCategory
  description?: string
  address?: string
}

export interface RateVendorRequest {
  complaintId: string
  rating: number
  review?: string
}

export interface VendorSearchParams {
  status?: VendorStatus
  category?: ServiceCategory
  keyword?: string
  page?: number
  size?: number
}

export const vendorsApi = {
  create: (data: CreateVendorRequest) =>
    api.post<ApiResponse<Vendor>>('/vendors', data),

  list: (params: VendorSearchParams = {}) =>
    api.get<ApiResponse<PagedResponse<Vendor>>>('/vendors', { params }),

  getById: (id: string) =>
    api.get<ApiResponse<Vendor>>(`/vendors/${id}`),

  getMe: () =>
    api.get<ApiResponse<Vendor>>('/vendors/me'),

  update: (id: string, data: Partial<CreateVendorRequest>) =>
    api.put<ApiResponse<Vendor>>(`/vendors/${id}`, data),

  approve: (id: string) =>
    api.patch<ApiResponse<Vendor>>(`/vendors/${id}/approve`),

  suspend: (id: string) =>
    api.patch<ApiResponse<Vendor>>(`/vendors/${id}/suspend`),

  activate: (id: string) =>
    api.patch<ApiResponse<Vendor>>(`/vendors/${id}/activate`),

  getJobs: (id: string, params: { page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<VendorJob>>>(`/vendors/${id}/jobs`, { params }),

  updateJobStatus: (vendorId: string, jobId: string, newStatus: JobStatus) =>
    api.patch<ApiResponse<VendorJob>>(`/vendors/${vendorId}/jobs/${jobId}/status`, null, {
      params: { newStatus },
    }),

  rate: (id: string, data: RateVendorRequest) =>
    api.post<ApiResponse<VendorRating>>(`/vendors/${id}/ratings`, data),

  getRatings: (id: string, params: { page?: number; size?: number } = {}) =>
    api.get<ApiResponse<PagedResponse<VendorRating>>>(`/vendors/${id}/ratings`, { params }),
}