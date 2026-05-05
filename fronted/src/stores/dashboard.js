import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getPaymentAnalytics, getPaymentCalendar } from '@/api/calendar'
import http from '@/api/http'

const CATEGORY_COLORS = {
  OTT: '#BA6B52',
  Music: '#5D8260',
  AI: '#8A6A00',
  Cloud: '#C7B895',
  ETC: '#998C71',
  Etc: '#998C71',
}

const createDefaultSummary = () => ({
  nickname: '',
  monthlyExpectedAmount: 0,
  previousMonthChangeRate: 0,
  previousMonthAmount: 0,
  activeSubscriptionCount: 0,
  potentialSavingsAmount: 0,
  nextPaymentDate: '-',
  billingWindowLabel: '이번 달',
})

const createDefaultNextPayment = () => ({
  subscriptionId: null,
  subscriptionName: '예정된 결제가 없습니다',
  categoryName: '-',
  nextPaymentDate: '-',
  paymentAmount: 0,
  paymentCardName: '등록 카드 없음',
  dDay: null,
  billingCycleLabel: '-',
})

const normalizeSinglePayload = (response) => {
  const root = response?.data ?? response
  return root?.data ?? root?.items?.[0] ?? null
}

const normalizeArrayPayload = (response) => {
  const root = response?.data ?? response
  return root?.data ?? root?.items ?? []
}

const parseDateString = (value) => {
  if (!value || typeof value !== 'string') return null

  const [year, month, day] = value.split('-').map(Number)

  if (!year || !month || !day) return null

  return new Date(year, month - 1, day)
}

const startOfDate = (value = new Date()) => {
  return new Date(value.getFullYear(), value.getMonth(), value.getDate())
}

const formatCurrency = (value) => `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`
const formatShortDate = (value) => {
  const date = value instanceof Date ? value : parseDateString(value)
  if (!date) return '-'
  return `${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}`
}

const formatFullDate = (value) => {
  const date = value instanceof Date ? value : parseDateString(value)
  if (!date) return '-'

  return [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, '0'),
    String(date.getDate()).padStart(2, '0'),
  ].join('-')
}

const getBillingCycleLabel = (billingCycle) => {
  if (billingCycle === '1Y') return '매년 결제'
  return '매월 결제'
}

const createClampedDate = (year, monthIndex, day) => {
  const lastDay = new Date(year, monthIndex + 1, 0).getDate()
  return new Date(year, monthIndex, Math.min(day, lastDay))
}

const computeNextPaymentDate = (startDateValue, billingCycle, referenceDate = new Date()) => {
  const startDate = parseDateString(startDateValue)

  if (!startDate) return null

  const today = startOfDate(referenceDate)
  const startDay = startDate.getDate()

  if (billingCycle === '1Y') {
    let candidate = createClampedDate(today.getFullYear(), startDate.getMonth(), startDay)

    if (candidate < today) {
      candidate = createClampedDate(today.getFullYear() + 1, startDate.getMonth(), startDay)
    }

    return candidate
  }

  let candidate = createClampedDate(today.getFullYear(), today.getMonth(), startDay)

  if (candidate < today) {
    candidate = createClampedDate(today.getFullYear(), today.getMonth() + 1, startDay)
  }

  return candidate
}

const calculateDday = (targetDate, referenceDate = new Date()) => {
  if (!(targetDate instanceof Date)) return null

  const today = startOfDate(referenceDate)
  const diff = targetDate.getTime() - today.getTime()

  return Math.round(diff / (1000 * 60 * 60 * 24))
}

const getRelativeMonth = (offset) => {
  const today = new Date()
  const date = new Date(today.getFullYear(), today.getMonth() + offset, 1)

  return {
    year: date.getFullYear(),
    month: date.getMonth() + 1,
    key: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`,
    monthLabel: `${date.getMonth() + 1}월`,
    isCurrent: offset === 0,
  }
}

const estimatePotentialSavings = (subscriptions = []) => {
  const duplicatedMap = subscriptions.reduce((acc, item) => {
    const key = String(item.subscriptionName || '').trim().toLowerCase()

    if (!key) return acc

    if (!acc.has(key)) {
      acc.set(key, [])
    }

    acc.get(key).push(Number(item.paymentAmount || 0))
    return acc
  }, new Map())

  let total = 0

  duplicatedMap.forEach((amounts) => {
    if (amounts.length <= 1) return

    const sorted = [...amounts].sort((a, b) => b - a)
    total += sorted.slice(1).reduce((sum, amount) => sum + amount, 0)
  })

  return total
}

export const useDashboardStore = defineStore('dashboard', () => {
  const isLoading = ref(false)
  const errorMessage = ref('')
  const userSummary = ref(createDefaultSummary())
  const nextPayment = ref(createDefaultNextPayment())
  const upcomingSubscriptions = ref([])
  const categorySpendSummary = ref([])
  const monthlySpendTrend = ref([])
  const hasFetched = ref(false)

  const highestCategory = computed(() => {
    return [...categorySpendSummary.value].sort((a, b) => b.totalAmount - a.totalAmount)[0] ?? null
  })

  const totalCategoryAmount = computed(() => {
    return categorySpendSummary.value.reduce((total, item) => total + Number(item.totalAmount || 0), 0)
  })

  const averageMonthlySpend = computed(() => {
    if (!monthlySpendTrend.value.length) return 0

    const total = monthlySpendTrend.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0)
    return Math.round(total / monthlySpendTrend.value.length)
  })

  const resetState = () => {
    userSummary.value = createDefaultSummary()
    nextPayment.value = createDefaultNextPayment()
    upcomingSubscriptions.value = []
    categorySpendSummary.value = []
    monthlySpendTrend.value = []
    hasFetched.value = false
  }

  const fetchDashboard = async ({ force = false } = {}) => {
    if (hasFetched.value && !force) {
      return {
        userSummary: userSummary.value,
        nextPayment: nextPayment.value,
        upcomingSubscriptions: upcomingSubscriptions.value,
      }
    }
    const currentMonth = getRelativeMonth(0)
    const monthRange = Array.from({ length: 6 }, (_, index) => getRelativeMonth(index - 5))

    try {
      isLoading.value = true
      errorMessage.value = ''

      const [trendResponses, categoryResponse, subscriptionsResponse] = await Promise.all([
        Promise.all(
          monthRange.map((item) => getPaymentCalendar({
            year: item.year,
            month: item.month,
          })),
        ),
        getPaymentAnalytics({
          year: currentMonth.year,
          month: currentMonth.month,
          rangeType: 'MONTH',
        }),
        http.get('/api/v1/subscriptions'),
      ])

      const trendData = trendResponses.map((response, index) => {
        const normalized = normalizeSinglePayload(response)
        const meta = monthRange[index]

        return {
          key: meta.key,
          monthLabel: meta.monthLabel,
          totalAmount: Number(normalized?.monthTotalAmount || 0),
          isCurrent: meta.isCurrent,
        }
      })

      const subscriptions = normalizeArrayPayload(subscriptionsResponse)
      const subscriptionDetailsResponses = await Promise.all(
        subscriptions.map((item) => http.get(`/api/v1/subscriptions/${item.subscriptionId}`)),
      )

      const today = new Date()
      const enrichedSubscriptions = subscriptions.map((item, index) => {
        const detail = normalizeSinglePayload(subscriptionDetailsResponses[index])
        const nextPaymentDate = computeNextPaymentDate(detail?.startDate, detail?.billingCycle, today)
        const dDay = calculateDday(nextPaymentDate, today)

        return {
          subscriptionId: Number(item.subscriptionId),
          subscriptionName: item.itemName || '구독 서비스',
          categoryName: item.categoryName || '기타',
          paymentAmount: Number(detail?.price ?? item.price ?? 0),
          paymentCardName: detail?.paymentMethod?.name || '등록 카드 없음',
          billingCycle: detail?.billingCycle || '1M',
          billingCycleLabel: getBillingCycleLabel(detail?.billingCycle),
          nextPaymentDate: formatFullDate(nextPaymentDate),
          nextPaymentDateShort: formatShortDate(nextPaymentDate),
          dDay,
        }
      }).filter((item) => item.dDay != null)
        .sort((a, b) => {
          if (a.dDay !== b.dDay) return a.dDay - b.dDay
          return a.subscriptionId - b.subscriptionId
        })

      const categoryAnalytics = normalizeSinglePayload(categoryResponse)
      const categories = Array.isArray(categoryAnalytics?.categories)
        ? categoryAnalytics.categories.map((item) => ({
            categoryName: item.categoryName || '기타',
            totalAmount: Number(item.totalAmount || 0),
            ratio: Number(item.ratio || 0),
            subscriptionCount: Number(item.subscriptionCount || 0),
            colorHex: CATEGORY_COLORS[item.categoryName] || CATEGORY_COLORS.ETC,
            description: `${item.categoryName || '기타'} 카테고리 지출 비중`,
          }))
        : []

      const currentSummary = trendData[trendData.length - 1] || { totalAmount: 0 }
      const previousSummary = trendData[trendData.length - 2] || { totalAmount: 0 }
      const previousMonthAmount = Number(previousSummary.totalAmount || 0)
      const monthlyExpectedAmount = Number(currentSummary.totalAmount || 0)
      const previousMonthChangeRate = previousMonthAmount > 0
        ? Number((((monthlyExpectedAmount - previousMonthAmount) / previousMonthAmount) * 100).toFixed(1))
        : 0

      const upcomingList = enrichedSubscriptions.slice(0, 4)
      const nextUpcomingPayment = upcomingList[0] || createDefaultNextPayment()

      userSummary.value = {
        nickname: '',
        monthlyExpectedAmount,
        previousMonthChangeRate,
        previousMonthAmount,
        activeSubscriptionCount: subscriptions.length,
        potentialSavingsAmount: estimatePotentialSavings(enrichedSubscriptions),
        nextPaymentDate: nextUpcomingPayment.nextPaymentDateShort || '-',
        billingWindowLabel: `${currentMonth.year}년 ${currentMonth.month}월`,
      }

      nextPayment.value = {
        subscriptionId: nextUpcomingPayment.subscriptionId ?? null,
        subscriptionName: nextUpcomingPayment.subscriptionName || '예정된 결제가 없습니다',
        categoryName: nextUpcomingPayment.categoryName || '-',
        nextPaymentDate: nextUpcomingPayment.nextPaymentDate || '-',
        paymentAmount: Number(nextUpcomingPayment.paymentAmount || 0),
        paymentCardName: nextUpcomingPayment.paymentCardName || '등록 카드 없음',
        dDay: nextUpcomingPayment.dDay,
        billingCycleLabel: nextUpcomingPayment.billingCycleLabel || '-',
      }

      upcomingSubscriptions.value = enrichedSubscriptions
      categorySpendSummary.value = categories
      monthlySpendTrend.value = trendData
      hasFetched.value = true

      return {
        userSummary: userSummary.value,
        nextPayment: nextPayment.value,
        upcomingSubscriptions: upcomingSubscriptions.value,
      }
    } catch (error) {
      console.error('dashboard fetch 실패:', error)
      errorMessage.value = '대시보드 데이터를 불러오지 못했습니다.'
      resetState()
      throw error
    } finally {
      isLoading.value = false
    }
  }

  return {
    isLoading,
    errorMessage,
    userSummary,
    nextPayment,
    upcomingSubscriptions,
    categorySpendSummary,
    monthlySpendTrend,
    hasFetched,
    highestCategory,
    totalCategoryAmount,
    averageMonthlySpend,
    fetchDashboard,
    resetState,
    formatCurrency,
  }
})
