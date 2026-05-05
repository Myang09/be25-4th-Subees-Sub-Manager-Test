<script setup>
defineProps({
  selectedSort: { type: String, default: 'latest' },
})

const emit = defineEmits(['select-sort'])

const sortOptions = [
  { label: '최신순', value: 'latest' },
  { label: '인기순', value: 'views' },
]
</script>

<template>
  <div class="flex w-full flex-wrap justify-end gap-2">
    <button
      v-for="option in sortOptions"
      :key="option.value"
      type="button"
      class="chip-button !h-[34px] !min-h-0 !px-3.5 !text-[12px]"
      :class="{ 'is-selected': option.value === selectedSort }"
      @click="emit('select-sort', option.value)"
    >
      {{ option.label }}
    </button>
  </div>
</template>
