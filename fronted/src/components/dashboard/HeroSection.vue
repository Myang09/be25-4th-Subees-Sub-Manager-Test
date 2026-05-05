<script setup>
import { computed } from 'vue'
import AppAsset from '@/components/ui/AppAsset.vue'

const props = defineProps({
  summary: { type: Object, required: true },
  nextPayment: { type: Object, required: true },
  isLoggedIn: { type: Boolean, default: false },
  nickname: { type: String, default: '' },
})

const formatCurrency = (value) => `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`
const displayName = computed(() => (props.isLoggedIn ? (props.nickname || props.summary.nickname) : '사용자'))
const metricCards = computed(() => [
  { label: '이번 달 결제금액', value: formatCurrency(props.summary.monthlyExpectedAmount), note: '활성 구독 기준' },
  { label: '다음 결제', value: props.summary.nextPaymentDate, note: props.nextPayment.subscriptionName || '예정 없음' },
  { label: '절감 가능', value: formatCurrency(props.summary.potentialSavingsAmount), note: '중복/미사용 추정' },
  { label: '활성 구독', value: `${props.summary.activeSubscriptionCount}개`, note: '정기 결제 기준' },
])
</script>

<template>
  <section class="grid gap-6 xl:grid-cols-[1.32fr_0.98fr]">
    <div class="shell-card overflow-hidden p-0">
      <div class="border-b border-[rgba(46,34,10,0.08)] bg-[linear-gradient(180deg,rgba(240,228,186,0.42),rgba(255,253,249,0.92))] px-8 py-8">
        <p class="eyebrow-label">대시보드 (로그인 후 메인 화면)</p>
        <h2 class="mt-2 text-[34px] font-bold tracking-[-0.05em] text-neutral-900">{{ displayName }}님, 구독·결제·캘린더를 한 곳에 정리했어요!</h2>
        <p class="mt-3 max-w-[720px] text-sm leading-7 text-neutral-500">현재 월의 예상 구독 결제 총액, 가장 가까운 결제, 카테고리 소비 비중, 월별 지출 흐름을 바로 읽을 수 있도록 구성한 메인 화면입니다.</p>

        <div class="mt-6 grid gap-3 md:grid-cols-2 xl:grid-cols-4">
          <article v-for="card in metricCards" :key="card.label" class="rounded-[22px] border border-[rgba(46,34,10,0.08)] bg-brand-50 p-5">
            <p class="text-xs font-bold uppercase tracking-[0.08em] text-neutral-300">{{ card.label }}</p>
            <p class="mt-3 text-2xl font-bold tracking-[-0.03em] text-neutral-900">{{ card.value }}</p>
            <p class="mt-2 text-sm text-neutral-500">{{ card.note }}</p>
          </article>
        </div>
      </div>

      <div class="grid gap-4 px-6 py-6 lg:grid-cols-[minmax(0,1fr)_320px]">
        <div class="rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-brand-50 p-5">
          <p class="eyebrow-label">이번 달 요약</p>
          <p class="mt-3 text-[40px] font-bold tracking-[-0.05em] text-neutral-900">{{ formatCurrency(props.summary.monthlyExpectedAmount) }}</p>
          <p class="mt-3 text-sm leading-7 text-neutral-500">사용자님의 이번 달 정기 결제 금액은 위와 같고, 가장 가까운 결제일은 {{ props.summary.nextPaymentDate }} 입니다.</p>
          <div class="mt-5 flex flex-wrap gap-3">
            <RouterLink :to="props.isLoggedIn ? '/subscriptions/new' : '/signup'" class="primary-button">구독 추가</RouterLink>
            <RouterLink :to="props.isLoggedIn ? '/subscriptions' : '/login'" class="secondary-button">구독 목록 보기</RouterLink>
          </div>
        </div>

        <div class="rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,249,0.94)] p-5 shadow-soft">
          <div class="flex items-start justify-between gap-3">
            <div class="flex items-start gap-3">
              <div class="grid h-14 w-14 place-items-center rounded-[18px] bg-white ring-1 ring-[rgba(46,34,10,0.08)]">
                <AppAsset
                  type="service"
                  :value="props.nextPayment.subscriptionName"
                  secondary-type="category"
                  :secondary-value="props.nextPayment.categoryName"
                  fallback="sparkles"
                  :size="18"
                  wrapper-class="inline-flex items-center justify-center"
                  image-class="h-9 w-9 object-contain"
                  icon-class="text-[#8A6A00]"
                />
              </div>
              <div>
                <p class="eyebrow-label">다음 결제</p>
                <p class="mt-2 text-lg font-bold text-neutral-900">{{ props.nextPayment.subscriptionName }}</p>
              </div>
            </div>
            <span class="rounded-full bg-[rgba(242,210,33,0.16)] px-3 py-1 text-xs font-bold text-warning">D-{{ props.nextPayment.dDay }}</span>
          </div>
          <div class="mt-5 grid gap-3">
            <div class="rounded-[20px] bg-neutral-900 px-4 py-4 text-white">
              <p class="text-sm font-bold">{{ props.nextPayment.categoryName }}</p>
              <p class="mt-2 text-xl font-bold">{{ formatCurrency(props.nextPayment.paymentAmount) }}</p>
              <div class="mt-2 flex items-center gap-2 text-xs text-white/70">
                <AppAsset
                  type="card"
                  :value="props.nextPayment.paymentCardName"
                  fallback="creditcard"
                  :size="14"
                  wrapper-class="inline-flex items-center justify-center"
                  image-class="h-6 w-6 rounded-md bg-white/90 p-1 object-contain"
                  icon-class="text-white"
                  badge-class="inline-flex min-w-[44px] items-center justify-center rounded-md px-2 py-1 text-[10px] font-black uppercase tracking-[-0.02em]"
                />
                <span>{{ props.nextPayment.nextPaymentDate }} · {{ props.nextPayment.paymentCardName }}</span>
              </div>
            </div>
            <div class="grid gap-3 sm:grid-cols-2">
              <div class="ghost-card p-4">
                <p class="eyebrow-label">결제 주기</p>
                <p class="mt-2 text-sm font-bold text-neutral-900">{{ props.nextPayment.billingCycleLabel }}</p>
              </div>
              <div class="ghost-card p-4">
                <p class="eyebrow-label">상태</p>
                <p class="mt-2 text-sm font-bold text-neutral-900">자동 결제 예정</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <section class="shell-card p-6">
      <p class="eyebrow-label">메인 기준 화면</p>
      <h3 class="mt-2 text-xl font-bold tracking-[-0.03em] text-neutral-900">기존 프론트 이미지와 스켈레톤 구조를 함께 맞춘 기준 대시보드</h3>
      <ul class="mt-5 grid gap-3 text-sm leading-7 text-neutral-500">
        <li class="ghost-card px-4 py-3">큰 금액과 다음 결제일을 먼저 보여주는 구조</li>
        <li class="ghost-card px-4 py-3">곧 결제되는 구독과 카테고리 분석을 같은 화면에서 연결</li>
        <li class="ghost-card px-4 py-3">OTT/음악/AI/클라우드 이미지 에셋과 카드사 이미지를 재사용</li>
        <li class="ghost-card px-4 py-3">기존 프론트의 크림·옐로우·브라운 톤을 전체 화면에 유지</li>
      </ul>
    </section>
  </section>
</template>
