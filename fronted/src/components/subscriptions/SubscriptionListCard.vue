<script setup>
import { computed } from 'vue'
import AppAsset from '@/components/ui/AppAsset.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
  isSelected: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['select'])

const formatCurrency = (value) =>
  `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`

const formatDate = (dateString) => {
  if (!dateString) return '-'

  const date = new Date(`${dateString}T00:00:00`)

  if (Number.isNaN(date.getTime())) return '-'

  return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`
}

const cycleLabel = computed(() =>
  props.item.billingCycle === 'YEARLY' ? '매년 결제' : '매월 결제'
)

const statusLabel = computed(() =>
  props.item.status === 'PAUSED' ? '일시정지' : '활성'
)

const statusClass = computed(() =>
  props.item.status === 'PAUSED'
    ? 'bg-[rgba(46,34,10,0.06)] text-neutral-500'
    : 'bg-[rgba(242,210,33,0.16)] text-[#8A6A00]'
)

const handleSelect = () => {
  emit('select', props.item)
}
</script>

<template>
  <button
    type="button"
    class="w-full rounded-[28px] border bg-white px-5 py-5 text-left transition md:px-6"
    :class="
      isSelected
        ? 'border-[rgba(242,210,33,0.6)] ring-2 ring-[rgba(242,210,33,0.18)] shadow-[0_16px_40px_rgba(46,34,10,0.08)]'
        : 'border-[rgba(46,34,10,0.08)] hover:border-[rgba(138,106,0,0.28)] hover:shadow-[0_12px_30px_rgba(46,34,10,0.06)]'
    "
    @click="handleSelect"
  >
    <div class="flex items-center gap-4">
      <div
        class="grid h-14 w-14 shrink-0 place-items-center rounded-[20px] border border-[rgba(46,34,10,0.06)] bg-brand-50"
      >
        <AppAsset
          type="service"
          :value="item.subscriptionName"
          secondary-type="category"
          :secondary-value="item.categoryName"
          fallback="sparkles"
          :size="22"
          wrapper-class="inline-flex items-center justify-center"
          image-class="h-9 w-9 object-contain"
          icon-class="text-[#8A6A00]"
        />
      </div>

      <div class="min-w-0 flex-1">
        <div class="flex flex-wrap items-center gap-2">
          <span
            class="rounded-full bg-[rgba(46,34,10,0.04)] px-3 py-1 text-[11px] font-extrabold text-neutral-600"
          >
            {{ item.categoryName || '미분류' }}
          </span>

          <span
            class="rounded-full px-3 py-1 text-[11px] font-extrabold"
            :class="statusClass"
          >
            {{ statusLabel }}
          </span>

          <span
            class="rounded-full bg-[rgba(242,210,33,0.1)] px-3 py-1 text-[11px] font-extrabold text-[#8A6A00]"
          >
            {{ cycleLabel }}
          </span>
        </div>

        <div class="mt-3 flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
          <div class="min-w-0">
            <h3 class="truncate text-[20px] font-bold tracking-[-0.03em] text-neutral-900">
              {{ item.subscriptionName || '이름 없는 구독' }}
            </h3>

            <div class="mt-3 grid gap-1 text-sm leading-6 text-neutral-500">
              <p>
                다음 결제일 :
                <span class="font-semibold text-neutral-700">
                  {{ formatDate(item.nextPaymentDate) }}
                </span>
              </p>
              <p>
              시작일 :
                <span class="font-semibold text-neutral-700">
                  {{
                    item.paymentStartDate || item.startDate || item.registeredAt || item.createdAt
                      ? formatDate(item.paymentStartDate || item.startDate || item.registeredAt || item.createdAt)
                      : '-'
                  }}
                </span>
              </p>
            </div>
          </div>

          <div class="grid shrink-0 gap-1 text-left xl:min-w-[180px] xl:text-right">
            <p class="text-xs font-semibold uppercase tracking-[0.08em] text-neutral-400">
              결제 금액
            </p>
            <p class="text-[28px] font-extrabold tracking-[-0.04em] text-neutral-900">
              {{ formatCurrency(item.paymentAmount) }}
            </p>
          </div>
        </div>
<!--
        <div class="mt-4 grid gap-2 text-sm text-neutral-500 md:grid-cols-2">
          <p>
            결제 카드 :
            <span class="font-semibold text-neutral-700">
              {{ item.paymentCardName || '-' }}
            </span>
          </p>
        </div>
-->

      </div>
    </div>
  </button>
</template>