import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
  getSubscription as fetchSubscriptionsApi,
  getSubscriptionDetail as fetchSubscriptionDetailApi,
  updateSubscription as updateSubscriptionApi,
  deleteSubscription as deleteSubscriptionApi,
  getSubscriptionCategory as fetchSubscriptionCategoriesApi,
  getPaymentCard as fetchPaymentCardsApi,
} from '@/api/subscription'

export const useSubscriptionsStore = defineStore('subscriptions', () => {
  const subscriptions = ref([])
  const selectedSubscription = ref(null)
  const categoryOptions = ref([])
  const paymentCards = ref([])

  const filters = ref({
    query: '',
    categoryName: '전체',
    sortBy: 'latest',
  })

  const loading = ref(false)
  const error = ref('')

  const normalizeBillingCycle = (billingCycle) => {
    if (billingCycle === '1Y' || billingCycle === 'YEARLY') return 'YEARLY'
    if (billingCycle === '1M' || billingCycle === 'MONTHLY') return 'MONTHLY'
    return billingCycle || 'MONTHLY'
  }

  const mapCardDisplayName = (card) => {
    if (!card) return ''
    if (card.cardCompany) return `${card.cardCompany}(${card.cardName})`
    return card.customCardCompany || ''
  }

  const mapSubscription = (item) => ({
    subscriptionId: item?.subscriptionId ?? null,
    categoryName: item?.categoryName ?? '',
    subscriptionName: item?.subscriptionName ?? item?.itemName ?? '',
    paymentAmount: Number(item?.paymentAmount ?? item?.price ?? 0),
    billingCycle: normalizeBillingCycle(item?.billingCycle),
    paymentCardName:
      item?.paymentCardName ??
      item?.paymentMethodName ??
      item?.paymentMethod?.name ??
      '',
    paymentId: item?.paymentId ?? item?.paymentMethod?.id ?? null,
    paymentStartDate:
      item?.paymentStartDate ??
      item?.startDate ??
      item?.registeredAt ??
      item?.createdAt ??
      '',
    nextPaymentDate: item?.nextPaymentDate ?? item?.targetDate ?? '',
    registeredAt: item?.registeredAt ?? item?.createdAt ?? '',
    note: item?.note ?? '',
    status: item?.status ?? (item?.useYn === 'Y' ? 'ACTIVE' : 'PAUSED'),
  })

  const loadAuxiliaryData = async () => {
    const [categoryResponse, cardResponse] = await Promise.all([
      fetchSubscriptionCategoriesApi(),
      fetchPaymentCardsApi(),
    ])

    categoryOptions.value = categoryResponse.data?.data ?? []
    paymentCards.value = (cardResponse.data?.data ?? []).map((card) => ({
      ...card,
      displayName: mapCardDisplayName(card),
    }))
  }

  const loadSubscriptions = async () => {
    loading.value = true
    error.value = ''

    try {
      await loadAuxiliaryData()

      const response = await fetchSubscriptionsApi()
      const list = response.data?.data ?? []

      subscriptions.value = list.map(mapSubscription)
      selectedSubscription.value = subscriptions.value[0] ?? null
    } catch (e) {
      console.error(e)
      error.value = '구독 목록을 불러오지 못했습니다.'
      subscriptions.value = []
      selectedSubscription.value = null
    } finally {
      loading.value = false
    }
  }

  const selectSubscription = async (item) => {
    try {
      const response = await fetchSubscriptionDetailApi(item.subscriptionId)
      const detail = response.data?.data ?? {}

      selectedSubscription.value = mapSubscription({
        ...item,
        ...detail,
      })
    } catch (e) {
      console.error(e)
      selectedSubscription.value = mapSubscription(item)
    }
  }

  const setQuery = (query) => {
    filters.value.query = query
  }

  const setCategory = (categoryName) => {
    filters.value.categoryName = categoryName
  }

  const setSort = (sortBy) => {
    filters.value.sortBy = sortBy
  }

  const categories = computed(() => {
    const names = subscriptions.value.map((item) => item.categoryName).filter(Boolean)
    return ['전체', ...new Set(names)]
  })

  const filteredSubscriptions = computed(() => {
    let result = [...subscriptions.value]

    if (filters.value.query.trim()) {
      const keyword = filters.value.query.trim().toLowerCase()
      result = result.filter((item) =>
        String(item.subscriptionName || '').toLowerCase().includes(keyword)
      )
    }

    if (filters.value.categoryName !== '전체') {
      result = result.filter((item) => item.categoryName === filters.value.categoryName)
    }

    if (filters.value.sortBy === 'latest') {
      result.sort((a, b) => (b.subscriptionId || 0) - (a.subscriptionId || 0))
    } else if (filters.value.sortBy === 'price-high') {
      result.sort((a, b) => (b.paymentAmount || 0) - (a.paymentAmount || 0))
    } else if (filters.value.sortBy === 'price-low') {
      result.sort((a, b) => (a.paymentAmount || 0) - (b.paymentAmount || 0))
    }

    return result
  })

  const totalCount = computed(() => subscriptions.value.length)

  const activeCount = computed(() =>
    subscriptions.value.filter((item) => item.status === 'ACTIVE').length
  )

  const pausedCount = computed(() =>
    subscriptions.value.filter((item) => item.status === 'PAUSED').length
  )

  const monthlyExpectedAmount = computed(() => {
    return subscriptions.value
      .filter((item) => item.status !== 'PAUSED')
      .reduce((sum, item) => {
        if (item.billingCycle === 'YEARLY') {
          return sum + Math.floor((item.paymentAmount || 0) / 12)
        }
        return sum + (item.paymentAmount || 0)
      }, 0)
  })

  const buildUpdatePayload = (payload) => {
    const matchedCategory = categoryOptions.value.find(
      (category) => category.categoryName === payload.categoryName
    )

    const matchedCard = paymentCards.value.find(
      (card) => card.displayName === payload.paymentCardName
    )

    if (!matchedCategory) {
      throw new Error('일치하는 카테고리를 찾을 수 없습니다.')
    }

    if (!matchedCard) {
      throw new Error('일치하는 결제 카드를 찾을 수 없습니다.')
    }

    return {
      categoryId: matchedCategory.categoryId,
      itemName: payload.subscriptionName?.trim(),
      price: Number(payload.paymentAmount || 0),
      billingCycle: payload.billingCycle === 'YEARLY' ? '1Y' : '1M',
      startDate: payload.paymentStartDate,
      paymentId: matchedCard.paymentId,
    }
  }

  const updateSubscription = async (payload) => {
    if (!payload?.subscriptionId) return

    try {
      const requestBody = buildUpdatePayload(payload)
      const response = await updateSubscriptionApi(payload.subscriptionId, requestBody)

      const updatedItem = mapSubscription({
        ...payload,
        ...requestBody,
        itemName: payload.subscriptionName,
        paymentMethod: {
          id: requestBody.paymentId,
          name: payload.paymentCardName,
        },
      })

      const index = subscriptions.value.findIndex(
        (item) => item.subscriptionId === payload.subscriptionId
      )

      if (index !== -1) {
        subscriptions.value[index] = {
          ...subscriptions.value[index],
          ...updatedItem,
        }
      }

      if (selectedSubscription.value?.subscriptionId === payload.subscriptionId) {
        selectedSubscription.value = {
          ...selectedSubscription.value,
          ...updatedItem,
        }
      }

      return response
    } catch (e) {
      console.error(e)
      throw e
    }
  }

  const deleteSubscription = async (subscriptionId) => {
    try {
      await deleteSubscriptionApi(subscriptionId)

      subscriptions.value = subscriptions.value.filter(
        (item) => item.subscriptionId !== subscriptionId
      )

      if (selectedSubscription.value?.subscriptionId === subscriptionId) {
        selectedSubscription.value = subscriptions.value[0] ?? null
      }
    } catch (e) {
      console.error(e)
      throw e
    }
  }


  return {
    subscriptions,
    selectedSubscription,
    categoryOptions,
    paymentCards,
    filters,
    categories,
    filteredSubscriptions,
    totalCount,
    activeCount,
    pausedCount,
    monthlyExpectedAmount,
    loading,
    error,
    loadSubscriptions,
    selectSubscription,
    setQuery,
    setCategory,
    setSort,
    updateSubscription,
    deleteSubscription
  }
})
