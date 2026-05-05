import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import {
  deleteRecommend,
  getRecommendDetail,
  getRecommendList,
  postRecommendGenerate,
  postRecommendSave,
  postRecommendSubmit,
} from '@/api/recommend'
import { getSubscriptionCategory, getSubscriptionItemByCategory } from '@/api/subscription'

const DRAFT_KEY = 'subees-ai-recommend-draft-v4'
const ACTIVE_REPORT_KEY = 'subees-ai-recommend-active-v4'
const LEGACY_STORAGE_KEYS = [DRAFT_KEY, ACTIVE_REPORT_KEY]

const FALLBACK_CATEGORY_OPTIONS = ['OTT', 'AI', 'Music', 'Others']
const FALLBACK_SERVICE_LIBRARY = [
  { name: 'Netflix', category: 'OTT', monthlyPrice: 0, description: '' },
  { name: 'Tving', category: 'OTT', monthlyPrice: 0, description: '' },
  { name: 'Disney+', category: 'OTT', monthlyPrice: 0, description: '' },
  { name: 'CoupangPlay', category: 'OTT', monthlyPrice: 0, description: '' },
  { name: 'Watcha', category: 'OTT', monthlyPrice: 0, description: '' },
  { name: 'Laftel', category: 'OTT', monthlyPrice: 0, description: '' },
  { name: 'Wave', category: 'OTT', monthlyPrice: 0, description: '' },
  { name: 'AppleTv', category: 'OTT', monthlyPrice: 0, description: '' },
  { name: 'ChatGpt', category: 'AI', monthlyPrice: 0, description: '' },
  { name: 'Gemini', category: 'AI', monthlyPrice: 0, description: '' },
  { name: 'Claude', category: 'AI', monthlyPrice: 0, description: '' },
  { name: 'Melon', category: 'Music', monthlyPrice: 0, description: '' },
  { name: 'AppleMusic', category: 'Music', monthlyPrice: 0, description: '' },
  { name: 'Spotify', category: 'Music', monthlyPrice: 0, description: '' },
  { name: 'YoutubeMusic', category: 'Music', monthlyPrice: 0, description: '' },
  { name: 'Flo', category: 'Music', monthlyPrice: 0, description: '' },
  { name: 'Genie', category: 'Music', monthlyPrice: 0, description: '' },
  { name: 'Vibe', category: 'Music', monthlyPrice: 0, description: '' },
  { name: '배민클럽', category: 'Others', monthlyPrice: 0, description: '' },
  { name: '카카오톡서랍', category: 'Others', monthlyPrice: 0, description: '' },
  { name: '유튜브프리미엄', category: 'Others', monthlyPrice: 0, description: '' },
  { name: '쿠팡와우', category: 'Others', monthlyPrice: 0, description: '' },
  { name: '이모티콘플러스', category: 'Others', monthlyPrice: 0, description: '' },
  { name: '인텔리제이', category: 'Others', monthlyPrice: 0, description: '' },
  { name: 'Icloud+', category: 'Others', monthlyPrice: 0, description: '' },
  { name: '컬리', category: 'Others', monthlyPrice: 0, description: '' },
  { name: '네이버멤버십', category: 'Others', monthlyPrice: 0, description: '' },
  { name: 'GoogleDrive', category: 'Others', monthlyPrice: 0, description: '' },
  { name: 'Microsoft 365 Personal', category: 'Others', monthlyPrice: 0, description: '' },
  { name: 'Notion', category: 'Others', monthlyPrice: 0, description: '' },
  { name: 'Adobe', category: 'Others', monthlyPrice: 0, description: '' },
]

const DEFAULT_DRAFT = {
  reportTitle: '',
  requestNote: '',
  maxMonthlyBudget: 0,
  mandatoryItems: [],
  optionalItems: [],
  subscriptionItems: [],
}

const clone = (value) => JSON.parse(JSON.stringify(value))
const pad = (value) => String(value).padStart(2, '0')
const createId = (prefix) => `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`
const toNumber = (value) => Number(value || 0) || 0
const normalizeText = (value) => String(value || '').trim()
const normalizeList = (list) => [...new Set((Array.isArray(list) ? list : []).map(normalizeText).filter(Boolean))]

const loadJson = (key, fallback) => {
  try {
    const raw = window.localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

const saveJson = (key, value) => {
  try {
    window.localStorage.setItem(key, JSON.stringify(value))
  } catch {
    // ignore storage errors
  }
}

const removeStorageItem = (key) => {
  try {
    window.localStorage.removeItem(key)
  } catch {
    // ignore storage errors
  }
}

const getScopedStorageKey = (key, scope) => `${key}:${scope || 'guest'}`

const toDateLabel = (value) => {
  const date = value instanceof Date ? value : new Date(value)
  return Number.isNaN(date.getTime()) ? '-' : `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}`
}

const normalizeSubscriptionItems = (items = []) =>
  items
    .map((item, index) => ({
      itemId: item.itemId || createId('item'),
      serviceName: normalizeText(item.serviceName),
      category: normalizeText(item.category) || '기타',
      monthlyPrice: toNumber(item.monthlyPrice),
      description: normalizeText(item.description),
      sortOrder: toNumber(item.sortOrder) || index + 1,
    }))
    .filter((item) => item.serviceName)

const totalPriceOf = (items = []) => items.reduce((sum, item) => sum + toNumber(item.monthlyPrice), 0)

const parseItemsJson = (value) => {
  try {
    return normalizeList(JSON.parse(value || '[]'))
  } catch {
    return []
  }
}

const normalizeReport = (payload = {}) => ({
  reportId: payload.reportId ?? null,
  reportTitle: normalizeText(payload.reportTitle),
  requestNote: normalizeText(payload.requestNote),
  generatedContent: normalizeText(payload.generatedContent),
  totalMonthlyPrice: toNumber(payload.totalMonthlyPrice),
  maxMonthlyBudget: toNumber(payload.maxMonthlyBudget),
  mandatoryItemsJson:
    typeof payload.mandatoryItemsJson === 'string'
      ? payload.mandatoryItemsJson
      : JSON.stringify(normalizeList(payload.mandatoryItems || [])),
  optionalItemsJson:
    typeof payload.optionalItemsJson === 'string'
      ? payload.optionalItemsJson
      : JSON.stringify(normalizeList(payload.optionalItems || [])),
  reportStatus: normalizeText(payload.reportStatus) || 'SUBMITTED',
  createdAt: payload.createdAt || new Date().toISOString(),
  updatedAt: payload.updatedAt || payload.createdAt || new Date().toISOString(),
  subscriptionItems: normalizeSubscriptionItems(payload.subscriptionItems || []),
})

const toSubmitPayload = (draft = {}) => ({
  reportTitle: normalizeText(draft.reportTitle),
  requestNote: normalizeText(draft.requestNote),
  maxMonthlyBudget: toNumber(draft.maxMonthlyBudget),
  mandatoryItems: normalizeList(draft.mandatoryItems),
  optionalItems: normalizeList(draft.optionalItems),
  subscriptionItems: normalizeSubscriptionItems(draft.subscriptionItems).map((item) => ({
    serviceName: item.serviceName,
    category: item.category,
    monthlyPrice: item.monthlyPrice,
    description: item.description,
  })),
})

const getErrorMessage = (error, fallback) =>
  error?.response?.data?.message || error?.response?.data?.error || error?.message || fallback

export const useAiRecommendationsStore = defineStore('aiRecommendations', () => {
  const authStore = useAuthStore()

  const storageScope = computed(() => (authStore.userId ? `user-${authStore.userId}` : 'guest'))

  const draft = ref(clone(DEFAULT_DRAFT))
  const reports = ref([])
  const activeReportId = ref(null)
  const statusMessage = ref('')
  const errorMessage = ref('')
  const isFetchingList = ref(false)
  const isFetchingDetail = ref(false)
  const isSubmitting = ref(false)

  const categoryOptions = ref([...FALLBACK_CATEGORY_OPTIONS])
  const serviceLibrary = ref([...FALLBACK_SERVICE_LIBRARY])
  const isCatalogLoading = ref(false)
  const catalogLoaded = ref(false)

  const reportList = computed(() =>
    [...reports.value].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)),
  )

  const currentReport = computed(
    () => reportList.value.find((item) => String(item.reportId) === String(activeReportId.value)) || null,
  )

  const generatedReports = computed(() =>
    reportList.value.filter((item) => item.reportStatus === 'GENERATED'),
  )

  const savedReports = computed(() =>
    reportList.value.filter((item) => item.reportStatus === 'SAVED'),
  )

  const draftTotalPrice = computed(() => totalPriceOf(draft.value.subscriptionItems || []))

  const persistDraft = () => {
    saveJson(getScopedStorageKey(DRAFT_KEY, storageScope.value), draft.value)
  }

  const persistActive = () => {
    saveJson(getScopedStorageKey(ACTIVE_REPORT_KEY, storageScope.value), activeReportId.value)
  }

  const clearMessages = () => {
    statusMessage.value = ''
    errorMessage.value = ''
  }

  const resetScopedState = (scope = storageScope.value) => {
    draft.value = {
      ...clone(DEFAULT_DRAFT),
      ...loadJson(getScopedStorageKey(DRAFT_KEY, scope), {}),
    }
    activeReportId.value = loadJson(getScopedStorageKey(ACTIVE_REPORT_KEY, scope), null)
    reports.value = []
    clearMessages()
  }

  const clearLegacyStorage = () => {
    LEGACY_STORAGE_KEYS.forEach((key) => removeStorageItem(key))
  }

  const clearDraftState = () => {
    draft.value = clone(DEFAULT_DRAFT)
    persistDraft()
  }

  const clearActiveReport = () => {
    activeReportId.value = null
    persistActive()
  }

  const clearRecommendationSession = () => {
    clearDraftState()
    clearActiveReport()
  }

  const setStatusMessage = (message = '') => {
    statusMessage.value = message
    errorMessage.value = ''
  }

  const setErrorMessage = (message = '') => {
    errorMessage.value = message
    statusMessage.value = ''
  }

  const updateDraft = (payload = {}) => {
    draft.value = {
      ...draft.value,
      ...payload,
      reportTitle: normalizeText(payload.reportTitle ?? draft.value.reportTitle),
      requestNote: normalizeText(payload.requestNote ?? draft.value.requestNote),
      maxMonthlyBudget: toNumber(payload.maxMonthlyBudget ?? draft.value.maxMonthlyBudget),
      mandatoryItems: payload.mandatoryItems
        ? normalizeList(payload.mandatoryItems)
        : normalizeList(draft.value.mandatoryItems),
      optionalItems: payload.optionalItems
        ? normalizeList(payload.optionalItems)
        : normalizeList(draft.value.optionalItems),
      subscriptionItems: Array.isArray(payload.subscriptionItems)
        ? normalizeSubscriptionItems(payload.subscriptionItems)
        : normalizeSubscriptionItems(draft.value.subscriptionItems),
    }

    persistDraft()
  }

  const resetDraft = () => {
    clearDraftState()
    setStatusMessage('입력 중인 추천 조건을 초기화했습니다.')
  }

  const upsertReport = (payload = {}) => {
    const normalized = normalizeReport(payload)
    reports.value = [
      normalized,
      ...reports.value.filter((item) => String(item.reportId) !== String(normalized.reportId)),
    ]
    return normalized
  }

  const setActiveReport = (reportId) => {
    activeReportId.value = reportId
    persistActive()
  }

  const getReportById = (reportId) =>
    reports.value.find((item) => String(item.reportId) === String(reportId)) || null

  const fetchReportDetail = async (reportId) => {
    if (!reportId) return null

    isFetchingDetail.value = true

    try {
      const response = await getRecommendDetail(reportId)
      const data = response?.data?.data ?? {}
      const report = upsertReport(data)
      setActiveReport(report.reportId)
      return report
    } catch (error) {
      setErrorMessage(getErrorMessage(error, '추천 상세 정보를 불러오지 못했습니다.'))
      throw error
    } finally {
      isFetchingDetail.value = false
    }
  }

  const ensureReportDetail = async (reportId) => {
    const cached = getReportById(reportId)

    if (cached?.subscriptionItems?.length || cached?.generatedContent) {
      setActiveReport(reportId)
      return cached
    }

    return fetchReportDetail(reportId)
  }

  const fetchSavedReports = async () => {
    isFetchingList.value = true
    clearMessages()

    try {
      const response = await getRecommendList()
      const items = response?.data?.data?.reports ?? []

      if (!items.length) {
        reports.value = reports.value.filter((item) => item.reportStatus !== 'SAVED')
        return []
      }

      const details = await Promise.all(
        items.map(async (item) => {
          try {
            const detailResponse = await getRecommendDetail(item.reportId)
            return normalizeReport(detailResponse?.data?.data ?? item)
          } catch {
            return normalizeReport(item)
          }
        }),
      )

      const nonSaved = reports.value.filter((item) => item.reportStatus !== 'SAVED')
      reports.value = [
        ...details,
        ...nonSaved.filter(
          (item) => !details.some((detail) => String(detail.reportId) === String(item.reportId)),
        ),
      ]

      return details
    } catch (error) {
      setErrorMessage(getErrorMessage(error, '추천 기록 목록을 불러오지 못했습니다.'))
      throw error
    } finally {
      isFetchingList.value = false
    }
  }

  const submitDraft = async () => {
    isSubmitting.value = true
    clearMessages()

    try {
      const payload = toSubmitPayload(draft.value)
      const response = await postRecommendSubmit(payload)
      const reportId = response?.data?.data?.reportId
      const report = await fetchReportDetail(reportId)
      setStatusMessage('추천 요청을 저장했습니다.')
      return report
    } catch (error) {
      setErrorMessage(getErrorMessage(error, '추천 요청 저장에 실패했습니다.'))
      throw error
    } finally {
      isSubmitting.value = false
    }
  }

  const generateRecommendations = async (reportId = activeReportId.value) => {
    if (!reportId) return null

    isSubmitting.value = true
    clearMessages()

    try {
      await postRecommendGenerate({ reportId: toNumber(reportId) })
      const report = await fetchReportDetail(reportId)
      setStatusMessage('추천 결과를 생성했습니다.')
      return report
    } catch (error) {
      setErrorMessage(getErrorMessage(error, '추천 결과 생성에 실패했습니다.'))
      throw error
    } finally {
      isSubmitting.value = false
    }
  }

  const saveReport = async (reportId = activeReportId.value, overrides = {}) => {
    if (!reportId) return null

    isSubmitting.value = true
    clearMessages()

    try {
      const target = await ensureReportDetail(reportId)

      await postRecommendSave({
        reportId: toNumber(reportId),
        reportTitle: normalizeText(overrides.reportTitle || target?.reportTitle),
        generatedContent: normalizeText(overrides.generatedContent || target?.generatedContent),
      })

      const report = await fetchReportDetail(reportId)
      setStatusMessage('추천 결과를 저장했습니다.')
      return report
    } catch (error) {
      setErrorMessage(getErrorMessage(error, '추천 결과 저장에 실패했습니다.'))
      throw error
    } finally {
      isSubmitting.value = false
    }
  }

  const updateReportTitle = async (reportId, reportTitle) => {
    const target = await ensureReportDetail(reportId)
    return saveReport(reportId, {
      reportTitle,
      generatedContent: target?.generatedContent,
    })
  }

  const fetchSubscriptionCatalog = async () => {
    if (catalogLoaded.value || isCatalogLoading.value) {
      return {
        categories: categoryOptions.value,
        services: serviceLibrary.value,
      }
    }

    isCatalogLoading.value = true

    try {
      const categoryResponse = await getSubscriptionCategory()
      const categories = Array.isArray(categoryResponse?.data?.data)
        ? categoryResponse.data.data
        : []

      if (!categories.length) {
        categoryOptions.value = [...FALLBACK_CATEGORY_OPTIONS]
        serviceLibrary.value = [...FALLBACK_SERVICE_LIBRARY]
        catalogLoaded.value = true

        return {
          categories: categoryOptions.value,
          services: serviceLibrary.value,
        }
      }

      categoryOptions.value = categories
        .map((item) => normalizeText(item.categoryName))
        .filter(Boolean)

      const itemResponses = await Promise.all(
        categories.map(async (category) => {
          try {
            const response = await getSubscriptionItemByCategory(category.categoryId)
            const items = Array.isArray(response?.data?.data) ? response.data.data : []

            return items.map((item) => ({
              name: normalizeText(item.itemName),
              category: normalizeText(category.categoryName),
              monthlyPrice: 0,
              description: '',
            }))
          } catch {
            return []
          }
        }),
      )

      const deduped = []
      const seen = new Set()

      itemResponses.flat().forEach((item) => {
        if (!item.name) return
        const key = `${item.category}::${item.name}`
        if (seen.has(key)) return
        seen.add(key)
        deduped.push(item)
      })

      serviceLibrary.value = deduped.length ? deduped : [...FALLBACK_SERVICE_LIBRARY]
      catalogLoaded.value = true

      return {
        categories: categoryOptions.value,
        services: serviceLibrary.value,
      }
    } catch {
      categoryOptions.value = [...FALLBACK_CATEGORY_OPTIONS]
      serviceLibrary.value = [...FALLBACK_SERVICE_LIBRARY]
      catalogLoaded.value = true

      return {
        categories: categoryOptions.value,
        services: serviceLibrary.value,
      }
    } finally {
      isCatalogLoading.value = false
    }
  }

  clearLegacyStorage()
  resetScopedState()

  watch(storageScope, (nextScope, previousScope) => {
    if (nextScope === previousScope) return
    resetScopedState(nextScope)
  })

  const deleteReportById = async (reportId) => {
    if (!reportId) return false

    isSubmitting.value = true
    clearMessages()

    try {
      await deleteRecommend(reportId)
      reports.value = reports.value.filter((item) => String(item.reportId) !== String(reportId))

      if (String(activeReportId.value) === String(reportId)) {
        activeReportId.value = reports.value[0]?.reportId || null
        persistActive()
      }

      setStatusMessage('추천 결과를 삭제했습니다.')
      return true
    } catch (error) {
      setErrorMessage(getErrorMessage(error, '추천 결과 삭제에 실패했습니다.'))
      throw error
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    draft,
    reports,
    activeReportId,
    statusMessage,
    errorMessage,
    isFetchingList,
    isFetchingDetail,
    isSubmitting,
    CATEGORY_OPTIONS: categoryOptions,
    SERVICE_LIBRARY: serviceLibrary,
    isCatalogLoading,
    catalogLoaded,
    reportList,
    currentReport,
    generatedReports,
    savedReports,
    draftTotalPrice,
    clearMessages,
    setStatusMessage,
    setErrorMessage,
    updateDraft,
    resetDraft,
    clearDraftState,
    clearActiveReport,
    clearRecommendationSession,
    submitDraft,
    generateRecommendations,
    saveReport,
    updateReportTitle,
    deleteReport: deleteReportById,
    fetchSubscriptionCatalog,
    fetchSavedReports,
    fetchReportDetail,
    ensureReportDetail,
    setActiveReport,
    getReportById,
    parseItemsJson,
    toDateLabel,
  }
})