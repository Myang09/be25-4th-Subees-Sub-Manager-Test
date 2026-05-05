import { defineStore } from 'pinia'

const initialForm = () => ({
  categoryId: null,
  itemId: null,
  itemName: '',
  customSubscriptionName: '',
  serviceIconFileName: '',
  paymentAmount: 0,
  billingCycle: '1M',
  paymentCardId: null,
  paymentCardName: '',
  customPaymentCardName: '',
  paymentStartDate: '',
  note: '',
})

export const useSubscriptionFormStore = defineStore('subscriptionForm', {
  state: () => ({
    currentStep: 1,
    subscriptionForm: initialForm(),
  }),

  getters: {
    resolvedSubscriptionName(state) {
      if (state.subscriptionForm.itemId === null) {
        return state.subscriptionForm.customSubscriptionName.trim()
      }
      return state.subscriptionForm.itemName
    },
  },

  actions: {
    goToStep(step) {
      this.currentStep = Math.min(Math.max(step, 1), 4)
    },

    nextStep() {
      this.currentStep = Math.min(this.currentStep + 1, 4)
    },

    prevStep() {
      this.currentStep = Math.max(this.currentStep - 1, 1)
    },

    // ✅ 카테고리 선택
    setCategory(categoryId) {
      this.subscriptionForm.categoryId = categoryId

      // 서비스 초기화
      this.subscriptionForm.itemId = null
      this.subscriptionForm.itemName = ''
      this.subscriptionForm.customSubscriptionName = ''
    },

    // ✅ 서비스 선택
    setSubscription(itemId, itemName, defaultAmount) {
      this.subscriptionForm.itemId = itemId
      this.subscriptionForm.itemName = itemName

      if (itemId !== null) {
        this.subscriptionForm.customSubscriptionName = ''
      }

      if (typeof defaultAmount === 'number') {
        this.subscriptionForm.paymentAmount = defaultAmount
      }
    },

    // ✅ 카드 선택
    setPaymentCardId(paymentId, paymentName) {
      this.subscriptionForm.paymentCardId = paymentId
      this.subscriptionForm.paymentCardName = paymentName

      if (paymentName !== '직접 입력') {
        this.subscriptionForm.customPaymentCardName = ''
      }
    },

    setIconFileName(fileName) {
      this.subscriptionForm.serviceIconFileName = fileName
    },

    setBillingCycle(billingCycle) {
      this.subscriptionForm.billingCycle = billingCycle
    },

    resetForm() {
      this.currentStep = 1
      this.subscriptionForm = initialForm()
    },
  },
})