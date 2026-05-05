import axios from 'axios'
import { handleSessionExpired, isExpiredTokenError, loadSession } from '@/utils/authSession'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.request.use((config) => {
  try {
    const session = loadSession()
    const accessToken = session?.accessToken
    const grantType = session?.grantType ?? 'Bearer'

    if (accessToken) {
      config.headers.Authorization = `${grantType} ${accessToken}`
    }
  } catch (error) {
    // 세션 파싱 실패 시 기본 요청 진행
  }

  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (isExpiredTokenError(error)) {
      handleSessionExpired()
    }

    return Promise.reject(error)
  },
)

export default http
