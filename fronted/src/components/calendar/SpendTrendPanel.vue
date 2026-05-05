<script setup>
import { computed } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
  maxAmount: {
    type: Number,
    default: 0,
  },
  view: {
    type: String,
    default: 'MONTHLY',
  },
  paymentPreview: {
    type: Array,
    default: () => [],
  },
  paymentPreviewLabel: {
    type: String,
    default: '',
  },
  totalAmount: {
    type: Number,
    default: 0,
  },
  previousTotalAmount: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['change-view'])

const formatCurrency = (value) =>
  `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`

const normalizedItems = computed(() => {
  const baseItems = (props.items || [])
    .map((item, index) => ({
      key: item.key || `item-${index}`,
      label: item.label || item.subscriptionName || '-',
      totalAmount: Number(item.totalAmount || item.paymentAmount || 0),
    }))
    .filter((item) => item.totalAmount > 0)

  if (baseItems.length) return baseItems

  if (props.view === 'MONTHLY') {
    return (props.paymentPreview || [])
      .map((item, index) => ({
        key: item.paymentId || `preview-${index}`,
        label: item.subscriptionName || '-',
        totalAmount: Number(item.paymentAmount || 0),
      }))
      .filter((item) => item.totalAmount > 0)
  }

  return []
})

const resolvedMaxAmount = computed(() => {
  if (Number(props.maxAmount || 0) > 0) return Number(props.maxAmount || 0)

  if (!normalizedItems.value.length) return 0

  return normalizedItems.value.reduce(
    (max, item) => Math.max(max, item.totalAmount),
    0,
  )
})

const topItem = computed(() => {
  if (!normalizedItems.value.length) return null

  return [...normalizedItems.value].sort(
    (a, b) => b.totalAmount - a.totalAmount,
  )[0]
})

const averageAmount = computed(() => {
  if (!normalizedItems.value.length) return 0

  const total = normalizedItems.value.reduce(
    (sum, item) => sum + item.totalAmount,
    0,
  )

  return Math.floor(total / normalizedItems.value.length)
})

const changeAmount = computed(() => {
  if (props.previousTotalAmount == null) return null
  return Number(props.totalAmount || 0) - Number(props.previousTotalAmount || 0)
})

const changeRate = computed(() => {
  const previous = Number(props.previousTotalAmount || 0)
  const current = Number(props.totalAmount || 0)

  if (props.previousTotalAmount == null) return null
  if (previous === 0) return current > 0 ? 100 : 0

  return Number((((current - previous) / previous) * 100).toFixed(1))
})

const barWidth = (amount) => {
  const max = resolvedMaxAmount.value
  if (!max || amount <= 0) return '0%'
  return `${Math.max((amount / max) * 100, 6)}%`
}
</script>

<template>
  <section class="section-card !p-5 lg:!p-6">
    <div class="flex items-start justify-between gap-4 border-b border-[rgba(46,34,10,0.08)] pb-4">
      <div>
        <p class="text-sm font-semibold text-[#8A6A00]">소비 분석</p>
        <h2 class="mt-2 text-[22px] font-bold tracking-[-0.03em] text-neutral-900">
          {{ view === 'YEARLY' ? '연간 소비 분석' : '월별 소비 분석' }}
        </h2>
        <p class="mt-2 text-xs leading-5 text-neutral-400">
          {{
            view === 'YEARLY'
              ? '연간 기준으로 카테고리 소비 비중을 한눈에 확인해 보세요.'
              : '연간구독권은 월별 소비 분석을 위해 12개월로 나누어 반영합니다'
          }}
        </p>
      </div>

      <div class="flex gap-2">
        <button
          type="button"
          class="rounded-full px-4 py-2 text-sm font-bold"
          :class="view === 'MONTHLY' ? 'bg-[#F4E2A1] text-neutral-900' : 'bg-[rgba(46,34,10,0.06)] text-neutral-500'"
          @click="emit('change-view', 'MONTHLY')"
        >
          월별
        </button>
        <button
          type="button"
          class="rounded-full px-4 py-2 text-sm font-bold"
          :class="view === 'YEARLY' ? 'bg-[#F4E2A1] text-neutral-900' : 'bg-[rgba(46,34,10,0.06)] text-neutral-500'"
          @click="emit('change-view', 'YEARLY')"
        >
          연간
        </button>
      </div>
    </div>

    <div class="mt-5 grid gap-3 md:grid-cols-2 xl:grid-cols-4">
      <article class="rounded-[24px] border border-[rgba(46,34,10,0.08)] p-4">
        <p class="text-xs text-neutral-400">
          {{ view === 'YEARLY' ? '선택 연도 총 지출' : '선택 월 총 지출' }}
        </p>
        <p class="mt-2 text-[18px] font-bold text-neutral-900">
          {{ formatCurrency(totalAmount) }}
        </p>
        <p class="mt-2 text-xs text-neutral-400">
          {{ view === 'YEARLY' ? '선택 연도 기준 합계' : '선택 월 기준 합계' }}
        </p>
      </article>

      <article class="rounded-[24px] border border-[rgba(46,34,10,0.08)] p-4">
        <p class="text-xs text-neutral-400">
          {{ view === 'YEARLY' ? '최고 지출 카테고리' : '최고 지출 항목' }}
        </p>
        <p class="mt-2 text-[18px] font-bold text-neutral-900">
          {{ topItem ? `${topItem.label} · ${formatCurrency(topItem.totalAmount)}` : '-' }}
        </p>
        <p class="mt-2 text-xs text-neutral-400">
          {{ topItem ? '가장 큰 금액 항목' : '표시할 데이터 없음' }}
        </p>
      </article>

      <article class="rounded-[24px] border border-[rgba(46,34,10,0.08)] p-4">
        <p class="text-xs text-neutral-400">
          {{ view === 'YEARLY' ? '카테고리 평균 지출' : '항목 평균 지출' }}
        </p>
        <p class="mt-2 text-[18px] font-bold text-neutral-900">
          {{ normalizedItems.length ? formatCurrency(averageAmount) : '-' }}
        </p>
        <p class="mt-2 text-xs text-neutral-400">
          {{ normalizedItems.length ? `${normalizedItems.length}개 항목 평균` : '표시할 데이터 없음' }}
        </p>
      </article>

      <article class="rounded-[24px] border border-[rgba(46,34,10,0.08)] p-4">
        <p class="text-xs text-neutral-400">
          {{ view === 'YEARLY' ? '전년 대비' : '전월 대비' }}
        </p>
        <p class="mt-2 text-[18px] font-bold text-neutral-900">
          {{ changeRate == null ? '-' : `${changeRate > 0 ? '+' : ''}${changeRate}%` }}
        </p>
        <p class="mt-2 text-xs text-neutral-400">
          {{
            changeAmount == null
              ? '비교 데이터 없음'
              : `${formatCurrency(Math.abs(changeAmount))} ${changeAmount >= 0 ? '증가' : '감소'}`
          }}
        </p>
      </article>
    </div>

    <div class="mt-5 rounded-[24px] border border-[rgba(46,34,10,0.08)] p-4">
      <div class="flex items-start justify-between gap-4">
        <div>
          <p class="text-sm font-bold text-[#8A6A00]">
            {{ view === 'YEARLY' ? '카테고리별 지출' : '구독 항목별 지출' }}
          </p>
          <p class="mt-1 text-xs text-neutral-400">
            가장 큰 금액 항목을 기준으로 상대 비교됩니다.
          </p>
        </div>

        <p v-if="topItem" class="text-xs font-bold text-neutral-500">
          최고값 {{ formatCurrency(topItem.totalAmount) }}
        </p>
      </div>

      <div v-if="normalizedItems.length" class="mt-5 grid gap-4">
        <article v-for="item in normalizedItems" :key="item.key" class="grid gap-2">
          <div class="flex items-center justify-between gap-3 text-sm font-bold text-neutral-900">
            <span>{{ item.label }}</span>
            <span>{{ formatCurrency(item.totalAmount) }}</span>
          </div>

          <div class="h-3 rounded-full bg-[rgba(46,34,10,0.08)]">
            <div
              class="h-3 rounded-full bg-[#E6C200]"
              :style="{ width: barWidth(item.totalAmount) }"
            ></div>
          </div>
        </article>
      </div>

      <div v-else class="py-12 text-center">
        <p class="text-base font-bold text-neutral-700">표시할 분석 데이터가 없습니다</p>
        <p class="mt-2 text-sm text-neutral-400">
          결제 데이터가 쌓이면 이 영역에 금액 흐름이 표시됩니다.
        </p>
      </div>
    </div>

    <div class="mt-6 border-t border-[rgba(46,34,10,0.08)] pt-6">
      <p class="text-sm font-bold text-[#8A6A00]">
        {{ paymentPreviewLabel }} 결제 정보
      </p>
      <p class="mt-1 text-xs text-neutral-400">
        선택한 달에 포함되는 구독 항목을 리스트로 다시 확인할 수 있어요.
      </p>

      <div v-if="paymentPreview.length" class="mt-4 grid gap-3">
        <article
          v-for="(item, index) in paymentPreview"
          :key="item.paymentId || `${item.subscriptionName}-${index}`"
          class="flex items-center justify-between gap-4 rounded-[24px] border border-[rgba(46,34,10,0.08)] p-4"
        >
          <div class="min-w-0">
            <p class="text-xs font-bold text-neutral-400">{{ item.displayDateLabel }}</p>
            <p class="mt-1 truncate text-base font-bold text-neutral-900">
              {{ item.subscriptionName }}
            </p>
            <p class="mt-1 text-xs text-neutral-500">
              {{ item.categoryName }} · {{ item.paymentCardName }}
            </p>
          </div>

          <p class="shrink-0 text-lg font-bold text-neutral-900">
            {{ formatCurrency(item.paymentAmount) }}
          </p>
        </article>
      </div>

      <div v-else class="py-10 text-center text-sm text-neutral-400">
        이 달에 예정된 결제 정보가 없습니다.
      </div>
    </div>
  </section>
</template>