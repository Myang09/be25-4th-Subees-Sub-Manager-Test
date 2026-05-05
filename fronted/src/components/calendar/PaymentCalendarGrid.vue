<script setup>
defineProps({
  weekdayLabels: {
    type: Array,
    default: () => [],
  },
  days: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['select'])

const getItems = (itemNames = '') =>
  String(itemNames)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)

const getVisibleItems = (itemNames = '') => getItems(itemNames).slice(0, 2)


const getEventClass = (name = '') => {
  const label = name.toLowerCase()

  if (
    label.includes('chatgpt') ||
    label.includes('claude') ||
    label.includes('gemini')
  ) {
    return 'bg-[rgba(228,238,255,0.98)] text-[#28496B] before:bg-[#4B8AF4]'
  }

  if (
    label.includes('coupang') ||
    label.includes('배민') ||
    label.includes('tving') ||
    label.includes('coupangplay')
  ) {
    return 'bg-[rgba(229,241,232,0.98)] text-[#35523D] before:bg-[#28B463]'
  }

  if (
    label.includes('youtube') ||
    label.includes('netflix') ||
    label.includes('disney')
  ) {
    return 'bg-[rgba(255,239,220,0.98)] text-[#6A512F] before:bg-[#E3A759]'
  }

  return 'bg-[rgba(242,242,242,0.98)] text-neutral-700 before:bg-[rgba(46,34,10,0.24)]'
}
</script>

<template>
  <div class="overflow-hidden rounded-[30px] border border-[rgba(46,34,10,0.08)] bg-[rgba(255,252,244,0.96)]">
    <div class="grid grid-cols-7 border-b border-[rgba(46,34,10,0.06)] bg-[rgba(255,255,255,0.18)]">
      <div
        v-for="label in weekdayLabels"
        :key="label"
        class="flex h-[44px] items-center justify-center border-r border-[rgba(46,34,10,0.05)] text-[11px] font-extrabold uppercase tracking-[0.16em] text-neutral-400 last:border-r-0"
      >
        {{ label }}
      </div>
    </div>

    <div class="grid grid-cols-7">
      <button
        v-for="day in days"
        :key="day.dateKey"
        type="button"
        class="group relative min-h-[138px] border-r border-b border-[rgba(46,34,10,0.05)] pl-0 pr-[8px] pb-[14px] text-left transition-colors duration-150 last:border-r-0 md:min-h-[138px]"
        :class="[
          day.isSelected
            ? 'bg-[rgba(255,247,214,0.60)] shadow-[inset_0_0_0_1.5px_rgba(88,68,18,0.55)]'
            : day.isCurrentMonth
              ? 'bg-[rgba(255,255,255,0.34)] hover:bg-[rgba(255,255,255,0.58)]'
              : 'bg-[rgba(242,238,228,0.78)] text-neutral-300',
        ]"
        @click="emit('select', day.dateKey)"
      >
        <!-- 날짜/건수: 절대 위치 고정 -->
        <div class="absolute left-0 right-[8px] top-[12px] flex h-[28px] items-start justify-between px-[10px]">
          <span
            class="pt-[1px] text-[14px] font-black leading-none"
            :class="day.isCurrentMonth ? 'text-neutral-900' : 'text-neutral-300'"
          >
            {{ day.label }}
          </span>

          <span
            v-if="day.totalCount"
            class="inline-flex h-[26px] min-w-[28px] shrink-0 items-center justify-center rounded-full px-2 text-[10px] font-extrabold leading-none"
            :class="
              day.isSelected
                ? 'bg-[rgba(138,106,0,0.10)] text-[#7A6100]'
                : 'bg-[rgba(46,34,10,0.05)] text-neutral-500'
            "
          >
            {{ day.totalCount }}건
          </span>
        </div>

        <!-- 이벤트 영역: 아래에서 시작 -->
        <div class="pt-[48px] pr-[8px] flex flex-col gap-[6px]">
          <template v-if="day.itemNames && day.isCurrentMonth">
            <div
              v-for="(item, index) in getVisibleItems(day.itemNames)"
              :key="`${day.dateKey}-${index}-${item}`"
              class="relative w-full overflow-hidden  py-[6px] pl-[10px] pr-[8px] text-left text-[10.5px] font-medium leading-[1.2] before:absolute before:left-0 before:top-0 before:h-full before:w-[3px]"
              :class="getEventClass(item)"
            >
              <div class="truncate">
                {{ item }}
              </div>
            </div>
          </template>
        </div>

        <div
          v-if="day.isSelected"
          class="absolute bottom-[12px] left-[10px] h-[4px] w-[38px] rounded-full bg-[#B38A00]"
        />
      </button>
    </div>
  </div>
</template>