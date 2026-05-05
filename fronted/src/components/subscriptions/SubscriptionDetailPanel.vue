<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import AppAsset from '@/components/ui/AppAsset.vue'

const props = defineProps({
  item: {
    type: Object,
    default: null,
  },
  cardOptions: {
    type: Array,
    default: () => [],
  },
  categoryOptions: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['save', 'delete'])

const displayItem = ref(null)
const isEditing = ref(false)
const editForm = ref(createEmptyForm())

function createEmptyForm() {
  return {
    subscriptionId: null,
    subscriptionName: '',
    categoryName: '',
    paymentAmount: 0,
    paymentCardName: '',
    billingCycle: 'MONTHLY',
    paymentStartDate: '',
    nextPaymentDate: '',
  }
}

const categoryNames = computed(() =>
  props.categoryOptions
    .map((category) => category?.categoryName)
    .filter(Boolean)
)

function normalizeDate(value) {
  if (!value) return ''

  if (typeof value === 'string') {
    return value.slice(0, 10)
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getLastDayOfMonth(year, month) {
  return new Date(year, month, 0).getDate()
}

function calculateNextPaymentDate(startDate, billingCycle) {
  const normalized = normalizeDate(startDate)
  if (!normalized) return ''

  const [year, month, day] = normalized.split('-').map(Number)
  if (!year || !month || !day) return ''

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const buildDate = (targetYear, targetMonth) => {
    const lastDay = getLastDayOfMonth(targetYear, targetMonth)
    const safeDay = Math.min(day, lastDay)
    return new Date(targetYear, targetMonth - 1, safeDay)
  }

  if (billingCycle === 'YEARLY') {
    let targetYear = today.getFullYear()
    let candidate = buildDate(targetYear, month)

    if (candidate < today) {
      targetYear += 1
      candidate = buildDate(targetYear, month)
    }

    return `${candidate.getFullYear()}-${String(candidate.getMonth() + 1).padStart(2, '0')}-${String(candidate.getDate()).padStart(2, '0')}`
  }

  let targetYear = today.getFullYear()
  let targetMonth = today.getMonth() + 1
  let candidate = buildDate(targetYear, targetMonth)

  if (candidate < today) {
    targetMonth += 1
    if (targetMonth > 12) {
      targetMonth = 1
      targetYear += 1
    }
    candidate = buildDate(targetYear, targetMonth)
  }

  return `${candidate.getFullYear()}-${String(candidate.getMonth() + 1).padStart(2, '0')}-${String(candidate.getDate()).padStart(2, '0')}`
}


function formatDate(value) {
  const normalized = normalizeDate(value)
  if (!normalized) return '-'

  const [year, month, day] = normalized.split('-')
  return `${year}.${month}.${day}`
}

function formatCurrency(value) {
  return `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`
}

function syncForm(item) {
  const billingCycle = item.billingCycle ?? 'MONTHLY'
  const paymentStartDate = normalizeDate(
    item.paymentStartDate || item.startDate || item.registeredAt || item.createdAt
  )

  editForm.value = {
    subscriptionId: item.subscriptionId ?? null,
    subscriptionName: item.subscriptionName ?? '',
    categoryName: item.categoryName ?? '',
    paymentAmount: item.paymentAmount ?? 0,
    paymentCardName: item.paymentCardName ?? '',
    billingCycle,
    paymentStartDate,
    nextPaymentDate:
      normalizeDate(item.nextPaymentDate) ||
      calculateNextPaymentDate(paymentStartDate, billingCycle),
  }
}


const cycleLabel = computed(() => {
  return editForm.value.billingCycle === 'YEARLY' ? '매년 결제' : '매월 결제'
})

const detailCycleLabel = computed(() => {
  return displayItem.value?.billingCycle === 'YEARLY' ? '매년 결제' : '매월 결제'
})

function handleEdit() {
  if (!displayItem.value) return
  syncForm(displayItem.value)
  isEditing.value = true
}

function handleCancel() {
  if (displayItem.value) {
    syncForm(displayItem.value)
  } else {
    editForm.value = createEmptyForm()
  }

  isEditing.value = false
}

async function handleSave() {
  const payload = {
    ...displayItem.value,
    ...editForm.value,
    subscriptionName: displayItem.value?.subscriptionName ?? editForm.value.subscriptionName,
    paymentAmount: Number(editForm.value.paymentAmount || 0),
  }

  await emit('save', payload)
  displayItem.value = { ...payload }
  syncForm(displayItem.value)
  isEditing.value = false
}

function handleDelete() {
  if (!displayItem.value?.subscriptionId) return
  emit('delete', displayItem.value.subscriptionId)
}

watch(
  () => [editForm.value.paymentStartDate, editForm.value.billingCycle],
  ([paymentStartDate, billingCycle]) => {
    editForm.value.nextPaymentDate = calculateNextPaymentDate(paymentStartDate, billingCycle)
  }
)

watch(
  () => props.item,
  (item) => {
    if (!item) {
      displayItem.value = null
      editForm.value = createEmptyForm()
      isEditing.value = false
      return
    }

    displayItem.value = { ...item }
    syncForm(displayItem.value)
    isEditing.value = false
  },
  { immediate: true, deep: true }
)
</script>

<template>
  <section class="detail-panel shell-card p-6 md:p-7">
    <template v-if="displayItem && Object.keys(displayItem).length">
      <div class="detail-panel__header">
        <div class="detail-panel__icon-wrap">
          <AppAsset
            type="service"
            :value="displayItem.subscriptionName"
            secondary-type="category"
            :secondary-value="displayItem.categoryName"
            fallback="sparkles"
            :size="22"
            wrapper-class="inline-flex items-center justify-center"
            image-class="h-10 w-10 object-contain"
            icon-class="text-[#8A6A00]"
          />
        </div>

        <div class="min-w-0 flex-1">
          <p class="detail-panel__eyebrow">구독 상세</p>
          <h2 class="detail-panel__title">{{ displayItem.subscriptionName }}</h2>
          <p class="detail-panel__description">
            항목 상세, 수정, 삭제를 이 영역에서 바로 처리할 수 있어요.
          </p>
        </div>
      </div>

      <div v-if="!isEditing" class="mt-5 grid gap-4">
        <div class="detail-grid">
          <div class="detail-card">
            <p class="detail-card__label">카테고리명</p>
            <p class="detail-card__value mt-2">{{ displayItem.categoryName || '-' }}</p>
          </div>

          <div class="detail-card">
            <p class="detail-card__label">결제 금액</p>
            <p class="detail-card__value mt-2">{{ formatCurrency(displayItem.paymentAmount) }}</p>
          </div>

          <div class="detail-card">
            <p class="detail-card__label">결제 카드</p>
            <p class="detail-card__value mt-2">{{ displayItem.paymentCardName || '-' }}</p>
          </div>

          <div class="detail-card">
            <p class="detail-card__label">결제 주기</p>
            <p class="detail-card__value mt-2">{{ detailCycleLabel }}</p>
          </div>

          <div class="detail-card">
            <p class="detail-card__label">결제 시작일</p>
            <p class="detail-card__value mt-2">
              {{ formatDate(displayItem.paymentStartDate || displayItem.startDate || displayItem.registeredAt || displayItem.createdAt) }}
            </p>
          </div>

          <div class="detail-card">
            <p class="detail-card__label">다음 결제일</p>
            <p class="detail-card__value mt-2">
              {{ formatDate(displayItem.nextPaymentDate || calculateNextPaymentDate(displayItem.paymentStartDate || displayItem.startDate || displayItem.registeredAt || displayItem.createdAt, displayItem.billingCycle)) }}
            </p>
          </div>
        </div>

        <div class="detail-actions">
          <button type="button" class="primary-button" @click="handleEdit">수정</button>
          <button type="button" class="secondary-button detail-actions__delete" @click="handleDelete">
            삭제
          </button>
        </div>
      </div>

      <div v-else class="mt-5 grid gap-4">
        <div>
          <label class="form-label">구독 항목명</label>
          <input
            v-model="editForm.subscriptionName"
            type="text"
            class="form-input form-input--readonly mt-2"
            readonly
          />
        </div>

        <div>
          <label class="form-label">카테고리명</label>
          <input
            v-model="editForm.categoryName"
            type="text"
            class="form-input form-input--readonly mt-2"
            readonly
          />
          <!--
            <option value="">카테고리를 선택하세요</option>
            <option v-for="name in categoryNames" :key="name" :value="name">{{ name }}</option>
          -->
        </div>

        <div>
          <label class="form-label">결제 금액</label>
          <input v-model="editForm.paymentAmount" type="number" min="0" class="form-input mt-2" />
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="form-label">결제 카드</label>
            <select v-model="editForm.paymentCardName" class="form-input mt-2">
              <option value="">카드를 선택하세요</option>
              <option v-for="name in cardOptions" :key="name" :value="name">{{ name }}</option>
            </select>
            <RouterLink to="/payment-cards" class="detail-link">등록 카드 관리하기</RouterLink>
          </div>

          <div>
            <label class="form-label">결제 주기</label>
            <select v-model="editForm.billingCycle" class="form-input mt-2">
              <option value="MONTHLY">매월 결제</option>
              <option value="YEARLY">매년 결제</option>
            </select>
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="form-label">결제 시작일</label>
            <input v-model="editForm.paymentStartDate" type="date" class="form-input mt-2" />
          </div>

          <div>
            <label class="form-label">다음 결제일</label>
            <input
              v-model="editForm.nextPaymentDate"
              type="date"
              class="form-input form-input--readonly mt-2"
              disabled
            />
          </div>
        </div>

        <div class="cycle-preview">
          <p class="cycle-preview__label">현재 선택한 결제 주기</p>
          <p class="cycle-preview__value">{{ cycleLabel }}</p>
        </div>

        <div class="detail-actions">
          <button type="button" class="primary-button" @click="handleSave">저장</button>
          <button type="button" class="secondary-button" @click="handleCancel">취소</button>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="detail-empty">
        <div>
          <p class="text-base font-semibold text-neutral-900">선택된 구독이 없습니다</p>
          <p class="mt-2 text-sm leading-6 text-neutral-500">
            왼쪽 목록에서 구독 항목을 선택하면 상세 정보가 열립니다.
          </p>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.detail-panel {
  border-radius: 28px;
  background: linear-gradient(180deg, #fffcf3 0%, #fffaf0 100%);
  border: 1px solid rgba(214, 186, 96, 0.16);
  box-shadow: 0 18px 44px rgba(80, 60, 12, 0.06);
}

.detail-panel__header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(46, 34, 10, 0.08);
}

.detail-panel__icon-wrap {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border-radius: 22px;
  border: 1px solid rgba(46, 34, 10, 0.06);
  background: #fff;
  box-shadow: 0 10px 24px rgba(46, 34, 10, 0.05);
  flex-shrink: 0;
}

.detail-panel__eyebrow {
  margin: 0;
  font-size: 13px;
  font-weight: 800;
  color: #8a6a00;
}

.detail-panel__title {
  margin-top: 6px;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.04em;
  color: #171717;
  word-break: keep-all;
}

.detail-panel__description {
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.7;
  color: #6b645c;
  word-break: keep-all;
}

.detail-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: 1fr;
}

@media (min-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.detail-card {
  border-radius: 22px;
  border: 1px solid rgba(46, 34, 10, 0.08);
  background: rgba(255, 255, 255, 0.72);
  padding: 18px 18px 16px;
}

.detail-card__label {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  color: #8d877f;
  text-transform: uppercase;
}

.detail-card__value {
  font-size: 17px;
  font-weight: 800;
  line-height: 1.45;
  color: #171717;
  word-break: keep-all;
}

.form-label {
  font-size: 13px;
  font-weight: 800;
  color: #5f5b57;
}

.form-input {
  min-height: 50px;
  width: 100%;
  border-radius: 18px;
  border: 1px solid rgba(46, 34, 10, 0.1);
  background: rgba(255, 255, 255, 0.95);
  padding: 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #171717;
  outline: none;
}

.form-input:focus {
  border-color: rgba(214, 174, 43, 0.9);
  box-shadow: 0 0 0 4px rgba(242, 210, 33, 0.14);
  background: #fff;
}

.form-input--readonly {
  background: rgba(245, 245, 245, 0.9);
  color: #7a746c;
  cursor: not-allowed;
}

.cycle-preview {
  border-radius: 22px;
  border: 1px solid rgba(46, 34, 10, 0.08);
  background: linear-gradient(180deg, rgba(255, 248, 220, 0.8) 0%, rgba(255, 252, 241, 0.95) 100%);
  padding: 18px;
}

.cycle-preview__label {
  font-size: 13px;
  font-weight: 800;
  color: #5f5b57;
}

.cycle-preview__value {
  margin-top: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #171717;
}

.detail-link {
  margin-top: 8px;
  display: inline-flex;
  font-size: 13px;
  font-weight: 700;
  color: #8a6a00;
  text-decoration: none;
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 4px;
}

.detail-actions__delete {
  color: #b7553c;
}

.detail-empty {
  display: flex;
  min-height: 480px;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  border: 1px dashed rgba(46, 34, 10, 0.16);
  background: rgba(255, 255, 255, 0.55);
  padding: 24px;
  text-align: center;
}
</style>
