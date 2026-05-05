import http from './http'

export const getSystemHealth = () => http.get('/api/v1/system/health')
export const getSystemInfo = () => http.get('/api/v1/system/info')
export const getSystemTime = () => http.get('/api/v1/system/time')
