export const STORAGE_KEY = 'subees-auth-session'

const parseJson = (value) => {
  try {
    return JSON.parse(value)
  } catch (error) {
    return null
  }
}

export const loadSession = () => {
  if (typeof window === 'undefined') return null

  const raw = window.localStorage.getItem(STORAGE_KEY)
  return raw ? parseJson(raw) : null
}

export const saveSession = (session) => {
  if (typeof window === 'undefined') return

  if (!session) {
    window.localStorage.removeItem(STORAGE_KEY)
    return
  }

  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

const decodeBase64Url = (value) => {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
  const decoded = window.atob(padded)

  return decodeURIComponent(
    Array.from(decoded)
      .map((character) => `%${character.charCodeAt(0).toString(16).padStart(2, '0')}`)
      .join(''),
  )
}

export const getTokenExpirationTime = (accessToken) => {
  if (!accessToken || typeof window === 'undefined') {
    return null
  }

  try {
    const [, payload] = accessToken.split('.')
    if (!payload) return null

    const decodedPayload = JSON.parse(decodeBase64Url(payload))
    return typeof decodedPayload.exp === 'number' ? decodedPayload.exp * 1000 : null
  } catch (error) {
    return null
  }
}

export const isExpiredTokenError = (error) => {
  const status = error?.response?.status
  const message = error?.response?.data?.message

  return status === 401 && typeof message === 'string' && message.includes('토큰이 만료되었습니다')
}

let expiryAlertOpen = false

export const handleSessionExpired = () => {
  if (typeof window === 'undefined' || expiryAlertOpen) {
    return
  }

  expiryAlertOpen = true
  saveSession(null)

  const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`
  const loginUrl = new URL('/login', window.location.origin)

  if (window.location.pathname !== '/login') {
    loginUrl.searchParams.set('redirect', currentPath)
  }

  window.alert('로그인 세션이 만료되었습니다. 다시 로그인해주세요.')

  if (window.location.pathname !== '/login' || window.location.search !== loginUrl.search) {
    window.location.replace(`${loginUrl.pathname}${loginUrl.search}`)
    return
  }

  expiryAlertOpen = false
}
