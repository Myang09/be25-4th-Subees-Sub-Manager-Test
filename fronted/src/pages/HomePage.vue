<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDashboardStore } from '@/stores/dashboard'
import BrandLockup from '@/components/common/BrandLockup.vue'
import AppAsset from '@/components/ui/AppAsset.vue'
import AppIcon from '@/components/ui/AppIcon.vue'
import PublicFooter from '@/components/layout/PublicFooter.vue'
import { animateScrollToTop } from '@/utils/smoothScroll'

const router = useRouter()
const authStore = useAuthStore()
const dashboardStore = useDashboardStore()

const isAuthed = computed(() => authStore.isLoggedIn)
const showScrollTop = ref(false)
let scrollAnimationId = null

const guestPreviewItems = [
  { subscriptionId: 'guest-1', subscriptionName: '넷플릭스', nextPaymentDate: '04/18', paymentAmount: 17000, dDay: 2 },
  { subscriptionId: 'guest-2', subscriptionName: 'ChatGPT Plus', nextPaymentDate: '04/21', paymentAmount: 29000, dDay: 5 },
  { subscriptionId: 'guest-3', subscriptionName: '유튜브 프리미엄', nextPaymentDate: '04/24', paymentAmount: 14900, dDay: 8 },
  { subscriptionId: 'guest-4', subscriptionName: '쿠팡와우', nextPaymentDate: '04/27', paymentAmount: 7890, dDay: 11 },
]

const homePreviewStatus = ref('idle')

const handleWindowScroll = () => {
  showScrollTop.value = window.scrollY > 520
}

const summaryStats = computed(() => {
  if (!isAuthed.value) {
    return [
      { label: '이번 달 결제 예정', value: '248,000원', note: '예상 결제 총액' },
      { label: '결제일 추적', value: '04/18', note: '가장 가까운 일정' },
      { label: '지출 흐름 확인', value: '12개', note: '등록된 구독 기준' },
      { label: '절감 가능', value: '12,000원', note: '중복·미사용 추정' },
    ]
  }

  if (homePreviewStatus.value === 'loading' || !dashboardStore.hasFetched) {
    return [
      { label: '이번 달 결제 예정', value: '불러오는 중', note: '개인화된 요약 준비 중' },
      { label: '결제일 추적', value: '-', note: '가장 가까운 일정 계산 중' },
      { label: '지출 흐름 확인', value: '-', note: '등록된 구독 확인 중' },
      { label: '절감 가능', value: '-', note: '중복·미사용 추정 계산 중' },
    ]
  }

  if (homePreviewStatus.value === 'error') {
    return [
      { label: '이번 달 결제 예정', value: '-', note: '요약 데이터를 불러오지 못했습니다.' },
      { label: '결제일 추적', value: '-', note: '잠시 후 다시 확인해주세요.' },
      { label: '지출 흐름 확인', value: '-', note: '대시보드에서 다시 조회할 수 있습니다.' },
      { label: '절감 가능', value: '-', note: '서버 응답을 기다리는 중입니다.' },
    ]
  }

  return [
    { label: '이번 달 결제 예정', value: formatCurrency(dashboardStore.userSummary.monthlyExpectedAmount), note: '예상 결제 총액' },
    { label: '결제일 추적', value: dashboardStore.userSummary.nextPaymentDate, note: dashboardStore.nextPayment.subscriptionName },
    { label: '지출 흐름 확인', value: `${dashboardStore.userSummary.activeSubscriptionCount}개`, note: '등록된 구독 기준' },
    { label: '절감 가능', value: formatCurrency(dashboardStore.userSummary.potentialSavingsAmount), note: '중복·미사용 추정' },
  ]
})

const previewItems = computed(() => {
  if (!isAuthed.value) {
    return guestPreviewItems.map((item) => ({
      ...item,
      badge: `D-${item.dDay}`,
    }))
  }

  return dashboardStore.upcomingSubscriptions.slice(0, 4).map((item) => ({
    ...item,
    badge: item.dDay == null ? '-' : item.dDay === 0 ? 'D-DAY' : item.dDay < 0 ? `D+${Math.abs(item.dDay)}` : `D-${item.dDay}`,
  }))
})

const hasPreviewItems = computed(() => previewItems.value.length > 0)
const isHomePreviewLoading = computed(() => isAuthed.value && homePreviewStatus.value === 'loading')
const hasHomePreviewError = computed(() => isAuthed.value && homePreviewStatus.value === 'error')

const introCopy = computed(() => {
  if (!isAuthed.value) {
    return '사용하던 여러 구독을 한 곳에 모아, 이번 달 결제 금액과 가장 가까운 결제일을 바로 확인할 수 있습니다.'
  }

  if (isHomePreviewLoading.value || !dashboardStore.hasFetched) {
    return `${authStore.nickname || '사용자'}님의 등록된 구독을 불러와 홈 화면 프리뷰를 실제 결제 데이터 기준으로 준비하고 있습니다.`
  }

  if (hasHomePreviewError.value) {
    return '홈 화면 프리뷰를 실제 데이터로 불러오지 못했습니다. 잠시 후 다시 시도하거나 대시보드에서 최신 값을 확인해주세요.'
  }

  return `${authStore.nickname || '사용자'}님의 이번 달 예상 결제 금액은 ${formatCurrency(dashboardStore.userSummary.monthlyExpectedAmount)}이며, 가장 가까운 결제일은 ${dashboardStore.userSummary.nextPaymentDate}입니다.`
})

const heroHighlights = computed(() => {
  if (!isAuthed.value) {
    return ['이번 달 결제 예정 확인', '가장 가까운 결제일 추적', '지출 흐름 한눈에 확인']
  }

  if (isHomePreviewLoading.value || !dashboardStore.hasFetched) {
    return ['실제 구독 데이터 동기화 중', '가장 가까운 결제일 계산 중', '홈 프리뷰 개인화 적용 중']
  }

  if (hasHomePreviewError.value) {
    return ['홈 프리뷰 재시도 필요', '대시보드에서 최신값 확인', '결제 캘린더는 계속 이용 가능']
  }

  return [
    `${authStore.nickname || '사용자'}님 구독 요약`,
    `${dashboardStore.userSummary.activeSubscriptionCount}개 활성 구독`,
    `절감 가능 ${formatCurrency(dashboardStore.userSummary.potentialSavingsAmount)}`,
  ]
})

const featureCards = [
  { title: '구독 등록', desc: '서비스명, 결제주기, 금액을 빠르게 입력해 바로 등록할 수 있습니다.', fallback: 'plus' },
  { title: '결제일 알림', desc: '다가오는 결제 예정일을 모아서 빠르게 확인할 수 있습니다.', fallback: 'bell' },
  { title: '캘린더', desc: '날짜별 결제 일정과 월간 흐름을 한 화면에서 확인할 수 있습니다.', fallback: 'calendar' },
  { title: '지출 분석', desc: '카테고리별 소비 금액과 구독 분포를 한눈에 볼 수 있습니다.', fallback: 'chart' },
  { title: '카테고리 분석', desc: '어떤 카테고리에 지출이 몰려 있는지 비중과 흐름을 기준으로 바로 확인할 수 있습니다.', fallback: 'chart' },
  { title: '절약 제안', desc: '중복 결제나 사용 빈도가 낮은 구독을 바탕으로 절약 포인트를 빠르게 확인할 수 있습니다.', fallback: 'sparkles' },
]

const categories = [
  {
    key: 'ott',
    label: 'OTT',
    headline: '실제 제공 중인 OTT 서비스를 바로 확인할 수 있어요',
    description: '구독 등록 화면에서 선택 가능한 OTT 항목만 기준으로 맞췄습니다. 영상 구독 서비스를 한 카드에서 빠르게 확인할 수 있습니다.',
    accent: 'rgba(186,107,82,0.14)',
    services: ['Netflix', 'Tving', 'Disney+', 'CoupangPlay', 'Watcha', 'Laftel', 'Wave', 'AppleTv'],
    layout: 'xl:col-span-6',
  },
  {
    key: 'music',
    label: 'Music',
    headline: '실제 제공 중인 음악 구독 항목을 한 번에 확인할 수 있어요',
    description: '구독 추가 단계에서 바로 선택 가능한 음악 서비스만 반영했습니다. 자주 쓰는 음원 서비스를 한곳에 정리할 수 있습니다.',
    accent: 'rgba(93,130,96,0.14)',
    services: ['Melon', 'AppleMusic', 'Spotify', 'YoutubeMusic', 'Flo', 'Genie', 'Vibe'],
    layout: 'xl:col-span-6',
  },
  {
    key: 'ai',
    label: 'AI',
    headline: '실제 제공 중인 AI 구독 서비스만 따로 모았습니다',
    description: 'AI 추천과 함께 사용하는 핵심 AI 서비스 기준으로 구성했습니다. 현재 등록 가능한 항목과 동일한 목록으로 보여줍니다.',
    accent: 'rgba(138,106,0,0.14)',
    services: ['ChatGpt', 'Gemini', 'Claude'],
    layout: 'xl:col-span-4',
  },
  {
    key: 'other',
    label: 'Others',
    headline: '기타 카테고리도 실제 제공 서비스 기준으로 모두 반영했습니다',
    description: '생활형 멤버십, 생산성 도구, 클라우드 서비스를 따로 분리하지 않고 실제 Others 카테고리 항목 전체를 그대로 보여줍니다.',
    accent: 'rgba(153,140,113,0.16)',
    services: ['배민클럽', '카카오톡서랍', '유튜브프리미엄', '쿠팡와우', '이모티콘플러스', '인텔리제이', 'Icloud+', '컬리', '네이버멤버십', 'GoogleDrive', 'Microsoft 365 Personal', 'Notion', 'Adobe'],
    layout: 'xl:col-span-8',
  },
]

const steps = [
  { n: '1', title: '구독 등록', desc: '카테고리와 서비스, 금액, 결제 주기를 한 번에 입력합니다.', detail: '대표 로고 선택 + 직접 입력 지원', image: '/image/home/upload.png', imageAlt: '구독 등록 흐름 이미지' },
  { n: '2', title: '카드 연결', desc: '토스·현대·신한 등 실제 카드 이미지로 결제수단을 정리합니다.', detail: '결제 카드와 시작일을 함께 저장', image: '/image/home/connect.png', imageAlt: '카드 연결 흐름 이미지' },
  { n: '3', title: '월간 확인', desc: '달력에서 날짜별 결제 아이콘과 총액을 빠르게 훑습니다.', detail: '선택 날짜 상세 패널 즉시 확인', image: '/image/home/check.png', imageAlt: '월간 확인 흐름 이미지' },
  { n: '4', title: '정리 판단', desc: '대시보드에서 카테고리 비중과 가까운 결제 일정을 확인합니다.', detail: '과한 지출 카테고리부터 점검', image: '/image/home/ok.png', imageAlt: '정리 판단 흐름 이미지' },
]

const quickMenus = [
  { label: '구독 추가', desc: '구독 및 카드 정보를 빠르게 등록', meta: '바로 등록', to: '/subscriptions/new', icon: 'plus' },
  { label: '결제내역 상세', desc: '등록된 구독과 결제 정보를 한눈에 확인', meta: '목록 확인', to: '/subscriptions', icon: 'list' },
  { label: '결제일정 상세', desc: '날짜별 결제 일정을 빠르게 확인', meta: '일정 확인', to: '/calendar', icon: 'calendar' },
  { label: '분석 추천', desc: 'AI 추천과 지출 분석 메뉴로 이동', meta: '추천 확인', to: '/ai-recommendations', icon: 'sparkles' },
]

const faqs = [
  { q: '구독은 어디서 등록하나요?', a: '구독 추가 메뉴에서 카테고리, 서비스, 카드, 결제일을 순서대로 입력하면 됩니다.' },
  { q: '카드 이미지를 고를 수 있나요?', a: '결제 카드 메뉴에서 직접 카드 이미지를 선택해 등록할 수 있습니다.' },
  { q: '로그인 후 홈 화면 내용이 달라지나요?', a: '로그인 후에는 실제 등록한 구독 데이터가 대시보드와 연동되어 표시됩니다.' },
  { q: '로그인 전 홈 화면 데이터는 실제인가요?', a: '로그인 전에는 서비스 소개용 예시 프리뷰 데이터가 표시됩니다.' },
]

const go = (path, needAuth = false) => {
  if (needAuth && !isAuthed.value) {
    router.push({ path: '/login', query: { redirect: path } })
    return
  }
  router.push(path)
}

const start = () => {
  router.push(isAuthed.value ? '/subscriptions' : '/signup')
}

const openSupportAction = () => {
  router.push(isAuthed.value ? '/dashboard' : '/login')
}

const handleLogout = async () => {
  await authStore.logout()
  homePreviewStatus.value = 'idle'
  dashboardStore.resetState()
  router.push('/')
}

const formatCurrency = (value) => `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`


const syncHomePreview = async ({ force = false } = {}) => {
  if (!isAuthed.value) {
    homePreviewStatus.value = 'idle'
    dashboardStore.resetState()
    return
  }

  try {
    homePreviewStatus.value = 'loading'
    await dashboardStore.fetchDashboard({ force })
    homePreviewStatus.value = 'ready'
  } catch (error) {
    homePreviewStatus.value = 'error'
  }
}

const scrollToTop = () => {
  if (scrollAnimationId) cancelAnimationFrame(scrollAnimationId)
  scrollAnimationId = animateScrollToTop({ duration: 680 })
}

onMounted(() => {
  handleWindowScroll()
  window.addEventListener('scroll', handleWindowScroll, { passive: true })
  syncHomePreview()
})

watch(isAuthed, (loggedIn, wasLoggedIn) => {
  if (loggedIn && !wasLoggedIn) {
    syncHomePreview({ force: true })
    return
  }

  if (!loggedIn) {
    homePreviewStatus.value = 'idle'
    dashboardStore.resetState()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleWindowScroll)
  if (scrollAnimationId) cancelAnimationFrame(scrollAnimationId)
})
</script>

<template>
  <div class="min-h-screen">
    <header class="sticky top-0 z-40 border-b border-[rgba(46,34,10,0.08)] bg-[rgba(255,249,230,0.82)] backdrop-blur">
      <div class="mx-auto flex w-full max-w-publicShell items-center justify-between gap-4 px-6 py-3 xl:px-8 2xl:px-10">
        <RouterLink to="/" class="min-w-0">
          <BrandLockup eyebrow="Subscription Utility" title="Subees" subtitle="구독·결제·캘린더를 한 곳에 정리해요" :size="44" />
        </RouterLink>

        <nav class="flex flex-wrap items-center justify-end gap-2.5">
          <button v-if="isAuthed" class="secondary-button !min-h-10 !px-4" @click="go('/dashboard')">대시보드</button>
          <button v-if="isAuthed" class="tertiary-button !min-h-10 !px-4 text-neutral-700" @click="handleLogout">로그아웃</button>
          <template v-else>
            <RouterLink to="/login" class="secondary-button !min-h-10 !px-4">로그인</RouterLink>
            <RouterLink to="/signup" class="primary-button !min-h-10 !px-4">회원가입</RouterLink>
          </template>
        </nav>
      </div>
    </header>

    <div class="px-6 pt-6 pb-8 xl:px-8 2xl:px-10">
      <div class="public-shell home-shell">
        <section class="section-card overflow-hidden">
          <div class="flex flex-col gap-6 xl:flex-row xl:items-start xl:justify-between 2xl:gap-7">
            <div class="min-w-0 flex-1">
              <div class="mt-1 inline-flex rounded-full bg-[rgba(242,210,33,0.16)] px-3 py-1 text-xs font-extrabold text-[#8A6A00]">Subees Preview</div>
              <h1 class="mt-5 text-[34px] font-bold leading-[1.14] tracking-[-0.05em] text-neutral-900 lg:text-[52px]">
                다양한 구독정보를 한곳에서,<br class="hidden lg:block" />Subees
              </h1>
              <p class="mt-4 max-w-[760px] text-[16px] leading-8 text-neutral-600">{{ introCopy }}</p>

              <div class="mt-5 flex flex-wrap gap-2.5">
                <span
                  v-for="tag in heroHighlights"
                  :key="tag"
                  class="inline-flex min-h-[36px] items-center rounded-full border border-[rgba(46,34,10,0.08)] bg-white/70 px-4 text-[13px] font-bold text-neutral-700"
                >
                  {{ tag }}
                </span>
              </div>

              <div class="mt-7 flex flex-wrap items-center gap-3">
                <button class="primary-button" @click="start">{{ isAuthed ? '구독 목록 보기' : '무료로 시작하기' }}</button>
                <button class="secondary-button" @click="openSupportAction">{{ isAuthed ? '지출 분석 보기' : '로그인' }}</button>
                <button v-if="isAuthed" class="tertiary-button" @click="go('/calendar', true)">결제 일정 보기</button>
              </div>
              <p class="mt-3 cta-helper">{{ isAuthed ? '등록된 구독과 지출 흐름을 한 화면에서 확인한 뒤 필요한 메뉴로 바로 이동할 수 있습니다.' : '회원가입 후 바로 로그인해 구독 목록과 지출 흐름을 확인할 수 있습니다.' }}</p>

              <div class="mt-7 grid gap-3 md:grid-cols-2 xl:grid-cols-4 2xl:gap-4">
                <article v-for="stat in summaryStats" :key="stat.label" class="ghost-card p-5">
                  <p class="text-[13px] font-bold text-neutral-500">{{ stat.label }}</p>
                  <strong class="mt-3 block text-[26px] font-bold tracking-[-0.03em] text-neutral-900">{{ stat.value }}</strong>
                  <p class="mt-3 meta-copy">{{ stat.note }}</p>
                </article>
              </div>
            </div>

            <aside class="shell-card bg-[linear-gradient(180deg,rgba(242,210,33,0.14),rgba(255,253,247,0.94))] p-6 xl:w-[378px] xl:shrink-0 2xl:w-[410px]">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <p class="eyebrow-label">Preview</p>
                  <h2 class="mt-2 text-[24px] font-bold tracking-[-0.04em] text-neutral-900">{{ isAuthed ? '실제 요약을 한눈에 확인' : '실제 요약을 한눈에 확인' }}</h2>
                </div>
                <span class="chip-button is-selected !min-h-[30px] !px-3 !text-xs">{{ isAuthed ? '실데이터' : '미리보기' }}</span>
              </div>

              <div v-if="isHomePreviewLoading" class="mt-5 rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-white px-5 py-6 text-center">
                <p class="text-sm font-bold text-neutral-900">실제 구독 데이터를 불러오는 중입니다</p>
                <p class="mt-2 text-sm leading-6 text-neutral-500">등록한 구독의 결제 일정과 이번 달 금액을 홈 화면 프리뷰에 반영하고 있습니다.</p>
              </div>

              <div v-else-if="hasHomePreviewError" class="mt-5 rounded-[24px] border border-[rgba(186,107,82,0.18)] bg-[rgba(255,247,244,0.92)] px-5 py-6 text-center">
                <p class="text-sm font-bold text-neutral-900">홈 화면 프리뷰를 불러오지 못했습니다</p>
                <p class="mt-2 text-sm leading-6 text-neutral-500">대시보드에서 다시 조회하거나 잠시 후 새로고침해 주세요.</p>
                <button class="secondary-button mt-4 !min-h-[44px] !px-4" @click="syncHomePreview({ force: true })">다시 불러오기</button>
              </div>

              <div v-else-if="hasPreviewItems" class="mt-5 grid gap-3">
                <button
                  v-for="item in previewItems"
                  :key="item.subscriptionId"
                  type="button"
                  class="flex items-center justify-between gap-3 rounded-[22px] border border-[rgba(46,34,10,0.08)] bg-white px-4 py-4 text-left transition hover:-translate-y-0.5 hover:shadow-soft"
                  @click="go('/calendar', true)"
                >
                  <div class="flex min-w-0 items-center gap-3">
                    <span class="grid h-11 w-11 place-items-center rounded-2xl bg-brand-50 ring-1 ring-[rgba(46,34,10,0.08)]">
                      <AppAsset
                        type="service"
                        :value="item.subscriptionName"
                        fallback="sparkles"
                        :size="18"
                        wrapper-class="inline-flex items-center justify-center"
                        image-class="h-7 w-7 object-contain"
                        icon-class="text-[#8A6A00]"
                      />
                    </span>
                    <div class="min-w-0">
                      <strong class="block truncate text-sm text-neutral-900">{{ item.subscriptionName }}</strong>
                      <p class="mt-1 text-xs text-neutral-500">{{ item.nextPaymentDate }} / {{ formatCurrency(item.paymentAmount) }}</p>
                    </div>
                  </div>
                  <span class="shrink-0 text-xs font-extrabold text-[#8A6A00]">{{ item.badge }}</span>
                </button>
              </div>

              <div v-else class="mt-5 rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-white px-5 py-6 text-center">
                <p class="text-sm font-bold text-neutral-900">{{ isAuthed ? '등록된 구독이 아직 없습니다' : '로그인 전 예시 프리뷰를 확인할 수 있습니다' }}</p>
                <p class="mt-2 text-sm leading-6 text-neutral-500">{{ isAuthed ? '첫 구독이 등록되면 가장 가까운 결제 일정이 여기 표시됩니다.' : '회원가입 후에는 이 영역이 실제 결제 데이터 프리뷰로 바뀝니다.' }}</p>
              </div>

              <div class="mt-5 grid gap-3">
                <button class="primary-button !min-h-[48px] w-full" @click="go('/subscriptions', true)">구독 목록 보기</button>
                <button class="secondary-button !min-h-[48px] w-full" @click="go('/dashboard', true)">지출 분석 보기</button>
              </div>
            </aside>
          </div>
        </section>

        <section class="guide-card">
          <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p class="eyebrow-label">처음 시작하는 흐름</p>
              <h2 class="mt-2 section-heading">가입 후 바로 써볼 수 있는 실제 사용 흐름입니다</h2>
              <p class="mt-2 body-copy">가입 → 첫 구독 등록 → 결제 캘린더 확인 → 대시보드 정리까지 바로 이어지는 구조로 구성했습니다.</p>
            </div>
            <div class="flex flex-wrap gap-2">
              <span class="guide-pill">1. 회원가입</span>
              <span class="guide-pill">2. 첫 구독 등록</span>
              <span class="guide-pill">3. 캘린더 확인</span>
              <span class="guide-pill">4. 대시보드 정리</span>
            </div>
          </div>
        </section>

        <section>
          <div class="mb-4 px-1">
            <p class="eyebrow-label">구독 등록</p>
            <h2 class="mt-2 text-[28px] font-bold tracking-[-0.04em] text-neutral-900">구독 관리에 필요한 기능을 한 눈에 확인해보세요</h2>
            <p class="mt-2 body-copy">구독 등록부터 결제 확인까지 필요한 기능을 빠르게 사용할 수 있습니다.</p>
          </div>
          <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            <article v-for="card in featureCards" :key="card.title" class="shell-card p-5">
              <div class="flex items-center gap-4">
                <div class="grid h-16 w-16 place-items-center rounded-[20px] bg-[linear-gradient(180deg,#f2d221,#e0bc15)] shadow-[inset_0_1px_0_rgba(255,255,255,0.4)] ring-1 ring-[rgba(138,106,0,0.18)]">
                  <AppAsset
                    type="core"
                    :value="card.title"
                    :fallback="card.fallback"
                    :size="22"
                    wrapper-class="inline-flex items-center justify-center"
                    image-class="h-9 w-9 object-contain"
                    icon-class="text-white"
                  />
                </div>
                <strong class="text-base text-neutral-900">{{ card.title }}</strong>
              </div>
              <p class="mt-4 body-copy">{{ card.desc }}</p>
            </article>
          </div>
        </section>

        <section class="shell-card overflow-hidden p-6 lg:p-8">
          <div class="rounded-[30px] border border-[rgba(242,210,33,0.18)] bg-[linear-gradient(135deg,rgba(255,250,232,0.98),rgba(247,241,227,0.92))] p-6 lg:p-7">
            <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
              <div>
                <p class="eyebrow-label">카테고리</p>
                <h2 class="mt-2 text-[28px] font-bold tracking-[-0.04em] text-neutral-900">구독 관리에 필요한 카테고리를 한눈에 확인해보세요</h2>
                <p class="mt-2 max-w-[780px] body-copy">현재 실제로 제공 중인 카테고리와 서비스 항목 기준으로 맞춰두었고, 각 서비스 로고가 모두 보이도록 구성했습니다.</p>
              </div>
              <span class="chip-button is-selected !min-h-[34px]">대표 카테고리</span>
            </div>

            <div class="mt-7 grid gap-5 xl:grid-cols-12">
              <article
                v-for="category in categories"
                :key="category.key"
                class="relative overflow-hidden rounded-[30px] border border-[rgba(46,34,10,0.08)] bg-white shadow-soft"
                :class="category.layout"
              >
                <div class="absolute inset-0 opacity-90" :style="{ background: `linear-gradient(160deg, ${category.accent}, rgba(255,255,255,0.92) 62%)` }"></div>
                <div class="absolute -right-10 -top-10 h-40 w-40 rounded-full bg-white/35 blur-2xl"></div>
                <div class="relative z-10 flex h-full flex-col p-6 lg:p-7">
                  <div class="flex items-start justify-between gap-4">
                    <div class="max-w-[65%]">
                      <p class="text-[11px] font-extrabold uppercase tracking-[0.12em] text-neutral-300">Category</p>
                      <strong class="mt-2 block text-[26px] font-black tracking-[-0.04em] text-neutral-900">{{ category.label }}</strong>
                      <p class="mt-3 text-[16px] font-semibold leading-7 text-neutral-700">{{ category.headline }}</p>
                      <p class="mt-2 text-[14px] leading-6 text-neutral-500">{{ category.description }}</p>
                    </div>
                    <div class="grid h-24 w-24 shrink-0 place-items-center rounded-[26px] bg-white/88 ring-1 ring-[rgba(46,34,10,0.08)] shadow-soft lg:h-28 lg:w-28">
                      <AppAsset
                        type="category"
                        :value="category.label"
                        fallback="grid"
                        :size="28"
                        wrapper-class="inline-flex items-center justify-center"
                        image-class="h-16 w-16 object-contain lg:h-20 lg:w-20"
                        icon-class="text-[#8A6A00]"
                      />
                    </div>
                  </div>

                  <div class="mt-6 rounded-[26px] border border-[rgba(46,34,10,0.06)] bg-white/88 p-4 lg:p-5">
                    <p class="text-[13px] font-bold text-neutral-500">빠르게 등록할 수 있는 대표 서비스</p>
                    <div class="mt-4 flex flex-wrap gap-2.5">
                      <span
                        v-for="service in category.services"
                        :key="service"
                        class="inline-flex min-h-[40px] items-center gap-2.5 rounded-full border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.92)] px-3.5 py-2 text-[13px] font-semibold text-neutral-700"
                      >
                        <span class="grid h-6 w-6 shrink-0 place-items-center rounded-full bg-white ring-1 ring-[rgba(46,34,10,0.06)]">
                          <AppAsset
                            type="service"
                            :value="service"
                            fallback="sparkles"
                            :size="12"
                            wrapper-class="inline-flex items-center justify-center"
                            image-class="h-4 w-4 object-contain"
                            icon-class="text-[#8A6A00]"
                          />
                        </span>
                        <span>{{ service }}</span>
                      </span>
                    </div>
                  </div>
                </div>
              </article>
            </div>
          </div>
        </section>

        <section class="grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
          <div class="shell-card p-6 lg:p-8">
            <p class="eyebrow-label">사용 흐름</p>
            <h2 class="mt-2 text-[28px] font-bold tracking-[-0.04em] text-neutral-900">등록부터 확인까지 한번에 이어지는 구조</h2>
            <p class="mt-2 body-copy">가입 후 구독 등록, 결제 확인, 분석까지 자연스럽게 이어지도록 구성했습니다.</p>
            <div class="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <article v-for="step in steps" :key="step.n" class="ghost-card flex h-full flex-col p-5 transition hover:-translate-y-0.5 hover:shadow-soft">
                <div class="flex items-center gap-3">
                  <span class="grid h-10 w-10 place-items-center rounded-full bg-brand-500 text-sm font-bold text-[#231A07]">{{ step.n }}</span>
                  <span class="grid h-16 w-16 place-items-center overflow-hidden rounded-[22px] bg-white ring-1 ring-[rgba(46,34,10,0.08)] shadow-soft">
                    <img :src="step.image" :alt="step.imageAlt" class="h-11 w-11 object-contain" />
                  </span>
                </div>
                <strong class="mt-4 block text-base text-neutral-900">{{ step.title }}</strong>
                <p class="mt-2 text-sm leading-6 text-neutral-600">{{ step.desc }}</p>
                <p class="mt-auto pt-3 text-[12px] font-semibold text-[#8A6A00]">{{ step.detail }}</p>
              </article>
            </div>
          </div>

          <div class="shell-card p-6 lg:p-8">
            <div class="flex items-end justify-between gap-3">
              <div>
                <p class="eyebrow-label">빠른 이동</p>
                <h2 class="mt-2 text-[28px] font-bold tracking-[-0.04em] text-neutral-900">지금 바로 필요한 메뉴 이동</h2>
              </div>
              <span class="guide-pill">핵심 메뉴 4개</span>
            </div>
            <div class="mt-6 grid gap-4 sm:grid-cols-2">
              <button v-for="menu in quickMenus" :key="menu.label" type="button" class="ghost-card p-5 text-left transition hover:-translate-y-0.5 hover:shadow-soft focus:outline-none focus:ring-4 focus:ring-[rgba(242,210,33,0.16)]" @click="go(menu.to, true)">
                <div class="flex items-start justify-between gap-3">
                  <div class="grid h-16 w-16 place-items-center rounded-[22px] bg-white ring-1 ring-[rgba(46,34,10,0.08)] shadow-soft text-[#8A6A00]">
                    <AppIcon :name="menu.icon" :size="24" />
                  </div>
                  <span class="text-[11px] font-extrabold uppercase tracking-[0.08em] text-[#8A6A00]">{{ menu.meta }}</span>
                </div>
                <strong class="mt-4 block text-base text-neutral-900">{{ menu.label }}</strong>
                <p class="mt-2 text-sm leading-6 text-neutral-600">{{ menu.desc }}</p>
              </button>
            </div>
          </div>
        </section>

        <section class="section-card">
          <div class="border-b border-[rgba(46,34,10,0.08)] pb-5">
            <p class="eyebrow-label">FAQ</p>
            <h2 class="mt-2 text-[28px] font-bold tracking-[-0.04em] text-neutral-900">자주 찾는 질문을 확인해보세요</h2>
          </div>
          <div class="mt-5 grid gap-3">
            <article v-for="faq in faqs" :key="faq.q" class="ghost-card p-5">
              <strong class="text-base text-neutral-900">{{ faq.q }}</strong>
              <p class="mt-3 body-copy">{{ faq.a }}</p>
            </article>
          </div>
        </section>
      </div>
    </div>

    <PublicFooter />

    <button
      type="button"
      aria-label="페이지 맨 위로 이동"
      class="fixed bottom-7 right-7 z-50 grid h-14 w-14 place-items-center rounded-full border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.96)] text-[#8A6A00] shadow-soft backdrop-blur transition-all duration-300 hover:-translate-y-0.5 hover:bg-white active:scale-[0.96]"
      :class="showScrollTop ? 'translate-y-0 opacity-100 pointer-events-auto' : 'translate-y-4 opacity-0 pointer-events-none'"
      @click="scrollToTop"
    >
      <span class="sr-only">맨 위로 이동</span>
      <AppIcon name="arrowup" :size="22" />
    </button>
  </div>
</template>


<style scoped>
.home-shell {
  max-width: min(100%, 1660px);
}
</style>
