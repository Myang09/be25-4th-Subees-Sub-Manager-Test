<script setup>
defineProps({ items: { type: Array, default: () => [] } })
const toneMap = {
  brand: 'border-[rgba(242,210,33,0.18)] bg-[rgba(242,210,33,0.10)]',
  amber: 'border-[rgba(138,106,0,0.18)] bg-[rgba(240,228,186,0.40)]',
  emerald: 'border-[rgba(93,130,96,0.18)] bg-[rgba(93,130,96,0.10)]',
}
</script>

<template>
  <section class="shell-card p-6">
    <div class="border-b border-[rgba(46,34,10,0.08)] pb-5">
      <p class="eyebrow-label">맞춤 인사이트</p>
      <h2 class="mt-2 text-xl font-bold tracking-[-0.03em] text-neutral-900">절감 포인트와 다음 행동을 함께 보여주는 영역</h2>
      <p class="mt-2 text-sm leading-6 text-neutral-500">단순 안내 문구가 아니라, 원인과 액션을 같이 보여주는 보조 패널입니다.</p>
    </div>

    <div class="mt-6 grid gap-4">
      <article v-for="item in items" :key="item.key" class="rounded-[22px] border p-5" :class="toneMap[item.tone] || 'border-[rgba(46,34,10,0.08)] bg-brand-50'">
        <p class="text-base font-bold tracking-[-0.02em] text-neutral-900">{{ item.title }}</p>
        <p class="mt-3 text-sm leading-7 text-neutral-600">{{ item.description }}</p>
        <RouterLink :to="item.to" class="mt-4 inline-flex text-sm font-bold text-warning">{{ item.actionLabel }}</RouterLink>
      </article>
    </div>
  </section>
</template>
