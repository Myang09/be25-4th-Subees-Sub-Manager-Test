<script setup>
const quickLinks = [
  { label: '대시보드', to: '/dashboard' },
  { label: '구독목록', to: '/subscriptions' },
  { label: '구독추가', to: '/subscriptions/new' },
  { label: '결제 캘린더', to: '/calendar' },
  { label: '커뮤니티', to: '/community' },
  { label: '알림', to: '/notifications' },
  { label: '결제 카드', to: '/payment-cards' },
]
</script>

<template>
  <footer class="mt-auto border-t border-[rgba(46,34,10,0.08)] bg-[linear-gradient(180deg,rgba(255,249,226,0.94),rgba(247,241,227,0.92))]">
    <div class="w-full px-[clamp(18px,2vw,28px)] py-5">
      <div class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(360px,max-content)] xl:items-center">
        <div class="min-w-0">
          <p class="text-[11px] font-extrabold uppercase tracking-[0.14em] text-neutral-300">Subscription Utility</p>
          <div class="mt-2 flex flex-wrap items-center gap-x-3 gap-y-1">
            <RouterLink to="/" class="text-[28px] font-black tracking-[-0.05em] text-neutral-900 no-underline">Subees</RouterLink>
            <span class="hidden h-4 w-px bg-[rgba(46,34,10,0.08)] sm:block"></span>
            <p class="text-sm text-neutral-500">구독 관리 서비스를 빠르게 이어서 사용할 수 있습니다.</p>
          </div>
        </div>

        <nav class="flex flex-wrap items-center gap-2.5 xl:justify-end" aria-label="앱 하단 이동">
          <RouterLink v-for="item in quickLinks" :key="item.to" :to="item.to" class="guide-pill !min-h-[34px] !px-3.5 !text-[11px]">
            {{ item.label }}
          </RouterLink>
        </nav>
      </div>

      <div class="mt-4 flex flex-col gap-2 border-t border-[rgba(46,34,10,0.08)] pt-3 text-[11px] text-neutral-400 xl:flex-row xl:items-center xl:justify-between">
        <p>Subees · 구독 관리 서비스 · 아이보리/옐로우/브라운 톤</p>
        <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
          <span>이용안내</span>
          <span class="text-[10px] text-neutral-300">•</span>
          <span>개인정보 안내</span>
          <span class="text-[10px] text-neutral-300">•</span>
          <span>고객지원</span>
        </div>
      </div>
    </div>
  </footer>
</template>
