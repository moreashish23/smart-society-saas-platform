import { useState, useCallback, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { notificationsApi } from '@/api/analytics.api'
import { useWebSocket } from './useWebSocket'
import { useAuth } from './useAuth'
import type { Notification } from '@/types'

export function useNotifications() {
  const { user, societyId, isAuthenticated } = useAuth()
  const queryClient = useQueryClient()
  const [liveNotifications, setLiveNotifications] = useState<Notification[]>([])

  // ── REST: unread count ────────────────────────────────────────────────────

  const { data: unreadData } = useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn:  () => notificationsApi.getUnreadCount().then((r) => r.data.data),
    enabled:  isAuthenticated,
    refetchInterval: 30000,  // poll every 30s as fallback
  })

  // ── REST: notification list ───────────────────────────────────────────────

  const { data: notificationsData } = useQuery({
    queryKey: ['notifications', 'list'],
    queryFn:  () => notificationsApi.list({ size: 20 }).then((r) => r.data.data),
    enabled:  isAuthenticated,
  })

  // ── Mutations ─────────────────────────────────────────────────────────────

  const markReadMutation = useMutation({
    mutationFn: (id: string) => notificationsApi.markRead(id),
    onSuccess:  () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
    },
  })

  const markAllReadMutation = useMutation({
    mutationFn: () => notificationsApi.markAllRead(),
    onSuccess:  () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
    },
  })

  // ── WebSocket handler ─────────────────────────────────────────────────────

  const handleLiveNotification = useCallback((notification: Notification) => {
    setLiveNotifications((prev) => [notification, ...prev.slice(0, 19)])

    // Show toast for important types
    const toastTypes: Notification['type'][] = [
      'COMPLAINT_ASSIGNED',
      'COMPLAINT_PENDING_VERIFICATION',
      'COMPLAINT_REOPENED',
      'COMPLAINT_ESCALATED',
      'NOTICE_PUBLISHED',
    ]

    if (toastTypes.includes(notification.type)) {
      toast(notification.title, {
        icon: '🔔',
        duration: 5000,
        style: { maxWidth: '360px' },
      })
    }

    // Invalidate unread count
    queryClient.invalidateQueries({ queryKey: ['notifications'] })
  }, [queryClient])

  useWebSocket({
    societyId:      societyId ?? undefined,
    userId:         user?.id,
    onNotification: handleLiveNotification,
    enabled:        isAuthenticated,
  })

  const unreadCount   = unreadData?.unreadCount ?? 0
  const notifications = notificationsData?.content ?? []

  return {
    notifications,
    liveNotifications,
    unreadCount,
    markRead:    (id: string) => markReadMutation.mutate(id),
    markAllRead: () => markAllReadMutation.mutate(),
  }
}