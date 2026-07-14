// ─── API Response wrapper ─────────────────────────────────────────────────────

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  errorCode?: string
  timestamp: string
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
  first: boolean
}

// ─── Enums ────────────────────────────────────────────────────────────────────

export type UserRole =
  | 'SUPER_ADMIN'
  | 'SOCIETY_MANAGER'
  | 'COMMITTEE_MEMBER'
  | 'RESIDENT'
  | 'MAINTENANCE_STAFF'
  | 'VENDOR'

export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED' | 'PENDING_VERIFICATION'

export type SocietyStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'

export type SubscriptionPlan = 'BASIC' | 'PROFESSIONAL' | 'ENTERPRISE'

export type MemberStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING'

export type NoticeType =
  | 'WATER_SHUTDOWN'
  | 'MAINTENANCE'
  | 'EMERGENCY_ALERT'
  | 'SOCIETY_MEETING'
  | 'GENERAL'

export type NoticeStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'

export type ComplaintCategory =
  | 'WATER_LEAKAGE'
  | 'PLUMBING'
  | 'ELECTRICITY'
  | 'LIFT_ISSUE'
  | 'PARKING'
  | 'SECURITY'
  | 'HOUSEKEEPING'
  | 'INTERNET_ISSUE'
  | 'NOISE_COMPLAINT'
  | 'OTHER'

export type ComplaintPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type ComplaintStatus =
  | 'OPEN'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'PENDING_VERIFICATION'
  | 'CLOSED'
  | 'CANCELLED'
  | 'REOPENED'

export type TimelineAction =
  | 'CREATED'
  | 'ASSIGNED'
  | 'ACCEPTED'
  | 'WORK_STARTED'
  | 'WORK_COMPLETED'
  | 'MARKED_RESOLVED'
  | 'VERIFIED_RESOLVED'
  | 'REOPENED'
  | 'ESCALATED'
  | 'CANCELLED'
  | 'COMMENTED'

export type ServiceCategory =
  | 'PLUMBING'
  | 'ELECTRICITY'
  | 'LIFT_MAINTENANCE'
  | 'HOUSEKEEPING'
  | 'SECURITY'
  | 'CARPENTRY'
  | 'PAINTING'
  | 'PEST_CONTROL'
  | 'INTERNET'
  | 'GENERAL_MAINTENANCE'
  | 'OTHER'

export type VendorStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'PENDING_APPROVAL'

export type JobStatus = 'ASSIGNED' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export type NotificationType =
  | 'COMPLAINT_CREATED'
  | 'COMPLAINT_ASSIGNED'
  | 'COMPLAINT_ACCEPTED'
  | 'COMPLAINT_WORK_STARTED'
  | 'COMPLAINT_WORK_COMPLETED'
  | 'COMPLAINT_PENDING_VERIFICATION'
  | 'COMPLAINT_RESOLVED'
  | 'COMPLAINT_REOPENED'
  | 'COMPLAINT_ESCALATED'
  | 'COMPLAINT_CANCELLED'
  | 'NOTICE_PUBLISHED'
  | 'VENDOR_APPROVED'
  | 'GENERAL'

export type AuditAction =
  | 'LOGIN' | 'LOGOUT' | 'LOGOUT_ALL' | 'REGISTER'
  | 'PASSWORD_CHANGE' | 'PASSWORD_RESET' | 'FAILED_LOGIN'
  | 'SOCIETY_CREATE' | 'SOCIETY_UPDATE' | 'SOCIETY_ACTIVATE' | 'SOCIETY_DEACTIVATE'
  | 'MEMBER_ADD' | 'MEMBER_REMOVE' | 'MEMBER_ROLE_UPDATE'
  | 'COMPLAINT_CREATE' | 'COMPLAINT_ASSIGN' | 'COMPLAINT_RESOLVE'
  | 'COMPLAINT_CLOSE' | 'COMPLAINT_REOPEN' | 'COMPLAINT_ESCALATE'
  | 'VENDOR_REGISTER' | 'VENDOR_APPROVE' | 'VENDOR_RATED'
  | 'NOTICE_CREATE' | 'NOTICE_PUBLISH' | 'NOTICE_ARCHIVE'

// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  fullName: string
  phone?: string
  role: UserRole
  status: AccountStatus
  societyId?: string
  flatNumber?: string
  lastLoginAt?: string
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

// ─── Society ──────────────────────────────────────────────────────────────────

export interface Society {
  id: string
  name: string
  code: string
  description?: string
  addressLine1: string
  addressLine2?: string
  city: string
  state: string
  pincode: string
  country: string
  contactEmail: string
  contactPhone: string
  totalUnits: number
  totalFloors: number
  status: SocietyStatus
  subscriptionPlan: SubscriptionPlan
  subscriptionExpiry?: string
  logoUrl?: string
  createdBy: string
  totalMembers?: number
  createdAt: string
  updatedAt: string
}

export interface SocietyMember {
  id: string
  societyId: string
  societyName: string
  userId: string
  role: string
  flatNumber?: string
  block?: string
  floor?: number
  status: MemberStatus
  joinedAt: string
  leftAt?: string
  createdAt: string
}

// ─── Notice ───────────────────────────────────────────────────────────────────

export interface Notice {
  id: string
  societyId: string
  societyName: string
  title: string
  content: string
  noticeType: NoticeType
  status: NoticeStatus
  priority: boolean
  publishedAt?: string
  expiresAt?: string
  createdBy: string
  createdAt: string
  updatedAt: string
}

// ─── Complaint ────────────────────────────────────────────────────────────────

export interface ComplaintTimeline {
  id: string
  action: TimelineAction
  performedBy: string
  note?: string
  createdAt: string
}

export interface ComplaintComment {
  id: string
  complaintId: string
  authorId: string
  authorRole: string
  content: string
  createdAt: string
  updatedAt: string
}

export interface ComplaintAttachment {
  id: string
  fileUrl: string
  fileName: string
  fileType?: string
  fileSize?: number
  uploadedBy: string
  createdAt: string
}

export interface Complaint {
  id: string
  societyId: string
  residentId: string
  title: string
  description: string
  category: ComplaintCategory
  priority: ComplaintPriority
  status: ComplaintStatus
  assignedToId?: string
  assignedAt?: string
  slaDeadline: string
  slaBreached: boolean
  escalationLevel: number
  escalatedAt?: string
  reopenCount: number
  resolutionNote?: string
  resolvedAt?: string
  closedAt?: string
  location?: string
  createdAt: string
  updatedAt: string
  timeline: ComplaintTimeline[]
  comments: ComplaintComment[]
  attachments: ComplaintAttachment[]
}

// ─── Vendor ───────────────────────────────────────────────────────────────────

export interface Vendor {
  id: string
  societyId: string
  userId: string
  businessName: string
  contactPerson: string
  contactEmail: string
  contactPhone: string
  serviceCategory: ServiceCategory
  description?: string
  address?: string
  rating: number
  totalJobs: number
  completedJobs: number
  status: VendorStatus
  approvedBy?: string
  approvedAt?: string
  createdAt: string
  updatedAt: string
}

export interface VendorJob {
  id: string
  vendorId: string
  societyId: string
  complaintId: string
  complaintTitle?: string
  status: JobStatus
  assignedAt: string
  acceptedAt?: string
  startedAt?: string
  completedAt?: string
  cancelledAt?: string
  notes?: string
  createdAt: string
}

export interface VendorRating {
  id: string
  vendorId: string
  complaintId: string
  ratedBy: string
  rating: number
  review?: string
  createdAt: string
}

// ─── Notification ─────────────────────────────────────────────────────────────

export interface Notification {
  id: string
  societyId: string
  recipientId?: string
  type: NotificationType
  title: string
  message: string
  entityId?: string
  entityType?: string
  read: boolean
  readAt?: string
  createdAt: string
}

// ─── Analytics ────────────────────────────────────────────────────────────────

export interface TrendPoint {
  date: string
  created: number
  closed: number
  escalated: number
}

export interface VendorStat {
  vendorId: string
  businessName: string
  serviceCategory: string
  totalJobs: number
  completedJobs: number
  rating: number
  completionRate: number
}

export interface Dashboard {
  societyId: string
  totalComplaints: number
  openComplaints: number
  closedComplaints: number
  inProgressComplaints: number
  pendingVerificationComplaints: number
  reopenedComplaints: number
  criticalComplaints: number
  slaBreaches: number
  avgResolutionHours: number
  criticalCount: number
  highCount: number
  mediumCount: number
  lowCount: number
  slaComplianceRate: number
  residentSatisfactionRate: number
  topVendors: VendorStat[]
  complaintTrend: TrendPoint[]
}

// ─── Audit ────────────────────────────────────────────────────────────────────

export interface AuditLog {
  id: string
  societyId?: string
  userId?: string
  action: AuditAction
  entityType?: string
  entityId?: string
  description?: string
  ipAddress?: string
  createdAt: string
}

// ─── Redux Auth State ─────────────────────────────────────────────────────────

export interface AuthState {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}