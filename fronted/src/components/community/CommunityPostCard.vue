<script setup>
defineProps({
  item: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <article class="rounded-card border border-neutral-200 bg-white px-4 py-4 shadow-soft transition hover:-translate-y-0.5 hover:shadow-card">
    <div class="flex flex-wrap items-center justify-between gap-2.5">
      <div class="flex flex-wrap items-center gap-2">
        <span
          v-if="item.isMine"
          class="rounded-full bg-emerald-50 px-2.5 py-1 text-[11px] font-semibold text-emerald-600 ring-1 ring-emerald-100"
        >
          내 게시글
        </span>
      </div>
      <p class="text-[12px] text-neutral-500">{{ item.createdAtLabel }}</p>
    </div>

    <RouterLink :to="`/community/${item.postId}`" class="mt-2.5 block">
      <h3 class="text-[18px] font-bold tracking-[-0.02em] text-neutral-900">
        {{ item.title }}
      </h3>
    </RouterLink>

    <div class="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-neutral-200 pt-3 text-[13px] text-neutral-500">
      <div class="flex flex-wrap items-center gap-3.5">
        <span>{{ item.authorNickname }}</span>
        <span>조회 {{ item.viewCount }}</span>
      </div>
      <RouterLink :to="`/community/${item.postId}`" class="font-semibold text-brand-600">
        상세 보기
      </RouterLink>
    </div>
  </article>
</template>
