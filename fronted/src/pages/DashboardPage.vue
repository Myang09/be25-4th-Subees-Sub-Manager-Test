<script setup>
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import AppShell from '@/components/layout/AppShell.vue'
import { useDashboardStore } from '@/stores/dashboard'
import { useAuthStore } from '@/stores/auth'
import AppAsset from '@/components/ui/AppAsset.vue'
import AppStatePanel from '@/components/ui/AppStatePanel.vue'

const router = useRouter()
const dashboardStore = useDashboardStore()
const authStore = useAuthStore()

const {
  isLoading,
  errorMessage,
  userSummary,
  nextPayment,
  upcomingSubscriptions,
  categorySpendSummary,
  monthlySpendTrend,
  highestCategory,
  averageMonthlySpend,
} = storeToRefs(dashboardStore)

const monthLabel = computed(() => userSummary.value.billingWindowLabel || '이번 달')
const dueSoon = computed(() => upcomingSubscriptions.value.slice(0, 4))
const hasSubscriptions = computed(() => userSummary.value.activeSubscriptionCount > 0)
const topCategory = computed(() => highestCategory.value)

const overviewCards = computed(() => ([
  {
    label: '이번 달 결제',
    value: formatCurrency(userSummary.value.monthlyExpectedAmount),
    caption: '예상 총액',
  },
  {
    label: '다음 결제',
    value: userSummary.value.nextPaymentDate || '-',
    caption: nextPayment.value.subscriptionName || '예정된 결제 없음',
  },
  {
    label: '활성 구독',
    value: `${userSummary.value.activeSubscriptionCount}개`,
    caption: '정기 결제 기준',
  },
  {
    label: '절감 가능',
    value: formatCurrency(userSummary.value.potentialSavingsAmount),
    caption: userSummary.value.potentialSavingsAmount > 0 ? '중복 구독 추정' : '중복 구독 없음',
  },
]))

const monthlyTrend = computed(() => {
  const max = Math.max(...monthlySpendTrend.value.map((item) => Number(item.totalAmount || 0)), 1)

  return monthlySpendTrend.value.map((item) => ({
    ...item,
    height: Math.max(18, Math.round((Number(item.totalAmount || 0) / max) * 88)),
  }))
})

const summaryDescription = computed(() => {
  if (!nextPayment.value.subscriptionName || !hasSubscriptions.value) {
    return '이번 달 결제 총액과 가까운 결제 일정을 먼저 확인하세요.'
  }

  return `${nextPayment.value.subscriptionName} 결제가 가장 가깝고, ${userSummary.value.activeSubscriptionCount}개의 구독이 활성 상태입니다.`
})

const go = (path) => router.push(path)
const formatCurrency = (value) => `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`

const handleRetry = async () => {
  try {
    await dashboardStore.fetchDashboard({ force: true })
  } catch (error) {
    // 스토어 상태로 에러 패널 유지
  }
}
const formatDday = (value) => {
  if (value == null) return '-'
  if (value === 0) return 'D-DAY'
  if (value < 0) return `D+${Math.abs(value)}`
  return `D-${value}`
}

onMounted(async () => {
  try {
    await dashboardStore.fetchDashboard({ force: true })
  } catch (error) {
    // 스토어 상태로 에러 패널 노출
  }
})
</script>

<template>
  <AppShell>
    <AppStatePanel
      v-if="isLoading"
      title="대시보드를 불러오는 중입니다"
      description="이번 달 결제 총액과 가까운 결제 일정을 계산하고 있습니다."
      icon="sparkles"
      tone="info"
    />

    <AppStatePanel
      v-else-if="errorMessage"
      title="대시보드 데이터를 불러오지 못했습니다"
      :description="errorMessage"
      icon="chart"
      tone="error"
    >
      <template #actions>
        <button class="primary-button" @click="handleRetry">다시 시도</button>
      </template>
    </AppStatePanel>

    <AppStatePanel
      v-else-if="!hasSubscriptions"
      title="아직 등록된 구독이 없습니다"
      description="첫 구독을 등록하면 대시보드와 결제 캘린더가 바로 채워집니다."
      icon="plus"
    >
      <template #actions>
        <button class="primary-button" @click="go('/subscriptions/new')">첫 구독 등록</button>
        <button class="secondary-button" @click="go('/calendar')">결제 캘린더</button>
      </template>
    </AppStatePanel>

    <template v-else>
      <section class="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
        <div class="section-card !p-5 lg:!p-6">
          <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p class="eyebrow-label">{{ monthLabel }}</p>
              <h1 class="mt-2 text-[26px] font-black tracking-[-0.04em] text-neutral-900 lg:text-[32px]">{{ authStore.nickname || userSummary.nickname || '사용자' }}님 대시보드</h1>
              <p class="mt-2 text-sm leading-6 text-neutral-500">{{ summaryDescription }}</p>
            </div>
            <div class="flex flex-wrap gap-2.5">
              <button class="primary-button !min-h-[44px] !px-4" @click="go('/subscriptions/new')">구독 추가</button>
              <button class="secondary-button !min-h-[44px] !px-4" @click="go('/calendar')">결제 캘린더</button>
            </div>
          </div>

          <div class="mt-4 grid gap-3 md:grid-cols-2 xl:grid-cols-4">
            <article v-for="card in overviewCards" :key="card.label" class="dense-card !rounded-[22px] !p-4">
              <p class="text-[12px] font-bold text-neutral-500">{{ card.label }}</p>
              <strong class="mt-2 block text-[20px] font-black tracking-[-0.03em] text-neutral-900">{{ card.value }}</strong>
              <p class="mt-1.5 text-[12px] text-neutral-400">{{ card.caption }}</p>
            </article>
          </div>

          <div class="mt-4 grid gap-4 xl:grid-cols-[1fr_0.92fr]">
            <section class="ghost-card p-4">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <h2 class="text-[18px] font-bold tracking-[-0.02em] text-neutral-900">곧 결제되는 구독</h2>
                  <p class="mt-1 text-[13px] text-neutral-500">가까운 날짜 순</p>
                </div>
                <RouterLink to="/subscriptions" class="text-sm font-semibold text-brand-600">전체 보기</RouterLink>
              </div>

              <div v-if="dueSoon.length" class="mt-4 grid gap-3">
                <article v-for="item in dueSoon" :key="item.subscriptionId" class="flex items-center justify-between gap-3 rounded-[18px] border border-[rgba(46,34,10,0.08)] bg-white px-4 py-3">
                  <div class="flex min-w-0 items-center gap-3">
                    <span class="grid h-10 w-10 place-items-center rounded-2xl bg-brand-50 ring-1 ring-[rgba(46,34,10,0.08)]">
                      <AppAsset
                        type="service"
                        :value="item.subscriptionName"
                        secondary-type="category"
                        :secondary-value="item.categoryName"
                        fallback="sparkles"
                        :size="16"
                        wrapper-class="inline-flex items-center justify-center"
                        image-class="h-6 w-6 object-contain"
                        icon-class="text-[#8A6A00]"
                      />
                    </span>
                    <div class="min-w-0">
                      <p class="truncate text-sm font-bold text-neutral-900">{{ item.subscriptionName }}</p>
                      <p class="mt-1 text-[12px] text-neutral-500">{{ item.nextPaymentDateShort }} · {{ formatCurrency(item.paymentAmount) }}</p>
                    </div>
                  </div>
                  <span class="text-xs font-extrabold text-[#8A6A00]">{{ formatDday(item.dDay) }}</span>
                </article>
              </div>

              <AppStatePanel
                v-else
                class="mt-4"
                compact
                title="가까운 결제 일정이 없습니다"
                description="구독이 등록되면 다음 결제일 기준으로 자동 정렬됩니다."
                icon="calendar"
              />
            </section>

            <section class="rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-[linear-gradient(180deg,rgba(255,248,221,0.9),rgba(255,253,247,0.95))] p-4">
              <p class="text-sm font-semibold text-[#8A6A00]">이번 달 핵심</p>
              <p class="mt-3 text-[28px] font-black tracking-[-0.04em] text-neutral-900">{{ formatCurrency(userSummary.monthlyExpectedAmount) }}</p>
              <p class="mt-2 text-sm leading-6 text-neutral-500">{{ summaryDescription }}</p>
              <div class="mt-4 grid gap-2.5">
                <div class="rounded-[18px] border border-[rgba(46,34,10,0.08)] bg-white/88 px-4 py-3">
                  <p class="text-xs font-bold uppercase tracking-[0.08em] text-neutral-400">다음 결제</p>
                  <p class="mt-1.5 text-sm font-bold text-neutral-900">{{ nextPayment.nextPaymentDate }} · {{ nextPayment.paymentCardName }}</p>
                </div>
                <div class="rounded-[18px] border border-[rgba(46,34,10,0.08)] bg-white/88 px-4 py-3">
                  <p class="text-xs font-bold uppercase tracking-[0.08em] text-neutral-400">카테고리</p>
                  <p class="mt-1.5 text-sm font-bold text-neutral-900">{{ topCategory?.categoryName || '없음' }} · {{ topCategory ? `${topCategory.ratio}%` : '-' }}</p>
                </div>
              </div>
            </section>
          </div>
        </div>

        <div class="section-card !p-5 lg:!p-6">
          <div>
            <p class="eyebrow-label">카테고리별 소비</p>
            <h2 class="mt-2 text-[22px] font-bold tracking-[-0.03em] text-neutral-900">가장 많이 쓰는 분야</h2>
          </div>

          <div v-if="categorySpendSummary.length" class="mt-4 grid gap-3">
            <article v-for="row in categorySpendSummary" :key="row.categoryName" class="ghost-card p-4">
              <div class="flex items-center justify-between gap-3">
                <div class="flex items-center gap-3">
                  <AppAsset
                    type="category"
                    :value="row.categoryName"
                    fallback="grid"
                    :size="16"
                    wrapper-class="inline-flex items-center justify-center"
                    image-class="h-8 w-8 rounded-xl object-contain bg-white p-1 ring-1 ring-[rgba(46,34,10,0.08)]"
                    icon-class="text-[#8A6A00]"
                  />
                  <div>
                    <p class="text-sm font-bold text-neutral-900">{{ row.categoryName }}</p>
                    <p class="mt-1 text-xs text-neutral-500">{{ row.subscriptionCount }}개 구독</p>
                  </div>
                </div>
                <div class="text-right">
                  <strong class="text-sm text-neutral-900">{{ formatCurrency(row.totalAmount) }}</strong>
                  <p class="mt-1 text-xs text-neutral-500">{{ row.ratio }}%</p>
                </div>
              </div>
              <div class="mt-3 h-[8px] overflow-hidden rounded-full bg-[rgba(46,34,10,0.08)]">
                <span class="block h-full rounded-full bg-[linear-gradient(90deg,#f5d85d,#b68242)]" :style="{ width: `${row.ratio}%` }"></span>
              </div>
            </article>
          </div>

          <AppStatePanel
            v-else
            class="mt-4"
            compact
            title="카테고리 분석 데이터가 없습니다"
            description="이번 달 결제 내역이 생기면 카테고리별 비중이 표시됩니다."
            icon="grid"
          />
        </div>
      </section>

      <section class="section-card !p-5 lg:!p-6">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="eyebrow-label">월별 흐름</p>
            <h2 class="mt-2 text-[22px] font-bold tracking-[-0.03em] text-neutral-900">최근 6개월 지출</h2>
          </div>
          <p class="text-sm text-neutral-500">평균 {{ formatCurrency(averageMonthlySpend) }}</p>
        </div>

        <div class="mt-5 grid grid-cols-6 items-end gap-3">
          <article v-for="item in monthlyTrend" :key="item.key" class="flex flex-col items-center gap-2 text-center">
            <div class="flex h-36 w-full items-end rounded-[18px] bg-[rgba(240,233,216,0.96)] p-1.5 ring-1 ring-[rgba(46,34,10,0.05)]">
              <div
                class="w-full rounded-[14px]"
                :class="item.isCurrent ? 'bg-[linear-gradient(180deg,#7A5836,#503A22)]' : 'bg-[linear-gradient(180deg,#f2d84f,#d7b448)]'"
                :style="{ height: `${item.height}px` }"
              ></div>
            </div>
            <div>
              <p class="text-[12px] font-bold text-neutral-900">{{ item.monthLabel }}</p>
              <p class="mt-1 text-[11px] text-neutral-500">{{ formatCurrency(item.totalAmount) }}</p>
            </div>
          </article>
        </div>
      </section>
    </template>
  </AppShell>
</template>
