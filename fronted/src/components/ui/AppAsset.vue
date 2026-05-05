<script setup>
import { computed } from 'vue'
import { resolveAssetImage, cardBadge } from '@/utils/imageAssets'
import AppIcon from '@/components/ui/AppIcon.vue'

const props = defineProps({
  type: { type: String, default: 'service' },
  value: { type: String, default: '' },
  secondaryType: { type: String, default: '' },
  secondaryValue: { type: String, default: '' },
  fallback: { type: String, default: 'sparkles' },
  size: { type: Number, default: 20 },
  alt: { type: String, default: '' },
  wrapperClass: { type: String, default: 'inline-flex items-center justify-center' },
  imageClass: { type: String, default: 'object-contain' },
  iconClass: { type: String, default: '' },
  badgeClass: { type: String, default: 'inline-flex min-w-[40px] items-center justify-center rounded-md px-2 py-1 text-[10px] font-black uppercase tracking-[-0.02em]' },
})

const src = computed(() => {
  return resolveAssetImage(props.type, props.value)
    || resolveAssetImage(props.secondaryType, props.secondaryValue)
    || null
})

const badge = computed(() => {
  if (props.type !== 'card') return null
  return cardBadge(props.value)
})

const label = computed(() => props.alt || props.value || props.secondaryValue || props.type)
</script>

<template>
  <span :class="wrapperClass">
    <img v-if="src" :src="src" :alt="label" :class="imageClass" />
    <span v-else-if="badge" :class="badgeClass" :style="{ backgroundColor: badge.bg, color: badge.color }">{{ badge.text }}</span>
    <AppIcon v-else :name="fallback" :size="size" :class="iconClass" />
  </span>
</template>
