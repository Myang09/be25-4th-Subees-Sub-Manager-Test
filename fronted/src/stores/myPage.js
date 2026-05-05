import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { deleteProfileImage, deleteUser, getMyInfo, getProfileImage, patchPassword, patchProfile, patchProfileImageFile } from '@/api/user'
import { useAuthStore } from './auth'

const SETTINGS_KEY = 'subees-local-preferences'
const ACTIVITY_KEY = 'subees-account-activity-logs'
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

const previewProfile = { userId: null, email: 'guest@subees.app', nickname: '게스트', userState: 'PREVIEW', emailVerified: 'Y', emailVerifiedAt: '2026-03-01T09:00:00', createdAt: '2026-03-01T09:00:00' }
const defaultPreferences = { emailNotificationEnabled: true, messageNotificationEnabled: true, paymentReminderEnabled: true, monthlyInsightReportEnabled: false }

const loadPreferences = () => { try { const raw = window.localStorage.getItem(SETTINGS_KEY); return raw ? { ...defaultPreferences, ...JSON.parse(raw) } : { ...defaultPreferences } } catch (error) { return { ...defaultPreferences } } }
const savePreferences = (value) => window.localStorage.setItem(SETTINGS_KEY, JSON.stringify(value))
const loadLogs = () => { try { const raw = window.localStorage.getItem(ACTIVITY_KEY); const parsed = raw ? JSON.parse(raw) : []; return Array.isArray(parsed) ? parsed : [] } catch (error) { return [] } }
const saveLogs = (value) => window.localStorage.setItem(ACTIVITY_KEY, JSON.stringify(value))
const normalizeImageUrl = (value) => !value ? '' : /^https?:\/\//.test(value) ? value : `${API_BASE_URL}${value.startsWith('/') ? value : `/${value}`}`
const getErrorMessage = (error, fallback) => error?.response?.data?.message || error?.response?.data?.error || error?.message || fallback

export const useMyPageStore = defineStore('myPage', () => {
  const authStore = useAuthStore()
  const isLoading = ref(false)
  const isProfileSubmitting = ref(false)
  const isPasswordSubmitting = ref(false)
  const isImageSubmitting = ref(false)
  const isWithdrawing = ref(false)
  const profile = ref({ ...previewProfile })
  const profileImageUrl = ref('')
  const preferences = ref(loadPreferences())
  const activityLogs = ref(loadLogs())
  const successMessage = ref('')
  const errorMessage = ref('')

  const isLoggedIn = computed(() => authStore.isLoggedIn)
  const joinedAtLabel = computed(() => formatDate(profile.value?.createdAt))
  const verifiedAtLabel = computed(() => formatDate(profile.value?.emailVerifiedAt))
  const isEmailVerified = computed(() => profile.value?.emailVerified === 'Y')
  const summaryItems = computed(() => [
    { label: '계정 상태', value: profile.value?.userState || 'ACTIVE' },
    { label: '이메일 인증', value: isEmailVerified.value ? '완료' : '미인증' },
    { label: '가입일', value: joinedAtLabel.value },
  ])

  function clearStatus() { successMessage.value = ''; errorMessage.value = '' }
  function setSuccess(message) { successMessage.value = message; errorMessage.value = '' }
  function setError(message) { errorMessage.value = message; successMessage.value = '' }
  function addActivityLog(title, description) {
    activityLogs.value = [{ logId: Date.now(), title, description, createdAt: new Intl.DateTimeFormat('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).format(new Date()) }, ...activityLogs.value].slice(0, 8)
    saveLogs(activityLogs.value)
  }
  function formatDate(value) { if (!value) return '-'; const date = new Date(value); if (Number.isNaN(date.getTime())) return '-'; return new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(date) }

  async function hydrate() {
    clearStatus()
    if (!authStore.isLoggedIn || !authStore.userId) { profile.value = { ...previewProfile }; profileImageUrl.value = ''; return }
    isLoading.value = true
    try {
      const [infoResponse, imageResponse] = await Promise.all([getMyInfo(authStore.userId), getProfileImage(authStore.userId)])
      profile.value = infoResponse?.data?.data ?? { ...previewProfile }
      profileImageUrl.value = normalizeImageUrl(imageResponse?.data?.data?.profileImageUrl || '')
    } catch (error) {
      setError(getErrorMessage(error, '마이페이지 정보를 불러오지 못했습니다.'))
    } finally {
      isLoading.value = false
    }
  }

  async function updateProfile(payload) {
    if (!authStore.isLoggedIn || !authStore.userId) { setError('로그인 후 프로필을 수정할 수 있습니다.'); return false }
    isProfileSubmitting.value = true; clearStatus()
    try {
      const response = await patchProfile(authStore.userId, payload)
      const data = response?.data?.data
      profile.value = { ...profile.value, ...(data || {}) }
      authStore.setSession({ ...authStore.session, nickname: data?.nickname ?? payload.nickname })
      addActivityLog('프로필 수정', `${payload.nickname} 닉네임으로 프로필 정보를 업데이트했습니다.`)
      setSuccess(response?.data?.message || data?.message || '프로필 정보가 수정되었습니다.')
      return true
    } catch (error) {
      setError(getErrorMessage(error, '프로필 정보를 수정하지 못했습니다.'))
      return false
    } finally { isProfileSubmitting.value = false }
  }

  async function changePassword(payload) {
    if (!authStore.isLoggedIn || !authStore.userId) { setError('로그인 후 비밀번호를 변경할 수 있습니다.'); return false }
    isPasswordSubmitting.value = true; clearStatus()
    try {
      const response = await patchPassword(authStore.userId, payload)
      const data = response?.data?.data
      addActivityLog('비밀번호 변경', '보안 설정에서 비밀번호를 다시 설정했습니다.')
      setSuccess(response?.data?.message || data?.message || '비밀번호가 변경되었습니다.')
      return true
    } catch (error) {
      setError(getErrorMessage(error, '비밀번호를 변경하지 못했습니다.'))
      return false
    } finally { isPasswordSubmitting.value = false }
  }

  async function uploadProfileImage(file) {
    if (!authStore.isLoggedIn || !authStore.userId) { setError('로그인 후 프로필 이미지를 수정할 수 있습니다.'); return false }
    if (!file) { setError('업로드할 파일을 선택해주세요.'); return false }
    isImageSubmitting.value = true; clearStatus()
    try {
      const response = await patchProfileImageFile(authStore.userId, file)
      profileImageUrl.value = normalizeImageUrl(response?.data?.data?.profileImageUrl || '')
      addActivityLog('프로필 이미지 변경', `${file.name} 파일로 프로필 이미지를 업데이트했습니다.`)
      setSuccess('프로필 이미지가 업데이트되었습니다.')
      return true
    } catch (error) {
      setError(getErrorMessage(error, '프로필 이미지를 업로드하지 못했습니다.'))
      return false
    } finally { isImageSubmitting.value = false }
  }

  async function resetProfileImage() {
    if (!authStore.isLoggedIn || !authStore.userId) { setError('로그인 후 프로필 이미지를 수정할 수 있습니다.'); return false }
    isImageSubmitting.value = true; clearStatus()
    try {
      const response = await deleteProfileImage(authStore.userId)
      profileImageUrl.value = normalizeImageUrl(response?.data?.data?.profileImageUrl || '')
      addActivityLog('프로필 이미지 초기화', '기본 프로필 이미지로 되돌렸습니다.')
      setSuccess(response?.data?.message || '프로필 이미지가 기본 이미지로 변경되었습니다.')
      return true
    } catch (error) {
      setError(getErrorMessage(error, '프로필 이미지를 초기화하지 못했습니다.'))
      return false
    } finally { isImageSubmitting.value = false }
  }

  async function savePreferenceSettings() {
    savePreferences(preferences.value)
    addActivityLog('환경설정 저장', '알림, 리포트, 결제 리마인더 설정을 저장했습니다.')
    setSuccess('환경설정이 브라우저에 저장되었습니다.')
    return true
  }

  async function withdraw() {
    if (!authStore.isLoggedIn || !authStore.userId) { setError('로그인된 계정만 탈퇴할 수 있습니다.'); return false }
    isWithdrawing.value = true; clearStatus()
    try {
      const response = await deleteUser(authStore.userId)
      addActivityLog('회원탈퇴', '계정 비활성화를 진행했습니다.')
      setSuccess(response?.data?.message || '회원탈퇴가 완료되었습니다.')
      await authStore.logout()
      profile.value = { ...previewProfile }
      profileImageUrl.value = ''
      return true
    } catch (error) {
      setError(getErrorMessage(error, '회원탈퇴를 진행하지 못했습니다.'))
      return false
    } finally { isWithdrawing.value = false }
  }

  return { profile, profileImageUrl, preferences, activityLogs, successMessage, errorMessage, isLoading, isProfileSubmitting, isPasswordSubmitting, isImageSubmitting, isWithdrawing, isLoggedIn, joinedAtLabel, verifiedAtLabel, isEmailVerified, summaryItems, clearStatus, setError, hydrate, updateProfile, changePassword, uploadProfileImage, resetProfileImage, savePreferenceSettings, withdraw }
})
