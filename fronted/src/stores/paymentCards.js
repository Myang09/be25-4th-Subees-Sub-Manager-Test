import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getCards as getCardsApi,
  createCard as createCardApi,
  updateCard as updateCardApi,
  deleteCard as deleteCardApi,
} from '@/api/card'

const extractError = (error, fallbackMessage) => {
  const responseData = error?.response?.data

  return {
    code: responseData?.code || error?.response?.status || 500,
    message:
      responseData?.message ||
      responseData?.error ||
      fallbackMessage,
  }
}

export const usePaymentCardsStore = defineStore('paymentCards', () => {
  const cards = ref([])
  const selectedCardId = ref(null)

  const cardCount = computed(() => cards.value.length)

  const fetchCards = async () => {
    try {
      const response = await getCardsApi()
      cards.value = response.data.data ?? []
      return response.data
    } catch (error) {
      throw extractError(error, '카드 목록을 불러오는 중 오류가 발생했습니다.')
    }
  }

  const addCard = async (payload) => {
    try {
      const response = await createCardApi(payload)
      return response.data
    } catch (error) {
      throw extractError(error, '카드 등록 중 오류가 발생했습니다.')
    }
  }

  const updateCard = async ({ paymentId, ...payload }) => {
    try {
      const response = await updateCardApi(paymentId, payload)
      return response.data
    } catch (error) {
      throw extractError(error, '카드 수정 중 오류가 발생했습니다.')
    }
  }

  const deleteCard = async (paymentId) => {
    try {
      const response = await deleteCardApi(paymentId)
      return response.data
    } catch (error) {
      throw extractError(error, '카드 삭제 중 오류가 발생했습니다.')
    }
  }

  const selectCard = (paymentId) => {
    selectedCardId.value = paymentId
  }

  return {
    cards,
    cardCount,
    selectedCardId,
    fetchCards,
    addCard,
    updateCard,
    deleteCard,
    selectCard,
  }
})