import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getPaymentCalendar,
  getPaymentDateDetails,
  getPaymentAnalytics,
  getMonthlyPaymentList,
} from '@/api/calendar'

const WEEKDAY_LABELS = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']

const CATEGORY_META = {
  OTT: { displayName: 'OTT', colorClass: 'bg-[#F2A6A6]' },
  Music: { displayName: '음악', colorClass: 'bg-[#F7C66C]' },
  AI: { displayName: 'AI', colorClass: 'bg-[#8EC5FC]' },
  Cloud: { displayName: '클라우드', colorClass: 'bg-[#A8E6CF]' },
  Etc: { displayName: '기타', colorClass: 'bg-[#CDB4DB]' },
  Others: { displayName: '기타', colorClass: 'bg-[#CDB4DB]' },
}

const formatMonthLabel = (year, month) => `${year}년 ${month}월`
const formatShortMonthLabel = (year, month) => `${month}월`

const formatDateKey = (year, month, day) => {
  const mm = String(month).padStart(2, '0')
  const dd = String(day).padStart(2, '0')
  return `${year}-${mm}-${dd}`
}

const formatDateLabel = (dateKey) => {
  if (!dateKey) return '날짜를 선택해주세요'

  const [year, month, day] = dateKey.split('-')
  return `${year}년 ${Number(month)}월 ${Number(day)}일`
}

const normalizeSinglePayload = (response) => {
  const root = response?.data ?? response
  return root?.data ?? root?.items?.[0] ?? null
}

const normalizeArrayPayload = (response) => {
  const root = response?.data ?? response
  return root?.data ?? root?.items ?? []
}

const countItemNames = (itemNames = '') =>
  String(itemNames)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean).length

const getDaysInMonth = (year, month) => new Date(year, month, 0).getDate()

const clampDay = (year, month, day) =>
  Math.min(Math.max(Number(day || 1), 1), getDaysInMonth(year, month))

const buildSelectedDateKey = (year, month, day) =>
  formatDateKey(year, month, clampDay(year, month, day))

const buildCalendarMatrix = (year, month, items, selectedDateKey) => {
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)

  const firstWeekday = firstDay.getDay()
  const daysInMonth = lastDay.getDate()
  const prevMonthLastDate = new Date(year, month - 1, 0).getDate()

  const summaryMap = new Map()

  ;(items || []).forEach((item) => {
    summaryMap.set(Number(item.payDay), {
      payDay: Number(item.payDay),
      totalCount: Number(item.totalCount || 0),
      totalAmount: Number(item.totalAmount || 0),
      itemNames: item.itemNames || '',
    })
  })

  const result = []

  for (let i = 0; i < firstWeekday; i += 1) {
    const prevDay = prevMonthLastDate - firstWeekday + i + 1
    const prevDate = new Date(year, month - 2, prevDay)

    result.push({
      dateKey: formatDateKey(
        prevDate.getFullYear(),
        prevDate.getMonth() + 1,
        prevDate.getDate(),
      ),
      label: prevDay,
      isCurrentMonth: false,
      isSelected: false,
      totalCount: 0,
      totalAmount: 0,
      itemNames: '',
    })
  }

  for (let day = 1; day <= daysInMonth; day += 1) {
    const matched = summaryMap.get(day)
    const dateKey = formatDateKey(year, month, day)

    result.push({
      dateKey,
      label: day,
      isCurrentMonth: true,
      isSelected: selectedDateKey === dateKey,
      totalCount: matched?.totalCount || 0,
      totalAmount: matched?.totalAmount || 0,
      itemNames: matched?.itemNames || '',
    })
  }

  const remain = result.length % 7 === 0 ? 0 : 7 - (result.length % 7)

  for (let day = 1; day <= remain; day += 1) {
    const nextDate = new Date(year, month, day)

    result.push({
      dateKey: formatDateKey(
        nextDate.getFullYear(),
        nextDate.getMonth() + 1,
        nextDate.getDate(),
      ),
      label: day,
      isCurrentMonth: false,
      isSelected: false,
      totalCount: 0,
      totalAmount: 0,
      itemNames: '',
    })
  }

  return result
}

export const usePaymentCalendarStore = defineStore('paymentCalendar', () => {
  const now = new Date()
  const todayYear = now.getFullYear()
  const todayMonth = now.getMonth() + 1
  const todayDay = now.getDate()

  const currentYear = ref(todayYear)
  const currentMonth = ref(todayMonth)
  const selectedDateKey = ref(
    buildSelectedDateKey(todayYear, todayMonth, todayDay),
  )

  const calendarSummary = ref({
    year: todayYear,
    month: todayMonth,
    monthTotalAmount: 0,
    items: [],
  })

  const previousCalendarSummary = ref({
    year: todayYear,
    month: todayMonth - 1,
    monthTotalAmount: 0,
    items: [],
  })

  const categoryAnalytics = ref({
    year: todayYear,
    month: todayMonth,
    rangeType: 'MONTH',
    totalAmount: 0,
    categories: [],
  })

  const yearlyAnalytics = ref({
    year: todayYear,
    month: todayMonth,
    rangeType: 'YEAR',
    totalAmount: 0,
    categories: [],
  })

  const currentMonthPaymentSource = ref([])
  const previousMonthPaymentSource = ref([])
  const selectedDateDetailList = ref([])

  const isLoading = ref(false)
  const errorMessage = ref('')
  const trendView = ref('MONTHLY')

  const weekdayLabels = ref(WEEKDAY_LABELS)

  const currentMonthLabel = computed(() =>
    formatMonthLabel(currentYear.value, currentMonth.value),
  )

  const currentMonthTitle = computed(() =>
    formatMonthLabel(currentYear.value, currentMonth.value),
  )

  const currentMonthShortLabel = computed(() =>
    formatShortMonthLabel(currentYear.value, currentMonth.value),
  )

  const monthTotalAmount = computed(() =>
    Number(calendarSummary.value?.monthTotalAmount || 0),
  )

  const calendarItems = computed(() => calendarSummary.value?.items || [])

  const calendarDays = computed(() =>
    buildCalendarMatrix(
      currentYear.value,
      currentMonth.value,
      calendarItems.value,
      selectedDateKey.value,
    ),
  )

  const selectedDateLabel = computed(() =>
    formatDateLabel(selectedDateKey.value),
  )

  const selectedDatePayments = computed(() => {
    return (selectedDateDetailList.value || []).map((item, index) => {
      const rawCardName =
        item.customCardCompany ||
        item.cardCompany ||
        item.cardName ||
        ''

      return {
        id:
          item.subscriptionId ||
          item.id ||
          `${selectedDateKey.value}-${index}`,
        name:
          item.itemName ||
          item.subscriptionName ||
          item.serviceName ||
          item.name ||
          '구독 서비스',
        amount: Number(item.price || item.amount || item.paymentAmount || 0),
        cardCompany: rawCardName || '카드',
        cardLabel: rawCardName
          ? `${rawCardName}로 결제 예정`
          : '등록 카드로 결제 예정',
      }
    })
  })

  const selectedDateAmount = computed(() =>
    selectedDatePayments.value.reduce(
      (sum, item) => sum + Number(item.amount || 0),
      0,
    ),
  )

  const currentMonthCategorySummary = computed(() => {
    const categories = categoryAnalytics.value?.categories || []

    const normalized = categories.map((item) => {
      const meta = CATEGORY_META[item.categoryName] || {
        displayName: item.categoryName,
        colorClass: 'bg-[#D9D9D9]',
      }

      const itemNames = item.itemNames || ''

      return {
        categoryName: item.categoryName,
        displayName: meta.displayName,
        colorClass: meta.colorClass,
        totalAmount: Number(item.totalAmount || 0),
        itemNames,
        subscriptionCount:
          item.subscriptionCount != null
            ? Number(item.subscriptionCount)
            : countItemNames(itemNames),
      }
    })

    const total = normalized.reduce(
      (sum, item) => sum + Number(item.totalAmount || 0),
      0,
    )

    return normalized
      .map((item) => ({
        ...item,
        ratio:
          total > 0
            ? Number(((item.totalAmount / total) * 100).toFixed(1))
            : 0,
      }))
      .sort((a, b) => b.totalAmount - a.totalAmount)
  })

  const topCategoryTotalAmount = computed(() =>
    currentMonthCategorySummary.value.reduce(
      (sum, item) => sum + Number(item.totalAmount || 0),
      0,
    ),
  )

  const dominantCategory = computed(() => {
    if (!currentMonthCategorySummary.value.length) return null
    return currentMonthCategorySummary.value[0]
  })

  const yearlyCategorySummary = computed(() => {
    const categories = yearlyAnalytics.value?.categories || []
    const total = categories.reduce(
      (sum, item) => sum + Number(item.totalAmount || 0),
      0,
    )

    return categories
      .map((item) => {
        const meta = CATEGORY_META[item.categoryName] || {
          displayName: item.categoryName,
          colorClass: 'bg-[#D9D9D9]',
        }

        return {
          categoryName: item.categoryName,
          displayName: meta.displayName,
          colorClass: meta.colorClass,
          totalAmount: Number(item.totalAmount || 0),
          subscriptionCount: Number(item.subscriptionCount || 0),
          ratio:
            total > 0
              ? Number(
                  ((Number(item.totalAmount || 0) / total) * 100).toFixed(1),
                )
              : 0,
        }
      })
      .sort((a, b) => b.totalAmount - a.totalAmount)
  })

  const currentMonthPaymentList = computed(() => {
    return (currentMonthPaymentSource.value || [])
      .map((item, index) => ({
        paymentId: item.paymentId || `subscription-${index}`,
        subscriptionName: item.subscriptionName || item.itemName || '구독 서비스',
        categoryName: item.categoryName || '',
        paymentCardName:
          item.paymentCardName ||
          item.paymentMethodName ||
          item.cardName ||
          item.cardCompany ||
          item.customCardCompany ||
          '등록 카드',
        paymentAmount: Number(item.paymentAmount || item.price || 0),
        displayDateLabel: item.displayDateLabel || '매월 구독',
        isYearly: Boolean(item.isYearly),
      }))
      .sort((a, b) => b.paymentAmount - a.paymentAmount)
  })

  const previousMonthPaymentList = computed(() => {
    return (previousMonthPaymentSource.value || [])
      .map((item, index) => ({
        paymentId: item.paymentId || `prev-subscription-${index}`,
        subscriptionName: item.subscriptionName || item.itemName || '구독 서비스',
        paymentAmount: Number(item.paymentAmount || item.price || 0),
      }))
      .sort((a, b) => b.paymentAmount - a.paymentAmount)
  })

  const activeTrendItems = computed(() => {
    if (trendView.value === 'YEARLY') {
      return yearlyCategorySummary.value.map((item) => ({
        key: item.categoryName,
        label: item.displayName,
        totalAmount: Number(item.totalAmount || 0),
        subscriptionCount: Number(item.subscriptionCount || 0),
      }))
    }

    return currentMonthPaymentList.value.map((item) => ({
      key: item.paymentId,
      label: item.subscriptionName,
      totalAmount: Number(item.paymentAmount || 0),
      categoryName: item.categoryName || '',
      paymentCardName: item.paymentCardName || '',
    }))
  })

  const maxTrendAmount = computed(() => {
    if (!activeTrendItems.value.length) return 1
    return Math.max(
      ...activeTrendItems.value.map((item) => Number(item.totalAmount || 0)),
    )
  })

  const trendTotalAmount = computed(() => {
    if (trendView.value === 'YEARLY') {
      return Number(yearlyAnalytics.value?.totalAmount || 0)
    }

    return currentMonthPaymentList.value.reduce(
      (sum, item) => sum + Number(item.paymentAmount || 0),
      0,
    )
  })

  const previousTrendTotalAmount = computed(() => {
    if (trendView.value === 'YEARLY') return null

    return previousMonthPaymentList.value.reduce(
      (sum, item) => sum + Number(item.paymentAmount || 0),
      0,
    )
  })

  const setTrendView = async (view) => {
    trendView.value = view

    if (view === 'YEARLY' && !yearlyAnalytics.value?.categories?.length) {
      await fetchYearlyAnalytics()
    }
  }

  const fetchCalendar = async () => {
    const response = await getPaymentCalendar({
      year: currentYear.value,
      month: currentMonth.value,
    })

    const normalized = normalizeSinglePayload(response)

    calendarSummary.value = normalized || {
      year: currentYear.value,
      month: currentMonth.value,
      monthTotalAmount: 0,
      items: [],
    }
  }

  const fetchPreviousCalendar = async () => {
    const previousDate = new Date(currentYear.value, currentMonth.value - 2, 1)
    const previousYear = previousDate.getFullYear()
    const previousMonth = previousDate.getMonth() + 1

    const response = await getPaymentCalendar({
      year: previousYear,
      month: previousMonth,
    })

    const normalized = normalizeSinglePayload(response)

    previousCalendarSummary.value = normalized || {
      year: previousYear,
      month: previousMonth,
      monthTotalAmount: 0,
      items: [],
    }
  }

  const fetchAnalytics = async () => {
    const response = await getPaymentAnalytics({
      year: currentYear.value,
      month: currentMonth.value,
      rangeType: 'MONTH',
    })

    const normalized = normalizeSinglePayload(response)

    categoryAnalytics.value = {
      year: normalized?.year ?? currentYear.value,
      month: normalized?.month ?? currentMonth.value,
      rangeType: normalized?.rangeType ?? 'MONTH',
      totalAmount: Number(normalized?.totalAmount || 0),
      categories: normalized?.categories ?? [],
    }
  }

  const fetchYearlyAnalytics = async () => {
    const response = await getPaymentAnalytics({
      year: currentYear.value,
      month: currentMonth.value,
      rangeType: 'YEAR',
    })

    const normalized = normalizeSinglePayload(response)

    yearlyAnalytics.value = {
      year: normalized?.year ?? currentYear.value,
      month: normalized?.month ?? currentMonth.value,
      rangeType: normalized?.rangeType ?? 'YEAR',
      totalAmount: Number(normalized?.totalAmount || 0),
      categories: normalized?.categories ?? [],
    }
  }

  const fetchCurrentMonthPaymentList = async () => {
    const response = await getMonthlyPaymentList({
      year: currentYear.value,
      month: currentMonth.value,
    })

    const normalized = normalizeArrayPayload(response)
    currentMonthPaymentSource.value = normalized || []
  }

  const fetchPreviousMonthPaymentList = async () => {
    const previousDate = new Date(currentYear.value, currentMonth.value - 2, 1)

    const response = await getMonthlyPaymentList({
      year: previousDate.getFullYear(),
      month: previousDate.getMonth() + 1,
    })

    const normalized = normalizeArrayPayload(response)
    previousMonthPaymentSource.value = normalized || []
  }

  const fetchSelectedDateDetails = async () => {
    if (!selectedDateKey.value) {
      selectedDateDetailList.value = []
      return
    }

    const [, , date] = selectedDateKey.value.split('-')

    const response = await getPaymentDateDetails({
      year: currentYear.value,
      month: currentMonth.value,
      date: Number(date),
    })

    const normalized = normalizeArrayPayload(response)
    selectedDateDetailList.value = normalized || []
  }

  const safeRun = async (fn, fallback) => {
    try {
      await fn()
    } catch (error) {
      console.error('paymentCalendar 개별 조회 실패:', error)
      fallback?.()
    }
  }

  const fetchAll = async () => {
    isLoading.value = true
    errorMessage.value = ''

    await Promise.allSettled([
      safeRun(fetchCalendar, () => {
        calendarSummary.value = {
          year: currentYear.value,
          month: currentMonth.value,
          monthTotalAmount: 0,
          items: [],
        }
      }),
      safeRun(fetchPreviousCalendar, () => {
        previousCalendarSummary.value = {
          year: currentYear.value,
          month: currentMonth.value,
          monthTotalAmount: 0,
          items: [],
        }
      }),
      safeRun(fetchAnalytics, () => {
        categoryAnalytics.value = {
          year: currentYear.value,
          month: currentMonth.value,
          rangeType: 'MONTH',
          totalAmount: 0,
          categories: [],
        }
      }),
      safeRun(fetchYearlyAnalytics, () => {
        yearlyAnalytics.value = {
          year: currentYear.value,
          month: currentMonth.value,
          rangeType: 'YEAR',
          totalAmount: 0,
          categories: [],
        }
      }),
      safeRun(fetchCurrentMonthPaymentList, () => {
        currentMonthPaymentSource.value = []
      }),
      safeRun(fetchPreviousMonthPaymentList, () => {
        previousMonthPaymentSource.value = []
      }),
    ])

    await safeRun(fetchSelectedDateDetails, () => {
      selectedDateDetailList.value = []
    })

    isLoading.value = false
  }

  const setSelectedDate = async (dateKey) => {
    const [year, month] = dateKey.split('-').map(Number)

    if (year !== currentYear.value || month !== currentMonth.value) return

    selectedDateKey.value = dateKey

    try {
      isLoading.value = true
      await fetchSelectedDateDetails()
    } catch (error) {
      console.error('선택 날짜 상세 조회 실패:', error)
      selectedDateDetailList.value = []
    } finally {
      isLoading.value = false
    }
  }

  const moveMonth = async (offset) => {
    const next = new Date(
      currentYear.value,
      currentMonth.value - 1 + offset,
      1,
    )

    currentYear.value = next.getFullYear()
    currentMonth.value = next.getMonth() + 1
    selectedDateKey.value = buildSelectedDateKey(
      currentYear.value,
      currentMonth.value,
      1,
    )

    await fetchAll()
  }

  return {
    currentYear,
    currentMonth,
    selectedDateKey,
    isLoading,
    errorMessage,
    weekdayLabels,
    currentMonthLabel,
    currentMonthTitle,
    currentMonthShortLabel,
    monthTotalAmount,
    topCategoryTotalAmount,
    dominantCategory,
    currentMonthCategorySummary,
    yearlyCategorySummary,
    calendarDays,
    selectedDatePayments,
    selectedDateAmount,
    selectedDateLabel,
    activeTrendItems,
    maxTrendAmount,
    trendTotalAmount,
    previousTrendTotalAmount,
    trendView,
    currentMonthPaymentList,
    setTrendView,
    setSelectedDate,
    fetchCalendar,
    fetchAnalytics,
    fetchSelectedDateDetails,
    fetchAll,
    moveMonth,
  }
})