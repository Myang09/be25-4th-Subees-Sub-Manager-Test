import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { postLogin, postLogout } from '@/api/auth'
import {
  getTokenExpirationTime,
  handleSessionExpired,
  loadSession,
  saveSession,
} from '@/utils/authSession'

let expirationTimerId = null

const clearExpirationTimer = () => {
  if (expirationTimerId) {
    window.clearTimeout(expirationTimerId)
    expirationTimerId = null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const session = ref(loadSession())
  const isSubmitting = ref(false)

  const isLoggedIn = computed(() => Boolean(session.value?.accessToken))
  const nickname = computed(() => session.value?.nickname ?? '')
  const userId = computed(() => session.value?.userId ?? null)
  const accessToken = computed(() => session.value?.accessToken ?? '')

  const scheduleSessionExpiration = (nextSession) => {
    clearExpirationTimer()

    const expiresAt = getTokenExpirationTime(nextSession?.accessToken)
    if (!expiresAt) {
      return
    }

    const remainingTime = expiresAt - Date.now()

    if (remainingTime <= 0) {
      session.value = null
      saveSession(null)
      handleSessionExpired()
      return
    }

    expirationTimerId = window.setTimeout(() => {
      session.value = null
      saveSession(null)
      handleSessionExpired()
    }, remainingTime)
  }

  const setSession = (payload) => {
    session.value = payload
    saveSession(payload)
    scheduleSessionExpiration(payload)
  }

  const login = async (payload) => {
    isSubmitting.value = true
    try {
      const response = await postLogin(payload)
      const authData = response?.data?.data ?? {}

      const nextSession = {
        grantType: authData.grantType ?? 'Bearer',
        accessToken: authData.accessToken ?? '',
        userId: authData.userId ?? null,
        nickname: authData.nickname ?? payload.email.split('@')[0],
      }

      setSession(nextSession)
      return nextSession
    } finally {
      isSubmitting.value = false
    }
  }

  const logout = async () => {
    try {
      if (isLoggedIn.value) {
        await postLogout()
      }
    } catch (error) {
      // 로컬 세션 정리는 항상 수행
    } finally {
      setSession(null)
    }
  }

  scheduleSessionExpiration(session.value)

  return {
    session,
    isSubmitting,
    isLoggedIn,
    nickname,
    userId,
    accessToken,
    setSession,
    login,
    logout,
  }
})
