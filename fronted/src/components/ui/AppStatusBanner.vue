<script setup>
import { computed } from 'vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const props = defineProps({
  tone: { type: String, default: 'info' },
  title: { type: String, default: '안내' },
  message: { type: String, default: '' },
})

const toneClass = computed(() => ({
  error: 'border-rose-200 bg-rose-50 text-rose-700',
  success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  info: 'border-amber-200 bg-amber-50 text-[#8A6A00]',
}[props.tone] || 'border-amber-200 bg-amber-50 text-[#8A6A00]'))

const iconName = computed(() => ({
  error: 'help',
  success: 'sparkles',
  info: 'bell',
}[props.tone] || 'bell'))
</script>

<template>
 <section
  class="rounded-card border px-4 py-1.5 text-sm font-medium flex items-center"
  :class="toneClass"
>
  {{ message }}
</section>
</template>
