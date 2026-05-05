<script setup>
import { computed } from 'vue'
const props = defineProps({ items: { type: Array, default: () => [] }, averageAmount: { type: Number, default: 0 } })
const maxAmount = computed(() => Math.max(...props.items.map((item) => Number(item.totalAmount || 0)), 1))
const latestAmount = computed(() => props.items.at(-1)?.totalAmount ?? 0)
const previousAmount = computed(() => props.items.at(-2)?.totalAmount ?? 0)
const deltaAmount = computed(() => Number(latestAmount.value || 0) - Number(previousAmount.value || 0))
const deltaLabel = computed(() => `${deltaAmount.value >= 0 ? '+' : ''}${new Intl.NumberFormat('ko-KR').format(deltaAmount.value)}원`)
const formatCurrency = (value) => `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`
</script>

<template>
  <section class="shell-card p-6">
    <div class="flex flex-wrap items-start justify-between gap-4 border-b border-[rgba(46,34,10,0.08)] pb-5">
      <div>
        <p class="eyebrow-label">월별 지출 흐름 분석</p>
        <h2 class="mt-2 text-xl font-bold tracking-[-0.03em] text-neutral-900">현재 월부터 이전 월의 구독료를 그래프로 정리</h2>
        <p class="mt-2 text-sm leading-6 text-neutral-500">막대 길이 중심으로 비교하기 쉽게 정리해 증가/감소 흐름을 빠르게 읽을 수 있습니다.</p>
      </div>
      <div class="grid gap-3 sm:grid-cols-2">
        <div class="ghost-card px-4 py-3"><p class="eyebrow-label">최근 변동</p><p class="mt-2 text-base font-bold text-neutral-900">{{ deltaLabel }}</p></div>
        <div class="ghost-card px-4 py-3"><p class="eyebrow-label">6개월 평균</p><p class="mt-2 text-base font-bold text-neutral-900">{{ formatCurrency(averageAmount) }}</p></div>
      </div>
    </div>

    <div class="mt-8 grid grid-cols-6 items-end gap-4">
      <article v-for="item in items" :key="item.key || item.monthLabel" class="flex flex-col items-center gap-3 text-center">
        <div class="relative flex h-64 w-full items-end rounded-[22px] bg-brand-100 p-2">
          <div class="w-full rounded-[16px] transition-all" :class="item.isCurrent ? 'bg-neutral-900 shadow-card' : 'bg-brand-500'" :style="{ height: `${Math.max((Number(item.totalAmount || 0) / maxAmount) * 100, 18)}%` }"></div>
          <span v-if="item.isCurrent" class="absolute left-1/2 top-3 -translate-x-1/2 rounded-full bg-white px-3 py-1 text-[11px] font-bold text-neutral-900 shadow-soft">현재 월</span>
        </div>
        <div>
          <p class="text-sm font-bold text-neutral-900">{{ item.monthLabel }}</p>
          <p class="mt-1 text-xs text-neutral-500">{{ formatCurrency(item.totalAmount) }}</p>
        </div>
      </article>
    </div>
  </section>
</template>
