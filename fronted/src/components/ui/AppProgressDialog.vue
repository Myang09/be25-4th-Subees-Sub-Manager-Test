<script setup>
import { computed } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '처리 중입니다' },
  description: { type: String, default: '' },
  percent: { type: Number, default: 0 },
  steps: { type: Array, default: () => [] },
  currentStep: { type: Number, default: 0 },
})

const normalizedPercent = computed(() => Math.max(0, Math.min(100, Number(props.percent || 0))))
</script>

<template>
  <div
    v-if="open"
    class="fixed inset-0 z-[70] flex items-center justify-center bg-[rgba(28,22,11,0.32)] px-4 py-6 backdrop-blur-[2px]"
  >
    <div class="w-full max-w-[420px] rounded-modal border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.98)] p-5 shadow-floating">
      <div class="flex items-start gap-4">
        <span class="mt-0.5 grid h-12 w-12 shrink-0 place-items-center rounded-full bg-brand-50">
          <span class="h-6 w-6 rounded-full border-[3px] border-warning border-r-transparent animate-spin"></span>
        </span>
        <div class="min-w-0 flex-1">
          <p class="text-sm font-semibold text-warning">AI 추천 실행</p>
          <h2 class="mt-1 text-[22px] font-bold tracking-[-0.03em] text-neutral-900">{{ title }}</h2>
          <p v-if="description" class="mt-2 text-sm leading-6 text-neutral-500">{{ description }}</p>
        </div>
      </div>

      <div class="mt-5 overflow-hidden rounded-full bg-brand-100/80">
        <div class="h-2.5 rounded-full bg-brand-500 transition-all duration-300" :style="{ width: `${normalizedPercent}%` }"></div>
      </div>
      <div class="mt-2 flex items-center justify-between text-xs font-semibold text-neutral-400">
        <span>진행률</span>
        <span>{{ normalizedPercent }}%</span>
      </div>

      <div v-if="steps.length" class="mt-4 grid gap-2">
        <div
          v-for="(step, index) in steps"
          :key="step"
          class="flex items-center gap-3 rounded-[16px] border px-3 py-2 text-sm transition"
          :class="index + 1 < currentStep
            ? 'border-[rgba(138,106,0,0.12)] bg-brand-50 text-neutral-900'
            : index + 1 === currentStep
              ? 'border-[rgba(138,106,0,0.28)] bg-white text-neutral-900'
              : 'border-[rgba(46,34,10,0.08)] bg-white/70 text-neutral-400'"
        >
          <span
            class="grid h-6 w-6 shrink-0 place-items-center rounded-full text-xs font-extrabold"
            :class="index + 1 < currentStep
              ? 'bg-brand-500 text-[#231A07]'
              : index + 1 === currentStep
                ? 'bg-warning/10 text-warning'
                : 'bg-neutral-100 text-neutral-400'"
          >
            {{ index + 1 < currentStep ? '✓' : index + 1 }}
          </span>
          <span>{{ step }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
