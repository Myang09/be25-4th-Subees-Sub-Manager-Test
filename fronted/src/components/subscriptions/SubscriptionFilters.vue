<script setup>
import AppIcon from '@/components/ui/AppIcon.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  categories: {
    type: Array,
    default: () => [],
  },
  selectedCategory: {
    type: String,
    default: '전체',
  },
  selectedSort: {
    type: String,
    default: 'NEXT_PAYMENT_ASC',
  },
})

const emit = defineEmits(['update:modelValue', 'select-category', 'select-sort'])

</script>

<template>
  <section class="shell-card py-0.5 px-1 md:px-6 xl:px-7">
    <div class="flex flex-col gap-4 mt-6 xl:flex-row xl:items-center xl:justify-between">
      <div>
        <p class="text-sm font-semibold text-[#8A6A00]">구독 검색 / 필터</p>
        <h2 class="mt-2 text-[22px] font-bold tracking-[-0.03em] text-neutral-900">서비스명으로 바로 찾고 카테고리로 좁혀보세요</h2>
        <p class="mt-2 text-sm leading-6 text-neutral-500">검색창에서 서비스명을 입력하면 목록을 빠르게 정리할 수 있습니다.</p>
      </div>

      <div class="flex flex-wrap items-center gap-2.5">
        <RouterLink to="/subscriptions/new" class="primary-button !min-h-[46px] !px-4">구독 추가</RouterLink>
        <RouterLink to="/calendar" class="secondary-button !min-h-[46px] !px-4">결제 캘린더</RouterLink>
      </div>
    </div>

    <div class="mt-6 grid gap-3 xl:grid-cols-[minmax(0,1fr)_300px]">
      <label class="rounded-[24px] border border-[rgba(138,106,0,0.18)] bg-white px-4 py-3 shadow-[0_10px_24px_rgba(46,34,10,0.04)]">
        <div class="flex items-center gap-2 text-[12px] font-extrabold uppercase tracking-[0.08em] text-[#8A6A00]">
          <AppIcon name="search" :size="14" />
          서비스명 검색
        </div>
        <div class="mt-2 flex items-center gap-3">
          <input
            :value="props.modelValue"
            type="text"
            class="w-full border-0 bg-transparent pl-6 text-[12px] tracking-[-0.03em] text-neutral-900 outline-none placeholder:text-neutral-400 xl:text-[20px]"
            placeholder="예 :  Netflix , ChatGPT , 멜론"
            @input="emit('update:modelValue', $event.target.value)"
          />
          <button
            v-if="props.modelValue"
            type="button"
            class="inline-flex h-9 inline-flex min-w-[56px] items-center justify-center whitespace-nowrap rounded-full border border-[rgba(46,34,10,0.08)]  bg-brand-50 px-3 text-[12px] font-bold text-neutral-700"
            @click="emit('update:modelValue', '')"
          >
            초기화
          </button>
        </div>
      </label>
<!--
      <label class="rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.92)] px-4 py-3">
        <div class="text-[12px] font-extrabold uppercase tracking-[0.08em] text-neutral-500">정렬 기준</div>
        <div class="mt-2 flex items-center gap-2">
          <select
            class="w-full border-0 bg-transparent p-0 text-[18px] font-bold tracking-[-0.03em] text-neutral-900 outline-none xl:text-[20px]"
            :value="selectedSort"
            @change="emit('select-sort', $event.target.value)"
          >
            <option v-for="option in sortOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
          <AppIcon name="chevrondown" :size="18" />
        </div>
      </label>
      -->
    </div>

    <div class="mt-5 flex flex-wrap gap-3">
      <button
        v-for="category in categories"
        :key="category.categoryName"
        type="button"
        class="chip-button !min-h-[42px] !px-4"
        :class="selectedCategory === category.categoryName ? 'is-selected' : ''"
        @click="emit('select-category', category.categoryName)"
      >
        {{ category.categoryName }}
        <span class="ml-2 rounded-full bg-white px-2 py-0.5 text-xs font-semibold text-neutral-500 ring-1 ring-[rgba(46,34,10,0.08)]">{{ category.count }}</span>
      </button>
    </div>
  </section>
</template>
