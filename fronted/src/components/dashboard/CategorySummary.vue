<script setup>
import { computed } from 'vue'
const props = defineProps({
  items: { type: Array, default: () => [] },
  highestCategory: { type: Object, default: null },
  totalAmount: { type: Number, default: 0 },
})
const formatCurrency = (value) => `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`
const donutStyle = computed(() => {
  if (!props.items.length) return { background: 'conic-gradient(#E7DEC9 0deg 360deg)' }
  let start = 0
  const segments = props.items.map((item) => {
    const end = start + ((item.ratio || 0) / 100) * 360
    const segment = `${item.colorHex} ${start}deg ${end}deg`
    start = end
    return segment
  })
  return { background: `conic-gradient(${segments.join(', ')})` }
})
</script>

<template>
  <section class="shell-card p-6">
    <div class="flex flex-wrap items-start justify-between gap-4 border-b border-[rgba(46,34,10,0.08)] pb-5">
      <div>
        <p class="eyebrow-label">카테고리별 소비 분석</p>
        <h2 class="mt-2 text-xl font-bold tracking-[-0.03em] text-neutral-900">이번 달에서 어떤 분야에 가장 많이 구독료가 나갔는지 확인하는 곳</h2>
        <p class="mt-2 text-sm leading-6 text-neutral-500">도넛형 구조를 유지하되, 아래에는 금액과 구독 건수까지 같이 배치했습니다.</p>
      </div>
      <span v-if="highestCategory" class="rounded-full bg-[rgba(242,210,33,0.16)] px-3 py-1 text-xs font-bold text-warning">최고 비중 · {{ highestCategory.categoryName }}</span>
    </div>

    <div class="mt-6 grid gap-6 xl:grid-cols-[210px_minmax(0,1fr)] xl:items-center">
      <div class="mx-auto grid gap-4 text-center">
        <div class="relative mx-auto h-44 w-44 rounded-full" :style="donutStyle">
          <div class="absolute inset-[20px] grid place-items-center rounded-full bg-[rgba(255,253,249,0.95)] shadow-soft">
            <div>
              <p class="eyebrow-label">총 예상 지출</p>
              <p class="mt-2 text-[22px] font-bold tracking-[-0.04em] text-neutral-900">{{ formatCurrency(totalAmount) }}</p>
              <p class="mt-2 text-xs text-neutral-500">{{ highestCategory?.categoryName }} {{ highestCategory?.ratio }}%</p>
            </div>
          </div>
        </div>
      </div>

      <div class="grid gap-3">
        <article v-for="item in items" :key="item.categoryName" class="rounded-[22px] border border-[rgba(46,34,10,0.08)] bg-brand-50 p-4">
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-start gap-3">
              <span class="mt-1 h-3 w-3 rounded-full" :style="{ backgroundColor: item.colorHex }"></span>
              <div>
                <p class="text-sm font-bold text-neutral-900">{{ item.categoryName }}</p>
                <p class="mt-1 text-xs leading-5 text-neutral-500">{{ item.description }}</p>
              </div>
            </div>
            <p class="text-sm font-bold text-neutral-900">{{ item.ratio }}%</p>
          </div>
          <div class="mt-3 h-2 overflow-hidden rounded-full bg-[rgba(46,34,10,0.08)]">
            <div class="h-full rounded-full" :style="{ width: `${item.ratio}%`, backgroundColor: item.colorHex }"></div>
          </div>
          <div class="mt-3 flex items-center justify-between text-sm text-neutral-500">
            <span>{{ formatCurrency(item.totalAmount) }}</span>
            <span>{{ item.subscriptionCount }}건</span>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>
