<script setup>
import { computed } from 'vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const props = defineProps({
  tone: { type: String, default: 'empty' },
  title: { type: String, default: '아직 표시할 내용이 없습니다' },
  description: { type: String, default: '필요한 작업을 시작하면 이 영역에 데이터가 채워집니다.' },
  icon: { type: String, default: 'sparkles' },
  compact: { type: Boolean, default: false },
})

const toneClass = computed(() => ({
  empty: 'border-[rgba(46,34,10,0.12)] bg-brand-50 text-neutral-700',
  error: 'border-rose-200 bg-rose-50 text-rose-700',
  info: 'border-amber-200 bg-amber-50 text-[#8A6A00]',
  success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
}[props.tone] || 'border-[rgba(46,34,10,0.12)] bg-brand-50 text-neutral-700'))

const iconWrapClass = computed(() => ({
  empty: 'bg-white text-[#8A6A00]',
  error: 'bg-white text-rose-600',
  info: 'bg-white text-[#8A6A00]',
  success: 'bg-white text-emerald-600',
}[props.tone] || 'bg-white text-[#8A6A00]'))
</script>

<template>
  <div class="rounded-card border border-dashed px-6 text-center" :class="[toneClass, compact ? 'py-7' : 'py-10']">
    <div class="mx-auto grid h-14 w-14 place-items-center rounded-2xl ring-1 ring-[rgba(46,34,10,0.08)]" :class="iconWrapClass">
      <AppIcon :name="icon" :size="22" />
    </div>
    <h3 class="mt-4 text-lg font-bold tracking-[-0.02em] text-neutral-900">{{ title }}</h3>
    <p class="mx-auto mt-2 max-w-2xl text-sm leading-6 text-neutral-500">{{ description }}</p>
    <div v-if="$slots.actions" class="mt-5 flex flex-wrap justify-center gap-3">
      <slot name="actions" />
    </div>
  </div>
</template>
