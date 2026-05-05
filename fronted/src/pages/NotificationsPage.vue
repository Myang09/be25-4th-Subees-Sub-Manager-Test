<script setup>
import { computed, nextTick, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute } from 'vue-router'
import AppShell from '@/components/layout/AppShell.vue'
import AppIcon from '@/components/ui/AppIcon.vue'
import AppStatePanel from '@/components/ui/AppStatePanel.vue'
import { useNotificationsStore } from '@/stores/notifications'

const route = useRoute()
const notificationsStore = useNotificationsStore()
const { unreadOnly, unreadCount, visibleNotifications, isLoading, errorMessage } = storeToRefs(notificationsStore)

const notificationAllEnabled = computed(() => {
  return localStorage.getItem('notificationAllEnabled') !== 'false'
})

const notificationD3Enabled = computed(() => {
  return localStorage.getItem('notificationD3Enabled') !== 'false'
})

const notificationD0Enabled = computed(() => {
  return localStorage.getItem('notificationD0Enabled') !== 'false'
})

const filteredNotifications = computed(() => {
  if (!notificationAllEnabled.value) return []

  return visibleNotifications.value.filter((item) => {
    if (item.dday === 'D-3') return notificationD3Enabled.value
    if (item.dday === 'D-DAY') return notificationD0Enabled.value
    return true
  })
})

const displayUnreadCount = computed(() => {
  return filteredNotifications.value.filter((item) => !item.read).length
})

const activeNotificationId = computed(() => {
  const raw = Number(route.query.notificationId)
  return Number.isFinite(raw) ? raw : null
})

const typeLabel = (value) => ({ PAYMENT: '결제 알림', SYSTEM: '시스템' }[value] || '알림')

const scrollToActiveNotification = async () => {
  if (!activeNotificationId.value) return

  await nextTick()
  const target = document.getElementById(`notification-card-${activeNotificationId.value}`)
  target?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const handleMarkAllRead = async () => {
  try {
    await notificationsStore.markAllRead()
  } catch (error) {
    // 스토어 상태로 에러 메시지 노출
  }
}

const handleMarkAsRead = async (notificationId) => {
  try {
    await notificationsStore.markAsRead(notificationId)
  } catch (error) {
    // 스토어 상태로 에러 메시지 노출
  }
}

const handleClose = async (notificationId) => {
  try {
    await notificationsStore.closeNotification(notificationId)
  } catch (error) {
    // 스토어 상태로 에러 메시지 노출
  }
}

const handleRefresh = async () => {
  try {
    await notificationsStore.fetchNotifications({ force: true })
  } catch (error) {
    // 스토어 상태로 에러 메시지 노출
  }
}

watch(activeNotificationId, () => {
  scrollToActiveNotification()
}, { immediate: true })

onMounted(async () => {
  try {
    if (!notificationAllEnabled.value) return

    await notificationsStore.ensureFetched()
    await scrollToActiveNotification()
  } catch (error) {
    // 스토어 상태로 에러 메시지 노출
  }
})
</script>

<template>
  <AppShell title="hidden">
    <div class="grid gap-6">
      <section class="shell-card p-7 md:p-8">
        <div class="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
          <div>
            <p class="text-sm font-semibold text-[#8A6A00]">알림 센터</p>
            <h1 class="mt-3 page-title !mt-0 !text-[34px] xl:!text-[42px]">결제 예정 알림을 한 곳에서 확인하세요</h1>
            <p class="mt-3 max-w-4xl body-copy">백엔드 알림 API와 연결해 결제 3일 전, 결제일 당일 알림을 실제 데이터로 표시합니다.</p>
          </div>
          <div class="flex flex-wrap items-center gap-3">
            <button type="button" class="secondary-button !min-h-11 !px-4" @click="notificationsStore.toggleUnreadOnly">{{ unreadOnly ? '전체 보기' : '안 읽은 알림만' }}</button>
            <button type="button" class="primary-button !min-h-11 !px-4" :disabled="!unreadCount || isLoading" @click="handleMarkAllRead">모두 읽음 처리</button>
          </div>
        </div>
      </section>

      

      <AppStatePanel
      
        v-if="isLoading && !visibleNotifications.length"
        title="알림을 불러오는 중입니다"
        description="서버에서 결제 예정 알림 목록을 가져오고 있습니다."
        icon="bell"
        tone="info"
      />

      <AppStatePanel
        v-else-if="!notificationAllEnabled"
        title="알림이 비활성화되어 있습니다"
        description="마이페이지에서 알림을 다시 켜면 알림센터를 확인할 수 있습니다."
        icon="bell"
        tone="info"
      />


      <AppStatePanel
        v-else-if="errorMessage && !visibleNotifications.length"
        title="알림을 불러오지 못했습니다"
        :description="errorMessage"
        icon="bell"
        tone="error"
      >
        <template #actions>
          <button class="primary-button" @click="handleRefresh">다시 시도</button>
        </template>

        
      </AppStatePanel>

      <section v-else class="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
        <aside class="grid gap-4 xl:sticky xl:top-6 xl:self-start">
          <article class="shell-card p-5">
            <p class="text-sm font-semibold text-[#8A6A00]">읽지 않은 알림</p>
            <p class="mt-2 text-[32px] font-black tracking-[-0.04em] text-neutral-900">{{ displayUnreadCount  }}개</p>
            <p class="mt-2 text-sm leading-6 text-neutral-500">읽지 않은 결제 알림을 우선 노출합니다. 읽음 처리 후 헤더 배지도 즉시 갱신됩니다.</p>
          </article>
          <article class="shell-card p-5">
            <p class="text-sm font-semibold text-[#8A6A00]">알림 기준</p>
            <p class="mt-2 text-sm leading-7 text-neutral-600">현재 백엔드에서는 결제 3일 전과 결제 당일 알림을 제공합니다. 닫기 처리한 알림은 목록에서 즉시 제거됩니다.</p>
            <RouterLink to="/calendar" class="secondary-button mt-4 w-full">결제 캘린더 보기</RouterLink>
          </article>
        </aside>

        <section class="section-card">
          <div class="flex items-end justify-between gap-3 border-b border-[rgba(46,34,10,0.08)] pb-5">
            <div>
              <p class="text-sm font-semibold text-[#8A6A00]">알림 목록</p>
              <h2 class="mt-2 section-heading">{{ filteredNotifications.length }}개 항목</h2>
            </div>
            <p class="text-sm text-neutral-500">읽음 처리와 닫기 처리는 모두 서버에 즉시 반영됩니다.</p>
          </div>

          <div v-if="filteredNotifications.length" class="mt-5 grid gap-4">
          <article
            v-for="item in filteredNotifications"
              :id="`notification-card-${item.notificationId}`"
              :key="item.notificationId"
              class="rounded-[24px] border px-5 py-5 transition"
              :class="[
                item.read ? 'border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.8)]' : 'border-[rgba(242,210,33,0.24)] bg-[rgba(255,248,224,0.92)] shadow-soft',
                activeNotificationId === item.notificationId ? 'ring-2 ring-[rgba(242,210,33,0.45)] ring-offset-2 ring-offset-transparent' : '',
              ]"
            >
              <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                <div class="flex min-w-0 gap-4">
                  <div class="grid h-12 w-12 shrink-0 place-items-center rounded-[18px] border border-[rgba(46,34,10,0.08)] bg-white text-[#8A6A00]">
                    <AppIcon name="bell" :size="18" />
                  </div>
                  <div class="min-w-0">
                    <div class="flex flex-wrap items-center gap-2">
                      <span class="rounded-full bg-white px-3 py-1 text-[11px] font-extrabold text-neutral-500 ring-1 ring-[rgba(46,34,10,0.08)]">{{ typeLabel(item.type) }}</span>
                      <span class="rounded-full bg-[rgba(46,34,10,0.06)] px-3 py-1 text-[11px] font-extrabold text-neutral-500">{{ item.dday || '-' }}</span>
                      <span v-if="!item.read" class="rounded-full bg-[rgba(242,210,33,0.2)] px-3 py-1 text-[11px] font-extrabold text-[#8A6A00]">새 알림</span>
                    </div>
                    <h3 class="mt-3 text-lg font-bold tracking-[-0.03em] text-neutral-900">{{ item.title }}</h3>
                    <p class="mt-2 text-sm leading-7 text-neutral-600">{{ item.message }}</p>
                  </div>
                </div>
                <div class="flex shrink-0 flex-wrap items-center gap-2">
                  <RouterLink :to="item.ctaTo" class="secondary-button !min-h-11 !px-4" @click="handleMarkAsRead(item.notificationId)">{{ item.ctaLabel }}</RouterLink>
                  <button v-if="!item.read" type="button" class="tertiary-button !min-h-11 !px-3.5" @click="handleMarkAsRead(item.notificationId)">읽음 처리</button>
                  <button type="button" class="tertiary-button !min-h-11 !px-3.5 text-neutral-500" @click="handleClose(item.notificationId)">닫기</button>
                </div>
              </div>
            </article>
          </div>

          <div v-else class="mt-5 grid min-h-[280px] place-items-center rounded-[24px] border border-dashed border-[rgba(46,34,10,0.12)] bg-[rgba(255,253,247,0.7)] px-6 text-center">
            <div>
              <p class="text-lg font-black tracking-[-0.03em] text-neutral-900">현재 표시할 알림이 없습니다</p>
              <p class="mt-2 text-sm leading-6 text-neutral-500">남은 결제 알림이 없거나 모두 닫힘 처리된 상태입니다. 새 결제 일정이 가까워지면 다시 자동 생성됩니다.</p>
            </div>
          </div>
        </section>
      </section>
    </div>
  </AppShell>
</template>
