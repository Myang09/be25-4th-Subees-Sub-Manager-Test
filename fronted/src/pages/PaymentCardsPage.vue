<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import AppShell from '@/components/layout/AppShell.vue'
import AppAsset from '@/components/ui/AppAsset.vue'
import { usePaymentCardsStore } from '@/stores/paymentCards'

const paymentCardsStore = usePaymentCardsStore()
const { cards, cardCount } = storeToRefs(paymentCardsStore)

const isEditing = ref(false)
const showDeleteConfirm = ref(false)
const pendingDeleteId = ref(null)
const originalEditValue = ref(null)

const noticeTimer = ref(null)
const notice = reactive({
  show: false,
  type: 'success', // success | error | warning | info
  message: '',
})

const showNotice = ({ type = 'success', message = '' }) => {
  notice.show = true
  notice.type = type
  notice.message = message

  if (noticeTimer.value) {
    clearTimeout(noticeTimer.value)
  }

  noticeTimer.value = setTimeout(() => {
    notice.show = false
    notice.message = ''
  }, 2500)
}

const closeNotice = () => {
  notice.show = false
  notice.message = ''

  if (noticeTimer.value) {
    clearTimeout(noticeTimer.value)
    noticeTimer.value = null
  }
}

const noticeClass = computed(() => {
  if (notice.type === 'error') {
    return 'border-[#F3B3A6] bg-[#FFF1ED] text-[#C65B38]'
  }

  if (notice.type === 'warning') {
    return 'border-[#F0D98A] bg-[#FFF8DD] text-[#8A6A00]'
  }

  if (notice.type === 'info') {
    return 'border-[#BFD7FF] bg-[#EEF5FF] text-[#315EA8]'
  }

  return 'border-[#9FE3C8] bg-[#EAFBF3] text-[#147A55]'
})

/**
 * 선택 카드 목록
 */
const predefinedCardOptions = ref([
  { cardId: 1, cardCompany: '신한카드' },
  { cardId: 2, cardCompany: '국민카드' },
  { cardId: 3, cardCompany: '농협카드' },
  { cardId: 4, cardCompany: '우리카드' },
  { cardId: 5, cardCompany: '하나카드' },
  { cardId: 6, cardCompany: '삼성카드' },
  { cardId: 7, cardCompany: '현대카드' },
  { cardId: 8, cardCompany: '롯데카드' },
])

const cardImageMap = {
  1: 'sin',
  2: 'kb',
  3: 'nh',
  4: 'u',
  5: 'han',
  6: 'sam',
  7: 'hun',
  8: 'lot',
}

const getCardImagePath = (card) => {
  if (!card.cardId) return null
  const imageName = cardImageMap[card.cardId]
  return imageName ? `/image/card/${imageName}.png` : null
}

const form = reactive({
  paymentId: null,
  cardType: 'CUSTOM',
  cardId: null,
  customCardCompany: '',
  cardName: '',
})

const pageStats = computed(() => [
  {
    label: '등록 카드 수',
    value: `${cardCount.value}개`,
    note: '구독 결제에 사용할 카드를 관리합니다.',
  },
])

const resetForm = () => {
  form.paymentId = null
  form.cardType = 'CUSTOM'
  form.cardId = null
  form.customCardCompany = ''
  form.cardName = ''
  isEditing.value = false
  lockedCardType.value = null
}

const isSelectedCard = (card) => !!card.cardId

const displayProviderName = (card) => {
  if (card.cardId) {
    const matched = predefinedCardOptions.value.find(
      (item) => item.cardId === card.cardId,
    )
    return matched?.cardCompany || '카드'
  }

  return card.customCardCompany || '직접 입력 카드'
}

const lockedCardType = ref(null)

const startEdit = (card) => {
  form.paymentId = card.paymentId ?? null
  form.cardName = card.cardName ?? ''

  if (card.cardId) {
    form.cardType = 'SELECT'
    form.cardId = card.cardId
    form.customCardCompany = ''
  } else {
    form.cardType = 'CUSTOM'
    form.cardId = null
    form.customCardCompany = card.customCardCompany ?? ''
  }

  lockedCardType.value = form.cardType

  originalEditValue.value = {
    paymentId: card.paymentId ?? null,
    cardType: card.cardId ? 'SELECT' : 'CUSTOM',
    cardId: card.cardId ?? null,
    customCardCompany: (card.customCardCompany ?? '').trim(),
    cardName: (card.cardName ?? '').trim(),
  }

  isEditing.value = true
}


const validateForm = () => {
  if (form.cardType === 'SELECT' && !form.cardId) {
    showNotice({
      type: 'warning',
      message: '카드를 선택해주세요.',
    })
    return false
  }

  if (form.cardType === 'CUSTOM' && !form.customCardCompany.trim()) {
    showNotice({
      type: 'warning',
      message: '카드사명을 입력해주세요.',
    })
    return false
  }

  if (!form.cardName.trim()) {
    showNotice({
      type: 'warning',
      message: '카드 별칭을 입력해주세요.',
    })
    return false
  }

  return true
}

const submitForm = async () => {
  if (!validateForm()) return

  try {
    const payload =
      form.cardType === 'SELECT'
        ? {
            cardId: form.cardId,
            customCardCompany: null,
            cardName: form.cardName.trim(),
          }
        : {
            cardId: null,
            customCardCompany: form.customCardCompany.trim(),
            cardName: form.cardName.trim(),
          }

    if (isEditing.value && form.paymentId) {
      await paymentCardsStore.updateCard({
        paymentId: form.paymentId,
        ...payload,
      })

      showNotice({
        type: 'success',
        message: '카드가 수정되었습니다.',
      })
    } else {
      await paymentCardsStore.addCard(payload)

      showNotice({
        type: 'success',
        message: '카드가 등록되었습니다.',
      })
    }

    resetForm()
    await paymentCardsStore.fetchCards()
  } catch (error) {
    console.error('카드 저장 실패:', error)

    showNotice({
      type: 'error',
      message: error?.message || '카드 저장 중 오류가 발생했습니다.',
    })
  }
}

const requestDelete = (paymentId) => {
  pendingDeleteId.value = paymentId
  showDeleteConfirm.value = true
}

const confirmDelete = async () => {
  if (!pendingDeleteId.value) return

  try {
    await paymentCardsStore.deleteCard(pendingDeleteId.value)

    if (form.paymentId === pendingDeleteId.value) {
      resetForm()
    }

    pendingDeleteId.value = null
    showDeleteConfirm.value = false

    await paymentCardsStore.fetchCards()

    showNotice({
      type: 'success',
      message: '카드가 삭제되었습니다.',
    })
  } catch (error) {
    console.error('카드 삭제 실패:', error)

    pendingDeleteId.value = null
    showDeleteConfirm.value = false

    showNotice({
      type: 'error',
      message: error?.message || '카드 삭제 중 오류가 발생했습니다.',
    })
  }
}

const cancelDelete = () => {
  pendingDeleteId.value = null
  showDeleteConfirm.value = false
}

onMounted(async () => {
  try {
    await paymentCardsStore.fetchCards()
  } catch (error) {
    console.error('카드 데이터 조회 실패:', error)

    showNotice({
      type: 'error',
      message: error?.message || '카드 데이터를 불러오지 못했습니다.',
    })
  }
})


</script>

<template>
  <AppShell title="hidden">
    <div class="grid gap-6">
      <section class="shell-card p-7 md:p-8">
        <div class="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
          <div>
            <p class="text-sm font-semibold text-[#8A6A00]">결제 카드 관리</p>
            <h1 class="mt-3 page-title !mt-0 !text-[34px] xl:!text-[42px]">
              구독 결제에 쓰는 카드를 한곳에서 관리하세요
            </h1>
            <p class="mt-3 max-w-4xl body-copy">
              카드 등록, 수정, 삭제를 같은 화면에서 바로 처리할 수 있습니다.
            </p>
          </div>

          <div class="grid gap-3 sm:grid-cols-1 xl:min-w-[220px]">
            <article
              v-for="item in pageStats"
              :key="item.label"
              class="ghost-card p-4"
            >
              <p class="text-xs font-semibold uppercase tracking-[0.08em] text-neutral-500">
                {{ item.label }}
              </p>
              <p class="mt-3 text-2xl font-bold tracking-[-0.03em] text-neutral-900">
                {{ item.value }}
              </p>
              <p class="mt-2 body-copy">{{ item.note }}</p>
            </article>
          </div>
        </div>
      </section>

          <div
        v-if="notice.show"
        :class="noticeClass"
        class="rounded-[22px] border px-5 py-4 text-sm font-semibold shadow-sm transition"
      >
        <div class="flex items-center justify-between gap-3">
          <p>{{ notice.message }}</p>
          <button
            type="button"
            class="shrink-0 text-xs font-bold opacity-70 hover:opacity-100"
            @click="closeNotice"
          >
            닫기
          </button>
        </div>
      </div>

      <section class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
        <div class="section-card">
          <div class="flex items-end justify-between gap-3 border-b border-[rgba(46,34,10,0.08)] pb-5">
            <div>
              <p class="text-sm font-semibold text-[#8A6A00]">등록 카드 목록</p>
              <h2 class="mt-2 section-heading">카드 {{ cardCount }}개</h2>
            </div>
            <button
              type="button"
              class="secondary-button !min-h-11 !px-4"
              @click="resetForm"
            >
              새 카드 입력
            </button>
          </div>

          <div class="mt-5 grid gap-4 md:grid-cols-2">
            <article
              v-for="card in cards"
              :key="card.paymentId"
              class="rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.88)] p-5"
            >

            <div class="flex items-center gap-3">
            <div class="grid h-14 w-14 place-items-center rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white">
              <img
                v-if="getCardImagePath(card)"
                :src="getCardImagePath(card)"
                :alt="displayProviderName(card)"
                class="h-9 w-9 object-contain"
              />
              <span
                v-else
                class="text-xs font-bold text-[#8A6A00]"
              >
                카드
              </span>
            </div>

            <div class="min-w-0 flex-1">
              <p class="text-base font-bold text-neutral-900">{{ card.cardName }}</p>
              <p class="mt-1 text-sm text-neutral-500">
                {{ displayProviderName(card) }}
              </p>
            </div>
          </div>

              <div class="mt-4 rounded-[18px] bg-brand-50 px-4 py-3 text-sm text-neutral-600">
                <p class="font-semibold text-neutral-900">
                  {{ isSelectedCard(card) ? '카드 선택' : '직접 입력 카드' }}
                </p>
                <p class="mt-1 leading-6">
                  {{
                    isSelectedCard(card)
                      ? '기본 카드 목록에서 선택한 카드입니다.'
                      : '사용자가 직접 입력한 결제 카드입니다.'
                  }}
                </p>
              </div>

              <div class="mt-4 flex flex-wrap gap-2.5">
                <button
                  type="button"
                  class="primary-button !min-h-11 !px-4"
                  @click="startEdit(card)"
                >
                  수정
                </button>
                <button
                  type="button"
                  class="danger-ghost-button !min-h-11 !px-4"
                  @click="requestDelete(card.paymentId)"
                >
                  삭제
                </button>
              </div>
            </article>

            <div
              v-if="cards.length === 0"
              class="col-span-full rounded-[24px] border border-dashed border-[rgba(46,34,10,0.16)] bg-white/70 p-8 text-center text-sm text-neutral-500"
            >
              등록된 카드가 없습니다.
            </div>
          </div>
        </div>

        <aside class="shell-card p-6 xl:sticky xl:top-6 xl:self-start">
          <p class="text-sm font-semibold text-[#8A6A00]">
            {{ isEditing ? '카드 수정' : '카드 등록' }}
          </p>

          <h2 class="mt-2 text-[24px] font-bold tracking-[-0.03em] text-neutral-900">
            {{ isEditing ? '등록 카드 수정' : '새 결제 카드 추가' }}
          </h2>

          <p class="mt-2 text-sm leading-6 text-neutral-500">
            선택 카드 또는 직접 입력 카드 중 하나를 등록할 수 있습니다.
          </p>

          <div class="mt-5 grid gap-4">
        <label class="grid gap-2">
          <span class="form-label">카드 등록 방식</span>

          <template v-if="isEditing">
            <div class="form-input flex items-center bg-neutral-100 text-neutral-500">
              {{ form.cardType === 'SELECT' ? '카드 선택' : '카드 직접 입력' }}
            </div>
          </template>

          <select
            v-else
            v-model="form.cardType"
            class="form-input"
          >
            <option value="CUSTOM">카드 직접 입력</option>
            <option value="SELECT">카드 선택</option>
          </select>
        </label>

            <label v-if="form.cardType === 'SELECT'" class="grid gap-2">
              <span class="form-label">카드 선택</span>
              <select v-model="form.cardId" class="form-input">
                <option :value="null">카드를 선택하세요</option>
                <option
                  v-for="item in predefinedCardOptions"
                  :key="item.cardId"
                  :value="item.cardId"
                >
                  {{ item.cardCompany }}
                </option>
              </select>
            </label>

            <label v-if="form.cardType === 'CUSTOM'" class="grid gap-2">
              <span class="form-label">카드사명</span>
              <input
                v-model="form.customCardCompany"
                class="form-input"
                placeholder="예: 토스뱅크, 카카오뱅크"
              />
            </label>

            <label class="grid gap-2">
              <span class="form-label">카드 별칭</span>
              <input
                v-model="form.cardName"
                class="form-input"
                placeholder="예: OTT 전용카드"
              />
            </label>
          </div>

          <div class="mt-6 flex flex-wrap gap-3">
            <button
              type="button"
              class="primary-button"
              @click="submitForm"
            >
              {{ isEditing ? '카드 저장' : '카드 등록' }}
            </button>
            <button
              type="button"
              class="secondary-button"
              @click="resetForm"
            >
              취소
            </button>
          </div>
        </aside>
      </section>
    </div>

    <div
      v-if="showDeleteConfirm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/40 px-4"
    >
      <div class="w-full max-w-md rounded-modal bg-brand-50 p-6 shadow-floating">
        <p class="text-sm font-semibold text-danger">카드 삭제</p>
        <h3 class="mt-2 text-xl font-bold tracking-[-0.02em] text-neutral-900">
          등록한 카드를 삭제하시겠습니까?
        </h3>
        <p class="mt-3 text-sm leading-6 text-neutral-500">
          삭제 후에는 다시 등록해야 사용할 수 있습니다.
        </p>
        <div class="mt-6 flex justify-end gap-3">
          <button
            type="button"
            class="secondary-button"
            @click="cancelDelete"
          >
            취소
          </button>
          <button
            type="button"
            class="primary-button bg-danger hover:bg-red-600 active:bg-red-700"
            @click="confirmDelete"
          >
            삭제
          </button>
        </div>
      </div>

      <div
  v-if="showNoticeModal"
  class="fixed inset-0 z-[60] flex items-center justify-center bg-neutral-900/40 px-4"
>
  <div class="w-full max-w-md rounded-[32px] bg-[rgba(255,253,247,0.98)] p-6 shadow-[0_24px_80px_rgba(46,34,10,0.18)]">
    <p class="text-sm font-semibold" :class="noticeTitleClass">
      {{ notice.title }}
    </p>

    <p class="mt-4 text-base leading-7 text-neutral-700 whitespace-pre-line">
      {{ notice.message }}
    </p>

    <div class="mt-8 flex justify-end">
      <button
        type="button"
        class="primary-button !min-h-11 !px-6"
        @click="closeNotice"
      >
        {{ notice.buttonText }}
      </button>
    </div>
  </div>
</div>
    </div>
  </AppShell>
</template>