<script setup>
import { serviceImage } from '@/utils/imageAssets'

const props = defineProps({
  dateLabel: {
    type: String,
    default: '',
  },
  totalAmount: {
    type: Number,
    default: 0,
  },
  payments: {
    type: Array,
    default: () => [],
  },
})

const formatCurrency = (value) =>
  `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`

const getCategoryLabel = (categoryName = '') => {
  const map = {
    Music: '음악',
    OTT: 'OTT',
    AI: 'AI',
    Cloud: '클라우드',
    Etc: '기타',
    Other: '기타',
  }

  return map[categoryName] || categoryName || '기타'
}

const getCardBadgeLabel = (payment) => {
  return (
    payment.cardCompany ||
    payment.cardIssuer ||
    payment.cardBrand ||
    '카드'
  )
}

const getCardBadgeClass = (cardName = '') => {
  const label = String(cardName).toLowerCase()

  if (label.includes('toss')) return 'bg-[#156EF3] text-white'
  if (label.includes('shinhan')) return 'bg-[#1A73E8] text-white'
  if (label.includes('kb')) return 'bg-[#6B5B2A] text-white'
  if (label.includes('hyundai')) return 'bg-[#111111] text-white'
  if (label.includes('samsung')) return 'bg-[#1C4FD7] text-white'
  if (label.includes('lotte')) return 'bg-[#E0002A] text-white'
  if (label.includes('hana')) return 'bg-[#009178] text-white'
  if (label.includes('woori')) return 'bg-[#0067AC] text-white'
  if (label.includes('nh')) return 'bg-[#1D8F3A] text-white'

  return 'bg-[#156EF3] text-white'
}
</script>

<template>
  <section class="shell-card flex max-h-[560px] flex-col p-5">
    <p class="text-sm font-semibold text-[#8A6A00]">선택 날짜 결제 정보</p>

    <h2 class="mt-3 text-[22px] font-black tracking-[-0.03em] text-neutral-900">
      {{ dateLabel }}
    </h2>

    <p class="mt-2 text-sm text-neutral-300">
      총 {{ payments.length }}건 · {{ formatCurrency(totalAmount) }}
    </p>

    <div class="mt-5 min-h-0 flex-1 border-t border-[rgba(46,34,10,0.08)] pt-5">
      <div
        v-if="payments.length"
        class="h-full overflow-y-auto pr-1"
        style="scrollbar-gutter: stable;"
      >
        <div class="grid gap-3">
          <article
            v-for="payment in payments"
            :key="payment.id"
            class="rounded-[28px] border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.72)] p-4"
          >
            <div class="flex items-start gap-3">
              <div
                class="flex h-14 w-14 shrink-0 items-center justify-center rounded-full border border-[rgba(46,34,10,0.08)] bg-white"
              >
                <img
                  :src="serviceImage(payment.name) || '/image/subees-logo.png'"
                  :alt="payment.name"
                  class="h-8 w-8 rounded-full object-cover"
                />
              </div>

              <div class="min-w-0 flex-1">
                <div class="min-w-0">
                  <p class="text-[16px] font-black leading-[1.25] text-neutral-900 break-words">
                    {{ payment.name }}
                  </p>

                  <div class="mt-2 flex items-center gap-2">
                    <span
                      class="inline-flex shrink-0 items-center rounded-full bg-[#156EF3] px-2 py-1 text-[10px] font-black leading-none text-white"
                    >
                      카드
                    </span>

                    <p class="min-w-0 flex-1 text-[12px] font-medium leading-[1.35] text-neutral-500 break-words">
                      {{ payment.cardLabel || '등록 카드로 결제 예정' }}
                    </p>
                  </div>
                </div>

                <div class="mt-5 flex items-end justify-between gap-3">
                  <p class="text-[13px] font-semibold text-neutral-500">
                    결제 금액
                  </p>
                  <p class="text-[18px] font-black tracking-[-0.02em] text-neutral-900">
                    {{ formatCurrency(payment.amount) }}
                  </p>
                </div>
              </div>
            </div>
          </article>
        </div>
      </div>

      <div
        v-else
        class="rounded-[24px] border border-dashed border-[rgba(46,34,10,0.10)] bg-[rgba(255,253,247,0.55)] px-5 py-10 text-center"
      >
        <p class="text-sm font-semibold text-neutral-500">
          선택한 날짜에 예정된 결제가 없습니다.
        </p>
      </div>
    </div>
  </section>
</template>