import http from './http'

export const getNotifications = () => http.get('/api/v1/notifications')
export const readNotification = (notificationId) => http.patch(`/api/v1/notifications/${notificationId}/read`)
export const closeNotification = (notificationId) => http.patch(`/api/v1/notifications/${notificationId}/close`)
