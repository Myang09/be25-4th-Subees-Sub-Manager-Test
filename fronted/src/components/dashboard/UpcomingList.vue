<script setup>
import AppAsset from '@/components/ui/AppAsset.vue'
defineProps({
  items: { type: Array, default: () => [] },
})
const formatCurrency = (value) => `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`
</script>

<template>
  <section class="shell-card p-6">
    <div class="flex flex-wrap items-start justify-between gap-4 border-b border-[rgba(46,34,10,0.08)] pb-5">
      <div>
        <p class="eyebrow-label">곧 결제되는 구독</p>
        <h2 class="mt-2 text-xl font-bold tracking-[-0.03em] text-neutral-900">가까운 날짜 순으로 결제되는 구독을 확인하세요</h2>
        <p class="mt-2 text-sm leading-6 text-neutral-500">설계서 기준 최대 4개를 먼저 보여주고, 기존 프론트의 서비스 로고와 카드 이미지를 같이 반영했습니다.</p>
      </div>
      <RouterLink to="/subscriptions" class="secondary-button !min-h-11 !px-4">구독 목록 보기</RouterLink>
    </div>

    <div class="mt-6 grid gap-4">
      <article v-for="item in items" :key="item.subscriptionId" class="rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-brand-50 p-5 transition hover:-translate-y-0.5 hover:shadow-soft">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div class="flex min-w-0 items-center gap-4">
            <div class="grid h-14 w-14 shrink-0 place-items-center rounded-[18px] bg-white shadow-soft ring-1 ring-[rgba(46,34,10,0.08)]">
              <AppAsset
                type="service"
                :value="item.subscriptionName"
                secondary-type="category"
                :secondary-value="item.categoryName"
                fallback="sparkles"
                :size="18"
                wrapper-class="inline-flex items-center justify-center"
                image-class="h-9 w-9 object-contain"
                icon-class="text-[#8A6A00]"
              />
            </div>
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-2">
                <h3 class="truncate text-base font-bold text-neutral-900">{{ item.subscriptionName }}</h3>
                <span class="rounded-full bg-white px-3 py-1 text-xs font-bold text-neutral-700">{{ item.categoryName }}</span>
              </div>
              <div class="mt-2 flex flex-wrap items-center gap-2 text-sm text-neutral-500">
                <span>{{ item.nextPaymentDate }} 결제 예정</span>
                <span>·</span>
                <div class="inline-flex items-center gap-2">
                  <AppAsset
                    type="card"
                    :value="item.paymentCardName"
                    fallback="creditcard"
                    :size="14"
                    wrapper-class="inline-flex items-center justify-center"
                    image-class="h-6 w-6 rounded-md bg-white p-1 ring-1 ring-[rgba(46,34,10,0.08)] object-contain"
                    icon-class="text-[#8A6A00]"
                    badge-class="inline-flex min-w-[44px] items-center justify-center rounded-md px-2 py-1 text-[10px] font-black uppercase tracking-[-0.02em]"
                  />
                  <span>{{ item.paymentCardName }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="grid gap-3 sm:grid-cols-2 lg:flex lg:items-center lg:gap-4">
            <div class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white px-4 py-3 text-left lg:min-w-[110px]">
              <p class="eyebrow-label">D-day</p>
              <p class="mt-2 text-base font-bold text-neutral-900">D-{{ item.dDay }}</p>
            </div>
            <div class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white px-4 py-3 text-left lg:min-w-[140px]">
              <p class="eyebrow-label">결제 금액</p>
              <p class="mt-2 text-base font-bold text-neutral-900">{{ formatCurrency(item.paymentAmount) }}</p>
            </div>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>
