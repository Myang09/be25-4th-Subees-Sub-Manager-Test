<script setup>
defineProps({
  stats: {
    type: Array,
    default: () => [],
  },
})
</script>

<template>
  <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
    <article v-for="stat in stats" :key="stat.label" class="shell-card p-5">
      <p class="text-xs font-semibold uppercase tracking-[0.08em] text-neutral-500">{{ stat.label }}</p>
      <p class="mt-3 text-[28px] font-bold tracking-[-0.03em] text-neutral-900">{{ stat.value }}</p>
      <p class="mt-2 text-sm leading-6 text-neutral-500">{{ stat.description }}</p>
    </article>
  </section>
</template>
