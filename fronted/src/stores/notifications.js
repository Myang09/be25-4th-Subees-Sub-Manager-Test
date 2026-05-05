import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  getNotifications,
  readNotification as readNotificationRequest,
  closeNotification as closeNotificationRequest,
} from '@/api/notifications'

const normalizeArrayPayload = (response) => {
  const root = response?.data ?? response
  return root?.data ?? root?.items ?? []
}

const isReadValue = (value) => value === true || value === 'Y' || value === '1' || value === 1

const mapNotification = (item = {}) => ({
  notificationId: Number(item.notificationId ?? 0),
  title: item.title ?? '결제 알림',
  message: item.content ?? '',
  content: item.content ?? '',
  dday: item.dday ?? '',
  read: isReadValue(item.isRead),
  type: 'PAYMENT',
  ctaLabel: '결제 캘린더 보기',
  ctaTo: '/calendar',
})

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref([])
  const unreadOnly = ref(false)
  const isLoading = ref(false)
  const errorMessage = ref('')
  const hasFetched = ref(false)

  const resetState = () => {
    notifications.value = []
    unreadOnly.value = false
    isLoading.value = false
    errorMessage.value = ''
    hasFetched.value = false
  }

  const sortedNotifications = computed(() => {
    return [...notifications.value].sort((a, b) => {
      if (a.read !== b.read) return Number(a.read) - Number(b.read)
      return Number(b.notificationId) - Number(a.notificationId)
    })
  })

  const unreadCount = computed(() => notifications.value.filter((item) => !item.read).length)
  const visibleNotifications = computed(() => {
    return unreadOnly.value
      ? sortedNotifications.value.filter((item) => !item.read)
      : sortedNotifications.value
  })

  const fetchNotifications = async ({ force = false } = {}) => {
    if (isLoading.value) return notifications.value
    if (hasFetched.value && !force) return notifications.value

    try {
      isLoading.value = true
      errorMessage.value = ''

      const response = await getNotifications()
      const items = normalizeArrayPayload(response)

      notifications.value = Array.isArray(items)
        ? items.map(mapNotification)
        : []
      hasFetched.value = true

      return notifications.value
    } catch (error) {
      console.error('notifications fetch 실패:', error)
      errorMessage.value = '알림을 불러오지 못했습니다.'
      notifications.value = []
      hasFetched.value = false
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const ensureFetched = async () => {
    if (!hasFetched.value) {
      await fetchNotifications()
    }
  }

  const toggleUnreadOnly = () => {
    unreadOnly.value = !unreadOnly.value
  }

  const markAsRead = async (notificationId) => {
    const target = notifications.value.find((item) => item.notificationId === notificationId)

    if (!target || target.read) return

    try {
      await readNotificationRequest(notificationId)
      notifications.value = notifications.value.map((item) => {
        return item.notificationId === notificationId
          ? { ...item, read: true }
          : item
      })
    } catch (error) {
      console.error('notification read 실패:', error)
      throw error
    }
  }

  const closeNotification = async (notificationId) => {
    try {
      await closeNotificationRequest(notificationId)
      notifications.value = notifications.value.filter((item) => item.notificationId !== notificationId)
    } catch (error) {
      console.error('notification close 실패:', error)
      throw error
    }
  }

  const markAllRead = async () => {
    const unreadItems = notifications.value.filter((item) => !item.read)

    if (!unreadItems.length) return

    await Promise.all(unreadItems.map((item) => readNotificationRequest(item.notificationId)))

    notifications.value = notifications.value.map((item) => ({
      ...item,
      read: true,
    }))
  }

  return {
    notifications,
    unreadOnly,
    isLoading,
    errorMessage,
    hasFetched,
    unreadCount,
    sortedNotifications,
    visibleNotifications,
    fetchNotifications,
    resetState,
    ensureFetched,
    toggleUnreadOnly,
    markAsRead,
    closeNotification,
    markAllRead,
  }
})
