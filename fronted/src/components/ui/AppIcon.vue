<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, default: 'sparkles' },
  size: { type: Number, default: 20 },
  strokeWidth: { type: Number, default: 1.8 },
})

const paths = {
  home: 'M4 10.5 12 4l8 6.5V20h-5.2v-5.8H9.2V20H4z',
  dashboard: 'M4.5 4.5h6v6h-6zm9 0h6v6h-6zm-9 9h6v6h-6zm9 0h6v6h-6z',
  list: 'M7 7h11M7 12h11M7 17h11M4.5 7h.01M4.5 12h.01M4.5 17h.01',
  plus: 'M12 5v14M5 12h14',
  user: 'M12 12a4 4 0 100-8 4 4 0 000 8zm0 2c-3.6 0-6.7 1.9-8 4.7M20 18.7c-1.3-2.8-4.4-4.7-8-4.7',
  bell: 'M7.5 9.2a4.5 4.5 0 119 0c0 5 2 5.7 2 7.3H5.5c0-1.6 2-2.3 2-7.3ZM10 18.5a2 2 0 004 0',
  search: 'M17.5 17.5 21 21M10.5 18a7.5 7.5 0 100-15 7.5 7.5 0 000 15z',
  logout: 'M16 17l5-5-5-5M21 12H9M12 19H7a2 2 0 01-2-2V7a2 2 0 012-2h5',
  chart: 'M5 18V9m7 9V6m7 12v-5',
  calendar: 'M7 3.5v3M17 3.5v3M4 8.5h16M6 5h12a2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V7a2 2 0 012-2z',
  sparkles: 'M12 3l1.7 4.3L18 9l-4.3 1.7L12 15l-1.7-4.3L6 9l4.3-1.7L12 3zm6.5 11l.8 2 .7.3-2 .8-.8 2-.8-2-2-.8 2-.8.8-2zM5.5 14l1 2.4L9 17.5l-2.5 1L5.5 21l-1-2.5L2 17.5l2.5-1.1 1-2.4z',
  grid: 'M4.5 4.5h6v6h-6zm9 0h6v6h-6zm-9 9h6v6h-6zm9 0h6v6h-6z',
  community: 'M5 6.5h14a2 2 0 012 2v7a2 2 0 01-2 2H11l-4.5 3v-3H5a2 2 0 01-2-2v-7a2 2 0 012-2z',
  bot: 'M9 3h6M12 3v3m-6 6V9a3 3 0 013-3h6a3 3 0 013 3v3m-12 0h12v5a3 3 0 01-3 3H9a3 3 0 01-3-3zm4 4h.01M14 16h.01',
  help: 'M9.09 9a3 3 0 115.82 1c0 2-3 3-3 3M12 17h.01',
  faq: 'M4 6h16M4 12h16M4 18h10',
  settings: 'M12 3.5l1.3 2.7 3 .4-2.2 2.1.5 3-2.6-1.4-2.6 1.4.5-3-2.2-2.1 3-.4L12 3.5zm0 7a2.5 2.5 0 100 5 2.5 2.5 0 000-5z',
  creditcard: 'M4 7h16a1.5 1.5 0 011.5 1.5v7A1.5 1.5 0 0120 17H4a1.5 1.5 0 01-1.5-1.5v-7A1.5 1.5 0 014 7zm0 3h16',
  bookmark: 'M7 4.5h10a1.5 1.5 0 011.5 1.5v14L12 16.5 5.5 20V6A1.5 1.5 0 017 4.5z',
  image: 'M5 6.5h14a1.5 1.5 0 011.5 1.5v8a1.5 1.5 0 01-1.5 1.5H5A1.5 1.5 0 013.5 16V8A1.5 1.5 0 015 6.5zm2.5 7 2.3-2.8 2.8 3.3 2-2.4 2.7 3.4M8 10.3h.01',
  arrowup: 'M12 19V5m0 0-6 6m6-6 6 6',
  chevrondown: 'M6 9l6 6 6-6',
  close: 'M6 6l12 12M18 6 6 18',
}

const d = computed(() => paths[props.name] || paths.sparkles)
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    :stroke-width="strokeWidth"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path :d="d" />
  </svg>
</template>
