import { useEffect, useRef, useCallback } from 'react'
import { Client, type StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { storage } from '@/lib/utils'
import type { Notification } from '@/types'

const WS_URL = import.meta.env.VITE_WS_URL ?? '/ws'

interface UseWebSocketOptions {
  societyId?: string
  userId?: string
  onNotification?: (notification: Notification) => void
  enabled?: boolean
}

export function useWebSocket({
  societyId,
  userId,
  onNotification,
  enabled = true,
}: UseWebSocketOptions) {
  const clientRef      = useRef<Client | null>(null)
  const subscriptionsRef = useRef<StompSubscription[]>([])

  const disconnect = useCallback(() => {
    subscriptionsRef.current.forEach((sub) => {
      try { sub.unsubscribe() } catch { /* ignore */ }
    })
    subscriptionsRef.current = []

    if (clientRef.current?.active) {
      clientRef.current.deactivate()
    }
    clientRef.current = null
  }, [])

  useEffect(() => {
    if (!enabled || !societyId || !userId) return

    const token = storage.get('accessToken') as string | null
    if (!token) return

    const client = new Client({
      // SockJS transport with JWT token as query param
      webSocketFactory: () =>
        new SockJS(`${WS_URL}?token=${token}`) as WebSocket,

      connectHeaders: { Authorization: `Bearer ${token}` },

      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      reconnectDelay:    5000,

      onConnect: () => {
        // 1. Subscribe to personal notifications
        const personalSub = client.subscribe(
          `/user/${userId}/queue/notifications`,
          (message) => {
            try {
              const notification = JSON.parse(message.body) as Notification
              onNotification?.(notification)
            } catch { /* malformed message */ }
          },
        )

        // 2. Subscribe to society-wide broadcasts
        const societySub = client.subscribe(
          `/topic/society/${societyId}`,
          (message) => {
            try {
              const notification = JSON.parse(message.body) as Notification
              onNotification?.(notification)
            } catch { /* malformed message */ }
          },
        )

        subscriptionsRef.current = [personalSub, societySub]
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers.message)
      },

      onDisconnect: () => {
        subscriptionsRef.current = []
      },
    })

    client.activate()
    clientRef.current = client

    return () => { disconnect() }
  }, [enabled, societyId, userId, onNotification, disconnect])

  return { disconnect }
}