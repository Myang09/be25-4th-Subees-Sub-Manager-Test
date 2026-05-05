<script setup>
import { computed } from 'vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const props = defineProps({
  notifications: {
    type: Array,
    default: () => [],
  },
  unreadCount: {
    type: Number,
    default: 0,
  },
})

const emit = defineEmits(['close', 'select', 'dismiss', 'open-all'])

const previewNotifications = computed(() => props.notifications.slice(0, 4))

const typeLabel = (value) => ({
  PAYMENT: '결제 알림',
  SYSTEM: '시스템',
}[value] || '알림')
</script>

<template>
  <div class="absolute right-0 top-[calc(100%+12px)] z-50 w-[min(420px,calc(100vw-32px))] overflow-hidden rounded-[28px] border border-[rgba(46,34,10,0.12)] bg-[rgba(255,253,249,0.98)] shadow-[0_28px_60px_rgba(46,34,10,0.18)] backdrop-blur">
    <div class="border-b border-[rgba(46,34,10,0.08)] bg-[linear-gradient(180deg,rgba(242,210,33,0.18),rgba(255,253,249,0.98))] px-5 py-4">
      <div class="flex items-start justify-between gap-3">
        <div>
          <p class="text-xs font-black uppercase tracking-[0.16em] text-[#8A6A00]">알림</p>
          <h3 class="mt-1 text-lg font-black tracking-[-0.03em] text-neutral-900">다가오는 결제 일정만 모아봤어요</h3>
          <p class="mt-2 text-sm leading-6 text-neutral-500">최신 알림이 위에 오도록 정리했습니다. 읽지 않은 알림은 {{ unreadCount }}개입니다.</p>
        </div>
        <button type="button" class="grid h-9 w-9 shrink-0 place-items-center rounded-full border border-[rgba(46,34,10,0.1)] bg-white text-neutral-500 transition hover:text-neutral-900" aria-label="알림 팝업 닫기" @click="emit('close')">
          <AppIcon name="close" :size="16" />
        </button>
      </div>
    </div>

    <div class="max-h-[420px] overflow-y-auto px-3 py-3">
      <div v-if="previewNotifications.length" class="grid gap-3">
        <article v-for="item in previewNotifications" :key="item.notificationId" class="rounded-[22px] border px-4 py-4 transition" :class="item.read ? 'border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.85)]' : 'border-[rgba(242,210,33,0.26)] bg-[rgba(255,248,224,0.92)] shadow-soft'">
          <div class="flex items-start gap-3">
            <div class="mt-0.5 grid h-11 w-11 shrink-0 place-items-center rounded-[16px] border border-[rgba(46,34,10,0.08)] bg-white text-[#8A6A00]">
              <AppIcon name="bell" :size="17" />
            </div>
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <span class="rounded-full bg-white px-2.5 py-1 text-[11px] font-extrabold text-neutral-500 ring-1 ring-[rgba(46,34,10,0.08)]">{{ typeLabel(item.type) }}</span>
                <span class="rounded-full bg-[rgba(46,34,10,0.06)] px-2.5 py-1 text-[11px] font-extrabold text-neutral-500">{{ item.dday || '-' }}</span>
                <span v-if="!item.read" class="rounded-full bg-[rgba(242,210,33,0.2)] px-2.5 py-1 text-[11px] font-extrabold text-[#8A6A00]">새 알림</span>
              </div>
              <button type="button" class="mt-3 block w-full text-left" @click="emit('select', item.notificationId)">
                <p class="text-[15px] font-black tracking-[-0.03em] text-neutral-900">{{ item.title }}</p>
                <p class="mt-1.5 text-sm leading-6 text-neutral-600">{{ item.message }}</p>
              </button>
              <div class="mt-3 flex flex-wrap items-center justify-between gap-2">
                <button type="button" class="text-sm font-bold text-[#8A6A00]" @click="emit('select', item.notificationId)">상세 보기</button>
                <button type="button" class="text-sm font-semibold text-neutral-400 transition hover:text-neutral-700" @click="emit('dismiss', item.notificationId)">닫기</button>
              </div>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="grid min-h-[220px] place-items-center rounded-[24px] border border-dashed border-[rgba(46,34,10,0.12)] bg-[rgba(255,253,247,0.7)] px-6 text-center">
        <div>
          <p class="text-base font-black tracking-[-0.03em] text-neutral-900">표시할 알림이 없습니다</p>
          <p class="mt-2 text-sm leading-6 text-neutral-500">새 알림이 생성되면 이 팝업에서 바로 확인할 수 있습니다.</p>
        </div>
      </div>
    </div>

    <div class="border-t border-[rgba(46,34,10,0.08)] px-4 py-3">
      <button type="button" class="secondary-button w-full" @click="emit('open-all')">알림 페이지 전체 보기</button>
    </div>
  </div>
</template>
