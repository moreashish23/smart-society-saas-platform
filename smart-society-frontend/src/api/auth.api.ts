import api from './axios'
import type { ApiResponse, AuthResponse, User } from '@/types'

export interface LoginRequest {
  email: string
  password: string
  deviceInfo?: string
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  phone?: string
  role: string
  societyId?: string
  flatNumber?: string
}

export interface ForgotPasswordRequest { email: string }
export interface ResetPasswordRequest  { token: string; newPassword: string; confirmPassword: string }
export interface ChangePasswordRequest { currentPassword: string; newPassword: string; confirmPassword: string }

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<ApiResponse<AuthResponse>>('/auth/login', data),

  register: (data: RegisterRequest) =>
    api.post<ApiResponse<User>>('/auth/register', data),

  logout: (refreshToken: string) =>
    api.post<ApiResponse<void>>('/auth/logout', { refreshToken }),

  logoutAll: () =>
    api.post<ApiResponse<void>>('/auth/logout-all'),

  forgotPassword: (data: ForgotPasswordRequest) =>
    api.post<ApiResponse<void>>('/auth/forgot-password', data),

  resetPassword: (data: ResetPasswordRequest) =>
    api.post<ApiResponse<void>>('/auth/reset-password', data),

  changePassword: (data: ChangePasswordRequest) =>
    api.post<ApiResponse<void>>('/auth/change-password', data),

  getProfile: () =>
    api.get<ApiResponse<User>>('/auth/me'),
}