<script setup>
import { computed, ref, onMounted } from 'vue'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppShell from '@/components/layout/AppShell.vue'
import { useAuthStore } from '@/stores/auth'
import { useSubscriptionFormStore } from '@/stores/subscriptionForm'
import { usePaymentCardsStore } from '@/stores/paymentCards'
import { createSubscription } from '@/api/subscription'
import AppAsset from '@/components/ui/AppAsset.vue'
import {
  getSubscriptionCategory,
  getSubscriptionItemByCategory,
  getPaymentCard,
} from '@/api/subscription'
import { useNotificationsStore } from '@/stores/notifications'

const authStore = useAuthStore()
const router = useRouter()
const subscriptionFormStore = useSubscriptionFormStore()
const paymentCardsStore = usePaymentCardsStore()
const notificationsStore = useNotificationsStore()

const { currentStep, subscriptionForm, resolvedSubscriptionName } = storeToRefs(subscriptionFormStore)

const showCardPicker = ref(false)
const showCardDropdown = ref(false)
const steps = [1, 2, 3, 4]
const categories = ref([])
const services = ref([])
const cardOptions = ref([])
const showSuccessModal = ref(false)
const previewImage = ref(null)
const categoryErrorMessage = ref('')
const serviceErrorMessage = ref('')
const paymentAmountErrorMessage = ref('')
const billingCycleErrorMessage = ref('')
const paymentCardErrorMessage = ref('')
const paymentStartDateErrorMessage = ref('')
const shouldResetOnLeave = ref(false)

const requestPayload = computed(() => ({
  categoryId: subscriptionForm.value.categoryId,
  itemId: subscriptionForm.value.itemId,
  itemName: null,
  paymentId: subscriptionForm.value.paymentCardId,
  price: Number(subscriptionForm.value.paymentAmount),
  billingCycle: subscriptionForm.value.billingCycle,
  startDate: subscriptionForm.value.paymentStartDate,
}))

onMounted(async () => {
  const categoryRes = await getSubscriptionCategory()
  categories.value = categoryRes.data.data

  const cardRes = await getPaymentCard()
  console.log('카드 응답 전체:', cardRes)
  console.log('카드 응답 data:', cardRes.data)

  const rawCards = cardRes.data?.data ?? cardRes.data ?? []

  cardOptions.value = rawCards.map((card) => ({
    paymentId: card.paymentId ?? card.cardId ?? card.id,
    paymentCardName: card.paymentCardName ?? card.cardName ?? card.name,
  }))

  console.log('최종 cardOptions:', cardOptions.value)

  if (subscriptionForm.value.categoryId) {
    try {
      const serviceRes = await getSubscriptionItemByCategory(subscriptionForm.value.categoryId)
      services.value = serviceRes.data?.data ?? []
    } catch (error) {
      services.value = []
    }
  }
})

const resetWizardState = () => {
  subscriptionFormStore.resetForm()
  previewImage.value = null
  showCardPicker.value = false
  showCardDropdown.value = false
  categoryErrorMessage.value = ''
  serviceErrorMessage.value = ''
  paymentAmountErrorMessage.value = ''
  billingCycleErrorMessage.value = ''
  paymentCardErrorMessage.value = ''
  paymentStartDateErrorMessage.value = ''
  services.value = []
}

onBeforeRouteLeave(() => {
  if (!shouldResetOnLeave.value) {
    return true
  }

  resetWizardState()
  shouldResetOnLeave.value = false
  return true
})

const billingCycles = [
  { label: '매월 결제', value: '1M' },
  { label: '매년 결제', value: '1Y' },
]

const selectService = (service) => {
  serviceErrorMessage.value = ''
  subscriptionFormStore.setSubscription(
    service.itemId,
    service.itemName,
    service.price,
  )
}

const progressPercent = computed(() => `${(currentStep.value / 4) * 100}%`)

const prompt = computed(() => {
  if (currentStep.value === 1) return '어떤 카테고리의 구독을 추가할까요?'
  if (currentStep.value === 2) return '어떤 서비스를 구독할지 선택하세요.'
  if (currentStep.value === 3) return '결제 정보를 입력해주세요'
  return '마지막으로 내용을 확인할게요.'
})

const stepGuide = computed(() => {
  if (currentStep.value === 1) return 'OTT, 음악, AI처럼 먼저 큰 종류를 고르면 다음 단계 서비스 목록이 바로 정리됩니다.'
  if (currentStep.value === 2) return '자주 쓰는 서비스가 보이면 바로 선택해주세요.'
  if (currentStep.value === 3) return '금액, 주기, 카드, 시작일만 채우면 등록에 필요한 핵심 정보는 끝납니다.'
  return '마지막 확인에서 이름·금액·카드·결제일만 다시 보고 저장하면 됩니다.'
})

const selectedCategory = computed(() => {
  return categories.value.find(
    (item) => item.categoryId === subscriptionForm.value.categoryId,
  ) || {}
})

const selectedCategoryLabel = computed(() => selectedCategory.value.categoryName || '')

const selectedServiceLabel = computed(() => {
  const found = services.value.find(
    (item) => item.itemId === subscriptionForm.value.itemId,
  )
  return found?.itemName || ''
})

const serviceInitial = computed(() => selectedServiceLabel.value.trim().slice(0, 1).toUpperCase() || '?')
const serviceOptions = computed(() => services.value)

const isStepValid = computed(() => {
  if (currentStep.value === 1) return Boolean(subscriptionForm.value.categoryId)
  if (currentStep.value === 2) {
    return Boolean(subscriptionForm.value.itemId)
  }
  if (currentStep.value === 3) {
    return Boolean(
      subscriptionForm.value.paymentAmount &&
      subscriptionForm.value.billingCycle &&
      subscriptionForm.value.paymentStartDate &&
      ((subscriptionForm.value.paymentCardName !== '직접 입력' && subscriptionForm.value.paymentCardName) ||
        (subscriptionForm.value.paymentCardName === '직접 입력' && subscriptionForm.value.customPaymentCardName.trim())),
    )
  }
  return true
})

const formatCurrency = (value) => `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`

const selectCategory = async (category) => {
  categoryErrorMessage.value = ''
  subscriptionFormStore.setCategory(category.categoryId)

  const res = await getSubscriptionItemByCategory(category.categoryId)
  services.value = res.data.data
}

const pickCard = (card) => {
  paymentCardErrorMessage.value = ''
  subscriptionFormStore.setPaymentCardId(card.paymentId)
  subscriptionForm.value.paymentCardName = card.paymentCardName
  showCardDropdown.value = false
  showCardPicker.value = false
}

const handleIconFileChange = (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  subscriptionFormStore.setIconFileName(file.name)

  const reader = new FileReader()
  reader.onload = (e) => {
    previewImage.value = e.target.result
  }
  reader.readAsDataURL(file)
}

const clearIconFile = () => {
  subscriptionFormStore.setIconFileName('')
  previewImage.value = null
}

const goNext = () => {
  if (currentStep.value === 1) {
    if (!subscriptionForm.value.categoryId) {
      categoryErrorMessage.value = '카테고리를 선택해주세요.'
      return
    }
    categoryErrorMessage.value = ''
  }

  if (currentStep.value === 2) {
    if (!subscriptionForm.value.itemId) {
      serviceErrorMessage.value = '구독 서비스를 선택해주세요.'
      return
    }
    serviceErrorMessage.value = ''
  }

  if (currentStep.value === 3) {
    let hasError = false

    paymentAmountErrorMessage.value = ''
    billingCycleErrorMessage.value = ''
    paymentCardErrorMessage.value = ''
    paymentStartDateErrorMessage.value = ''

    if (
      subscriptionForm.value.paymentAmount === '' ||
      subscriptionForm.value.paymentAmount === null ||
      subscriptionForm.value.paymentAmount === undefined
    ) {
      paymentAmountErrorMessage.value = '결제 금액을 입력해주세요.'
      hasError = true
    } else if (Number(subscriptionForm.value.paymentAmount) <= 0) {
      paymentAmountErrorMessage.value = '결제 금액은 0원보다 커야 합니다.'
      hasError = true
    }

    if (!subscriptionForm.value.billingCycle) {
      billingCycleErrorMessage.value = '결제 주기를 선택해주세요.'
      hasError = true
    }

    if (!subscriptionForm.value.paymentCardId) {
      paymentCardErrorMessage.value = '결제 카드를 선택해주세요.'
      hasError = true
    }

    if (!subscriptionForm.value.paymentStartDate) {
      paymentStartDateErrorMessage.value = '결제 시작일을 선택해주세요.'
      hasError = true
    }

    if (hasError) return
  }

  subscriptionFormStore.nextStep()
}

const submitDraft = async () => {
  try {
    console.log('보낼 payload:', requestPayload.value)

    const response = await createSubscription(requestPayload.value)

    try {
      await notificationsStore.fetchNotifications({ force: true })
    } catch (notificationError) {
      console.error('알림 갱신 실패:', notificationError)
    }

    console.log('등록 성공:', response.data)
    shouldResetOnLeave.value = true
    showSuccessModal.value = true
  } catch (error) {
    console.error('등록 실패:', error)
    console.error('응답 데이터:', error.response?.data)
    window.alert(error.response?.data?.message || '구독 등록에 실패했습니다.')
  }
}

const closeSuccessModal = () => {
  showSuccessModal.value = false
  router.push('/subscriptions')
}
</script>


<template>
  <AppShell title="hidden">
    <section class="wizard mx-auto w-full max-w-[1500px]">
      <div class="mb-5 rounded-card border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.92)] px-7 py-6 shadow-soft">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <p class="text-base font-bold text-neutral-900">{{ authStore.nickname || '사용자' }}님, 구독·결제·캘린더를 한 곳에 정리했어요!</p>
            <p class="mt-1 body-copy">카테고리 선택부터 결제 정보 입력까지 순서대로 등록할 수 있습니다.</p>
          </div>
          <div class="flex flex-wrap gap-2">
            <RouterLink to="/calendar" class="chip-button">캘린더</RouterLink>
            <RouterLink to="/subscriptions/new" class="chip-button is-selected">구독추가</RouterLink>
            <RouterLink to="/dashboard" class="chip-button">대시보드</RouterLink>
          </div>
        </div>
      </div>

      <div class="wizard__top">
        <div class="wizard__step">{{ currentStep }}/4</div>
        <div class="utility-gauge"><span :style="{ width: progressPercent }"></span></div>
      </div>

      <div class="wizard__body shell-card">
        <div class="wizard__heading">{{ prompt }}</div>
        <p class="page-description mt-2 !max-w-3xl">자주 쓰는 항목부터 차례대로 선택하면 빠르게 등록할 수 있어요.</p>
        <div class="guide-card mt-4 !rounded-[24px] !p-4">
          <p class="text-xs font-extrabold uppercase tracking-[0.12em] text-neutral-300">등록 안내</p>
          <p class="mt-2 text-sm font-bold text-neutral-900">{{ stepGuide }}</p>
        </div>
      

        <div v-if="currentStep === 1" class="wizard__block">
          <div class="wizard__selection">
            <div class="wizard__selection-label">현재 선택한 종류</div>
            <div class="wizard__selection-value">{{ selectedCategoryLabel }}</div>
            <div class="text-sm text-neutral-500">선택한 카테고리는 다음 단계 서비스 목록에 바로 반영돼요.</div>
          </div>

          <p v-if="categoryErrorMessage" class="wizard__error-message">
            {{ categoryErrorMessage }}
          </p>
          <div class="wizard__tile-grid">
            <button
              v-for="category in categories"
              :key="category.categoryId"
              type="button"
              class="wizard__tile"
              :class="{ 'wizard__tile--active': subscriptionForm.categoryId === category.categoryId }"
              @click="selectCategory(category)"
              >
              <div v-if="subscriptionForm.categoryId === category.categoryId" class="wizard__tile-check">선택됨</div>

              <div class="wizard__tile-image">
                <AppAsset
                  type="category"
                  :value="category.categoryName"
                  fallback="grid"
                  :size="72"
                  wrapper-class="inline-flex items-center justify-center"
                  image-class="h-32 w-32 object-contain lg:h-36 lg:w-36 xl:h-40 xl:w-40"
                  icon-class="text-[#8A6A00]"
                />
              </div>

<div class="wizard__tile-title">{{ category.categoryName }}</div>
<div class="text-sm text-neutral-500">{{ category.description }}</div>
            </button>
          </div>
        </div>

        <div v-else-if="currentStep === 2" class="wizard__block">
          
          <div class="wizard__selection">
          <div class="wizard__selection-label">현재 선택한 서비스</div>
          <div class="wizard__selection-value">
            {{ selectedServiceLabel || '서비스를 선택해주세요.' }}
          </div>
          <div class="text-sm text-neutral-500">
            선택한 서비스가 다음 단계와 최종 확인 화면에 반영돼요.
          </div>
        </div>
        <p v-if="serviceErrorMessage" class="wizard__error-message">
          {{ serviceErrorMessage }}
        </p>

          <div class="wizard__chip-grid">
            <button
              v-for="service in serviceOptions"
              :key="service.itemId"
              type="button"
              class="wizard__service-chip"
              :class="{ 'wizard__service-chip--active': subscriptionForm.itemId === service.itemId }"
              @click="selectService(service)"
            >
              <span class="wizard__service-chip-icon">
                <AppAsset
                  type="service"
                  :value="service.itemName"
                  fallback="sparkles"
                  :size="16"
                  wrapper-class="inline-flex items-center justify-center"
                  image-class="h-11 w-11 object-contain"
                  icon-class="text-[#8A6A00]"
                />
              </span>

              <span>{{ service.itemName }}</span>
            </button>
          </div>


          <div v-if="customServiceMode" class="field-block">
            <label class="form-label">서비스명 직접입력</label>
            <input
              v-model="subscriptionForm.customSubscriptionName"
              type="text"
              class="form-input mt-2"
              placeholder="예: Adobe Creative Cloud"
            />
          </div>

          <div class="wizard__icon-box">
            <div class="wizard__icon-preview">
              <img
                v-if="previewImage"
                :src="previewImage"
                class="preview-img"
              />
              <AppAsset
                v-else
                type="service"
                :value="selectedServiceLabel"
                fallback="sparkles"
                :size="20"
                wrapper-class="inline-flex items-center justify-center"
                image-class="h-20 w-20 object-contain"
              />
            </div>
            <div>
              <div class="text-sm font-extrabold text-neutral-900">서비스 아이콘</div>
              <div class="mt-1 text-sm leading-6 text-neutral-500">기본 로고가 있으면 먼저 보여주고, 원하는 이미지를 올려 교체할 수도 있어요.</div>
              <div class="mt-2 text-xs font-semibold text-neutral-500">{{ subscriptionForm.serviceIconFileName || '기본 아이콘 사용 중' }}</div>
            </div>
            <div class="wizard__icon-actions">
              <label class="secondary-button !min-h-11 cursor-pointer !px-4">
                업로드
                <input type="file" class="hidden" accept="image/*" @change="handleIconFileChange" />
              </label>
              <button type="button" class="secondary-button !min-h-11 !px-4" :disabled="!subscriptionForm.serviceIconFileName" @click="clearIconFile">삭제</button>
            </div>
          </div>
        </div>
              
                <div v-else-if="currentStep === 3" class="wizard__block">
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="form-label">결제 금액</label>
              <input
                v-model="subscriptionForm.paymentAmount"
                type="number"
                min="0"
                class="form-input mt-2"
                placeholder="예: 17000"
                @input="paymentAmountErrorMessage = ''"
              />
              <p v-if="paymentAmountErrorMessage" class="wizard__error-message">
                {{ paymentAmountErrorMessage }}
              </p>
            </div>

            <div>
              <label class="form-label">결제 주기</label>
              <div class="wizard__subline mt-2">
                <button
                  v-for="cycle in billingCycles"
                  :key="cycle.value"
                  type="button"
                  class="chip-button"
                  :class="{ 'is-selected': subscriptionForm.billingCycle === cycle.value }"
                  @click="() => { subscriptionFormStore.setBillingCycle(cycle.value); billingCycleErrorMessage = '' }"
                >
                  {{ cycle.label }}
                </button>
              </div>

              <p v-if="billingCycleErrorMessage" class="wizard__error-message">
                {{ billingCycleErrorMessage }}
              </p>
            </div>
          </div>

          <div class="mt-4 grid gap-4 md:grid-cols-2">
            <div>
              <div class="flex items-center justify-between gap-3">
                <label class="form-label">결제 카드</label>
                <div class="flex items-center gap-3">
                  <RouterLink to="/payment-cards" class="wizard__pick-link">카드 관리</RouterLink>
                  <button
                    type="button"
                    class="wizard__pick-link"
                    @click="showCardPicker = !showCardPicker"
                  >
                    이미지로 선택
                  </button>
                </div>
              </div>

              <div class="wizard__card-select-wrap mt-2">
                <button
                  type="button"
                  class="wizard__card-input w-full"
                  @click="showCardDropdown = !showCardDropdown"
                >
                  <div class="wizard__card-thumb">
                    <AppAsset
                      type="card"
                      :value="subscriptionForm.paymentCardName"
                      fallback="creditcard"
                      :size="18"
                      wrapper-class="inline-flex items-center justify-center"
                      image-class="h-12 w-12 object-contain"
                      icon-class="text-[#8A6A00]"
                      badge-class="inline-flex min-w-[44px] items-center justify-center rounded-xl px-2.5 py-1 text-[10px] font-black uppercase tracking-[-0.02em]"
                    />
                  </div>

                  <div class="wizard__card-placeholder">
                    {{ subscriptionForm.paymentCardName || '예: 현대카드' }}
                  </div>

                  <div class="wizard__card-arrow"></div>
                </button>

                <div v-if="showCardDropdown" class="wizard__card-dropdown">
                  <button
                    v-for="card in cardOptions"
                    :key="card.paymentId"
                    type="button"
                    class="wizard__card-option"
                    @click="pickCard(card)"
                  >
                    {{ card.paymentCardName }}
                  </button>

                  <div v-if="cardOptions.length === 0" class="wizard__card-empty">
                    등록된 카드가 없습니다.
                  </div>
                </div>
              </div>

              <p v-if="paymentCardErrorMessage" class="wizard__error-message">
                {{ paymentCardErrorMessage }}
              </p>
            </div>

            <div>
              <label class="form-label">결제 시작일</label>
              <input
                v-model="subscriptionForm.paymentStartDate"
                type="date"
                class="form-input mt-2 wizard__date-field"
                @change="paymentStartDateErrorMessage = ''"
              />

              <p v-if="paymentStartDateErrorMessage" class="wizard__error-message">
                {{ paymentStartDateErrorMessage }}
              </p>
            </div>
          </div>

          <div v-if="showCardPicker">
            <div v-if="cardOptions.length === 0" class="text-sm text-neutral-500 mt-3">
              등록된 카드가 없습니다. 카드 관리에서 먼저 카드를 등록해주세요.
            </div>

            <div v-else class="picker-grid mt-4">
              <button
                v-for="card in cardOptions"
                :key="card.paymentId"
                type="button"
                class="picker-card"
                :class="{ 'picker-card--active': subscriptionForm.paymentCardId === card.paymentId }"
                @click="pickCard(card)"
              >
                <div class="picker-card__img">
                  <AppAsset
                    type="card"
                    :value="card.paymentCardName"
                    fallback="creditcard"
                    :size="16"
                    wrapper-class="inline-flex items-center justify-center"
                    image-class="h-12 w-12 object-contain"
                    icon-class="text-[#8A6A00]"
                    badge-class="inline-flex min-w-[48px] items-center justify-center rounded-xl px-2.5 py-1 text-[10px] font-black uppercase tracking-[-0.02em]"
                  />
                </div>
                <div class="picker-card__label">{{ card.paymentCardName }}</div>
              </button>
            </div>
          </div>
        </div>
        <div v-else class="wizard__confirm">
          <div class="wizard__review">
            <div class="wizard__review-top">
              <div class="wizard__review-icon">
                <AppAsset
                  type="service"
                  :value="selectedServiceLabel || resolvedSubscriptionName"
                  secondary-type="category"
                  :secondary-value="selectedCategoryLabel"
                  fallback="sparkles"
                  :size="20"
                  wrapper-class="inline-flex items-center justify-center"
                  image-class="h-20 w-20 object-contain"
                  icon-class="text-[#8A6A00]"
                />
              </div>
              <div>
                <div class="wizard__review-name">
                  {{ selectedServiceLabel || resolvedSubscriptionName || '선택한 서비스' }}
                </div>
                <div class="text-sm text-neutral-500">{{ selectedCategoryLabel }}</div>
              </div>
            </div>
            <div class="wizard__row"><span>금액</span><strong>{{ formatCurrency(subscriptionForm.paymentAmount) }}</strong></div>
            <div class="wizard__row"><span>주기</span><strong>{{ subscriptionForm.billingCycle === 'YEARLY' ? '매년 결제' : '매월 결제' }}</strong></div>
            <div class="wizard__row"><span>결제일</span><strong>{{ subscriptionForm.paymentStartDate || '-' }}</strong></div>
            <div class="wizard__row"><span>카드</span><strong>{{ subscriptionForm.paymentCardName === '직접 입력' ? (subscriptionForm.customPaymentCardName || '직접 입력 예정') : (subscriptionForm.paymentCardName || '미등록') }}</strong></div>
          </div>

          <!--
          <div>
            <label class="form-label">메모를 남길 수 있어요 (선택)</label>
            <textarea
              v-model="subscriptionForm.note"
              rows="4"
              class="form-input mt-2 min-h-[120px] resize-none py-4"
              placeholder="예: 가족과 공유 중, 학생 할인 확인 필요"
            ></textarea>
          </div>
          -->
          <!--
          <div class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-neutral-900 p-4 text-xs leading-6 text-white">
            <div class="mb-2 text-[11px] font-extrabold uppercase tracking-[0.12em] text-white/60">API payload preview</div>
            <pre class="overflow-x-auto whitespace-pre-wrap break-all">{{ JSON.stringify(requestPayload, null, 2) }}</pre>
          </div>
        
        -->
        </div>

        <div class="wizard__actions">
          <button v-if="currentStep > 1" type="button" class="secondary-button !w-full" @click="subscriptionFormStore.prevStep()">이전</button>
          <button v-if="currentStep < 4" type="button" class="primary-button !w-full" @click="goNext">다음
</button>
          <button v-else type="button" class="primary-button !w-full" @click="submitDraft">구독 추가하기</button>
        </div>
      </div>
    </section>
    <div v-if="showSuccessModal" class="modal-overlay">
  <div class="success-modal">
    <div class="success-modal__icon">✓</div>
    <div class="success-modal__title">구독이 추가되었습니다</div>
    <div class="success-modal__desc">
      등록한 구독이 목록에 반영되었어요.
    </div>

    <div class="success-modal__actions">
      <button type="button" class="primary-button !w-full" @click="closeSuccessModal">
        확인
      </button>
    </div>
  </div>
</div>
  </AppShell>
</template>

<style scoped>
.wizard__top {
  display: grid;
  gap: 14px;
  margin-bottom: 18px;
}

.wizard__step {
  color: #8a6a00;
  font-weight: 800;
}

.utility-gauge {
  height: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(46, 34, 10, 0.08);
}

.utility-gauge > span {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #f2d221, #eed03b);
  transition: width 0.2s ease;
}

.wizard__body {
  padding: clamp(24px, 2.3vw, 34px);
}

.wizard__heading {
  font-size: clamp(28px, 4vw, 38px);
  font-weight: 900;
  line-height: 1.22;
  color: #1e180d;
}

.wizard__block,
.wizard__confirm {
  margin-top: 28px;
  display: grid;
  gap: 14px;
}

.wizard__selection,
.wizard__icon-box,
.wizard__review,
.wizard__card-input {
  border: 1px solid rgba(46, 34, 10, 0.08);
  border-radius: 22px;
  background: rgba(247, 241, 227, 0.78);
}

.wizard__selection {
  padding: 18px 20px;
  display: grid;
  gap: 4px;
  background: rgba(242, 210, 33, 0.12);
}

.wizard__selection-label {
  font-size: 12px;
  font-weight: 800;
  color: #61563d;
}

.wizard__selection-value {
  font-size: 22px;
  font-weight: 900;
  color: #1e180d;
}

.wizard__tile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 12px;
}

.wizard__tile {
  position: relative;
  padding: 20px;
  border: 1px solid rgba(46, 34, 10, 0.08);
  border-radius: 22px;
  background: rgba(255, 253, 247, 0.92);
  display: grid;
  gap: 12px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, background-color 0.18s ease, box-shadow 0.18s ease;
}

.wizard__tile:hover,
.wizard__service-chip:hover,
.picker-card:hover {
  transform: translateY(-1px);
}

.wizard__tile--active {
  border: 2px solid #f2d221;
  background: rgba(242, 210, 33, 0.18);
  box-shadow: 0 10px 24px rgba(242, 210, 33, 0.22);
  transform: translateY(-2px);
}

.wizard__tile-check {
  position: absolute;
  right: 12px;
  top: 12px;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 900;
  background: #f2d221;
  color: #231a07;
}

.wizard__tile-image,
.wizard__service-chip-icon,
.wizard__icon-preview,
.wizard__review-icon,
.wizard__card-thumb,
.picker-card__img {
  display: grid;
  place-items: center;
  border-radius: 18px;
  background: rgba(255, 253, 247, 0.92);
  border: 1px solid rgba(46, 34, 10, 0.08);
  color: #8a6a00;
  font-weight: 900;
}

.wizard__tile-image {
  height: 176px;
  font-size: 34px;
  background: linear-gradient(180deg, rgba(242,210,33,0.18), rgba(255,253,247,0.98));
}

.wizard__tile-title {
  font-size: 16px;
  font-weight: 900;
  color: #1e180d;
}

.wizard__chip-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.wizard__service-chip {
  border: 1px solid rgba(46, 34, 10, 0.08);
  background: rgba(255, 253, 247, 0.92);
  border-radius: 999px;
  min-height: 64px;
  padding: 0 20px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: 800;
  transition: transform 0.18s ease, border-color 0.18s ease, background-color 0.18s ease, box-shadow 0.18s ease;
}

.wizard__service-chip--active,
.picker-card--active {
  border-color: rgba(242, 210, 33, 0.34);
  background: rgba(242, 210, 33, 0.16);
  box-shadow: 0 0 0 3px rgba(242, 210, 33, 0.1);
}

.wizard__service-chip-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  font-size: 12px;
  background: linear-gradient(180deg, rgba(242,210,33,0.16), rgba(255,253,247,0.96));
}

.field-block {
  margin-top: 14px;
}

.wizard__icon-box {
  margin-top: 16px;
  padding: 16px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 16px;
  align-items: center;
}

.wizard__icon-preview,
.wizard__review-icon {
  width: 88px;
  height: 88px;
  border-radius: 20px;
  font-size: 18px;
}

.wizard__icon-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.wizard__subline {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.wizard__pick-link {
  border: 0;
  background: transparent;
  color: #8a6a00;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}

.wizard__card-input {
  padding: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.wizard__card-thumb {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  flex-shrink: 0;
}

.wizard__card-field {
  min-height: 44px;
  border: 0 !important;
  background: transparent !important;
  padding: 0 6px !important;
  box-shadow: none !important;
}

.wizard__review {
  padding: 18px;
  display: grid;
  gap: 14px;
}

.wizard__review-top {
  display: flex;
  gap: 12px;
  align-items: center;
}

.wizard__review-name {
  font-size: 18px;
  font-weight: 900;
  color: #1e180d;
}

.wizard__row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #61563d;
}

.wizard__row strong {
  color: #1e180d;
}

.wizard__actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 28px;
}

.wizard__actions > :only-child {
  grid-column: 1 / -1;
}

.picker-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.picker-card {
  border: 1px solid rgba(46, 34, 10, 0.08);
  background: rgba(255, 253, 247, 0.92);
  border-radius: 20px;
  padding: 12px;
  display: grid;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, background-color 0.18s ease, box-shadow 0.18s ease;
}

.picker-card__img {
  height: 124px;
  font-size: 24px;
}

.picker-card__label {
  font-size: 13px;
  font-weight: 800;
  color: #1e180d;
}

.wizard__card-select-wrap {
  position: relative;
}
/* 카드 선택 박스 */
.wizard__card-input {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 69px;
  border: 1px solid rgba(46, 34, 10, 0.08);
  border-radius: 22px;
  background: rgba(247, 241, 227, 0.78);
}

.wizard__card-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  min-height: 54px;
  text-align: left;
  color: #61563d;
  font-size: 15px;
  font-weight: 700;
}

/* 드롭다운 화살표 모양 */
.wizard__card-arrow {
  margin-left: auto;
  margin-right: 20px;
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 8px solid #8a6a00;
  display: flex;
  align-items: center;
  justify-content: center;
  align-self: center;
}
.wizard__card-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 100%;
  border: 1px solid rgba(46, 34, 10, 0.08);
  border-radius: 18px;
  background: #fffdf7;
  box-shadow: 0 10px 24px rgba(46, 34, 10, 0.08);
  padding: 8px;
  z-index: 20;
  display: grid;
  gap: 6px;
}

.wizard__card-option {
  min-height: 44px;
  border: 0;
  border-radius: 12px;
  background: transparent;
  text-align: left;
  padding: 0 14px;
  font-size: 14px;
  font-weight: 700;
  color: #1e180d;
  cursor: pointer;
}

.wizard__card-option:hover {
  background: rgba(242, 210, 33, 0.12);
}

.wizard__card-empty {
  padding: 12px 14px;
  font-size: 14px;
  color: #8a7f67;
}

/* 그 날짜 입력 부분  */
.wizard__date-field {
  min-height: 69px;
  border-radius: 22px;
}

/* 구독이 확인 되었습니다 팝업창 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(30, 24, 13, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  padding: 20px;
}

.success-modal {
  width: 100%;
  max-width: 360px;
  border-radius: 28px;
  background: #fffdf7;
  border: 1px solid rgba(46, 34, 10, 0.08);
  box-shadow: 0 24px 60px rgba(46, 34, 10, 0.18);
  padding: 28px 24px 22px;
  display: grid;
  gap: 14px;
  text-align: center;
}

.success-modal__icon {
  width: 64px;
  height: 64px;
  margin: 0 auto;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: rgba(242, 210, 33, 0.18);
  color: #8a6a00;
  font-size: 28px;
  font-weight: 900;
}

.success-modal__title {
  font-size: 22px;
  font-weight: 900;
  color: #1e180d;
}

.success-modal__desc {
  font-size: 14px;
  line-height: 1.6;
  color: #61563d;
}

.success-modal__actions {
  margin-top: 6px;
  display: grid;
  gap: 10px;
}

.success-modal__actions.two-buttons {
  grid-template-columns: 1fr 1fr;
}

.wizard__error-message {
  margin-top: 6px;
  color: #dc2626;
  font-size: 13px;
  font-weight: 700;
}

.wizard__error-message::before {
  content: "⚠ ";
}
@media (max-width: 1280px) {
  .wizard__icon-box {
    grid-template-columns: auto 1fr;
  }

  .wizard__icon-actions {
    grid-column: 1 / -1;
  }
}

@media (max-width: 1120px) {
  .wizard__actions {
    grid-template-columns: 1fr;
  }
}
</style>