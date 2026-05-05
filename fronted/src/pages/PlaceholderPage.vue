<script setup>
import AppShell from '@/components/layout/AppShell.vue'

defineProps({
  title: {
    type: String,
    default: '준비 중',
  },
  description: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <AppShell>
    <section class="shell-card p-10">
      <p class="text-sm font-semibold text-brand-600">Next step</p>
      <h2 class="mt-2 text-[28px] font-bold tracking-[-0.03em] text-neutral-900">{{ title }}</h2>
      <p class="mt-3 max-w-2xl text-sm leading-7 text-neutral-500">{{ description }}</p>
    </section>
  </AppShell>
</template>
