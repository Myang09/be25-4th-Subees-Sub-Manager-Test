<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppShell from '@/components/layout/AppShell.vue'
import SubscriptionFilters from '@/components/subscriptions/SubscriptionFilters.vue'
import SubscriptionListCard from '@/components/subscriptions/SubscriptionListCard.vue'
import SubscriptionDetailPanel from '@/components/subscriptions/SubscriptionDetailPanel.vue'
import AppStatePanel from '@/components/ui/AppStatePanel.vue'
import { useSubscriptionsStore } from '@/stores/subscriptions'
import { useAuthStore } from '@/stores/auth'

const subscriptionsStore = useSubscriptionsStore()
const authStore = useAuthStore()

onMounted(() => {
  subscriptionsStore.loadSubscriptions()
})

const {
  filters,
  categories,
  filteredSubscriptions,
  selectedSubscription,
  totalCount,
  monthlyExpectedAmount,
  paymentCards,
  categoryOptions,
} = storeToRefs(subscriptionsStore)

const displayNickname = computed(() => {
  return authStore.nickname || authStore.user?.nickname || '회원'
})

const showDeleteConfirm = ref(false)
const pendingDeleteId = ref(null)

const formatCurrency = (value) =>
  `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`

const requestDelete = (subscriptionId) => {
  pendingDeleteId.value = subscriptionId
  showDeleteConfirm.value = true
}

const confirmDelete = async () => {
  if (!pendingDeleteId.value) return

  try {
    await subscriptionsStore.deleteSubscription(pendingDeleteId.value)
    pendingDeleteId.value = null
    showDeleteConfirm.value = false
  } catch (e) {
    console.error(e)
  }
}

const cancelDelete = () => {
  pendingDeleteId.value = null
  showDeleteConfirm.value = false
}

const PAGE_SIZE = 5
const currentPage = ref(1)

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredSubscriptions.value.length / PAGE_SIZE))
})

const paginatedSubscriptions = computed(() => {
  const startIndex = (currentPage.value - 1) * PAGE_SIZE
  const endIndex = startIndex + PAGE_SIZE
  return filteredSubscriptions.value.slice(startIndex, endIndex)
})

const goToPreviousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value -= 1
  }
}

const goToNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value += 1
  }
}

const showFilteredEmptyState = computed(() => {
  const hasQuery = filters.value.query.trim().length > 0
  const hasCategoryFilter = filters.value.categoryName !== '전체'

  return filteredSubscriptions.value.length === 0 && (hasQuery || hasCategoryFilter)
})

watch(
  () => filteredSubscriptions.value.length,
  () => {
    if (currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value
    }
  }
)

watch(
  () => [filters.value.query, filters.value.categoryName, filters.value.sortBy],
  () => {
    currentPage.value = 1
  }
)
</script>


<template>
  <AppShell title="hidden">
    <div class="grid gap-6">
      <section class="shell-card px-6 py-6 md:px-7 md:py-7 xl:px-8 xl:py-8">
        <div class="hero-layout">
          <div class="subscription-hero-copy">
            <p class="hero-eyebrow">
              {{ displayNickname }}님, 구독·결제·캘린더를 한 곳에 정리했어요!
            </p>

            <div class="hero-summary-badge">
              구독 현황 요약
            </div>

            <h1 class="hero-title">
              구독 목록을 한눈에 살펴보세요.
            </h1>

            <p class="hero-description">
              서비스 이름으로 검색하고 카테고리별로 모아보며, 선택한 항목의 상세 정보를 바로 확인할 수 있어요.

            </p>

          </div>

          <div class="hero-stats">
            <article class="ghost-card hero-stat-card">
              <p class="hero-stat-label">전체 구독</p>
              <p class="hero-stat-value">총 {{ totalCount }}개</p>
              <p class="hero-stat-caption">현재 등록된 구독 항목 기준</p>
            </article>

            <article class="ghost-card hero-stat-card hero-stat-card--wide">
              <p class="hero-stat-label">월 환산 금액</p>
              <p class="hero-stat-value">{{ formatCurrency(monthlyExpectedAmount) }}</p>
              <p class="hero-stat-caption">
                연간 결제는 월 단위로 환산하고, 활성 구독만 합산합니다.
              </p>
            </article>
          </div>
        </div>
      </section>

      <SubscriptionFilters
        :model-value="filters.query"
        :categories="categories"
        :selected-category="filters.categoryName"
        :selected-sort="filters.sortBy"
        @update:model-value="subscriptionsStore.setQuery"
        @select-category="subscriptionsStore.setCategory"
        @select-sort="subscriptionsStore.setSort"
      />

      <section class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_400px]">
        <div class="section-card">
          <div class="flex flex-col gap-4 border-b border-[rgba(46,34,10,0.08)] pb-5 md:flex-row md:items-end md:justify-between">
            <div>
              <p class="text-sm font-semibold text-[#8A6A00]">구독 서비스 목록</p>
              <h2 class="mt-2 section-heading">검색 결과 {{ filteredSubscriptions.length }}개</h2>
            </div>
            <p class="body-copy">항목을 누르면 오른쪽에서 상세 내용을 확인할 수 있어요.</p>
          </div>

          <div v-if="filteredSubscriptions.length" class="mt-5 grid gap-4">
            <SubscriptionListCard
              v-for="item in paginatedSubscriptions"
              :key="item.subscriptionId"
              :item="item"
              :is-selected="selectedSubscription?.subscriptionId === item.subscriptionId"
              @select="subscriptionsStore.selectSubscription"
            />
          </div>
          <div
            v-if="filteredSubscriptions.length > PAGE_SIZE"
            class="mt-6 flex items-center justify-center gap-3"
          >
            <button
              type="button"
              class="secondary-button !min-h-[44px] !px-4 disabled:opacity-40"
              :disabled="currentPage === 1"
              @click="goToPreviousPage"
            >
              이전
            </button>

            <div class="rounded-full border px-4 py-2 text-sm font-bold">
              {{ currentPage }} / {{ totalPages }}
            </div>

            <button
              type="button"
              class="secondary-button !min-h-[44px] !px-4 disabled:opacity-40"
              :disabled="currentPage === totalPages"
              @click="goToNextPage"
            >
              다음
            </button>
          </div>

        <AppStatePanel
          v-else-if="showFilteredEmptyState"
          class="mt-6"
          title="조건에 맞는 구독이 없습니다"
          description="검색어를 지우거나 카테고리를 전체로 바꾸면 다시 목록을 확인할 수 있습니다."
          icon="list"
        >
          <template #actions>
            <button
              type="button"
              class="secondary-button"
              @click="subscriptionsStore.setQuery(''); subscriptionsStore.setCategory('전체')"
            >
              필터 초기화
            </button>
          </template>
        </AppStatePanel>
        </div>

        <div class="xl:sticky xl:top-7 xl:self-start">
          <SubscriptionDetailPanel
            :item="selectedSubscription"
            :card-options="paymentCards.map(card => card.displayName)"
            :category-options="categoryOptions"
            @save="subscriptionsStore.updateSubscription"
            @delete="requestDelete"
            @toggle-status="subscriptionsStore.toggleSubscriptionStatus"
          />
        </div>
      </section>
    </div>

    <div v-if="showDeleteConfirm" class="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/50 px-4">
      <div class="w-full max-w-md rounded-modal bg-brand-50 p-6 shadow-floating">
        <p class="text-sm font-semibold text-danger">구독 삭제</p>
        <h3 class="mt-2 text-xl font-bold tracking-[-0.02em] text-neutral-900">
          정말로 해당 구독 서비스를 삭제하시겠습니까?
        </h3>
        <p class="mt-3 text-sm leading-6 text-neutral-500">
          삭제하면 목록에서 즉시 제거되며 복구할 수 없습니다.
        </p>
        <div class="mt-6 flex justify-end gap-3">
          <button type="button" class="secondary-button" @click="cancelDelete">취소</button>
          <button
            type="button"
            class="inline-flex min-h-[54px] items-center justify-center rounded-[22px] border border-transparent bg-danger px-5 text-sm font-extrabold text-white transition hover:brightness-95 active:scale-[0.985]"
            @click="confirmDelete"
          >
            삭제
          </button>
        </div>
      </div>
    </div>
  </AppShell>
</template>

<style scoped>
.hero-layout {
  display: grid;
  gap: 20px;
}
.hero-stat-card {
  height: 100%;
  padding: 16px 18px;
}

.hero-stat-card--wide {
  min-height: 88px;
}

.hero-stat-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.04em;
  color: #171717;
  word-break: keep-all;
}

@media (min-width: 1280px) {
  .hero-layout {
    grid-template-columns: minmax(0, 1fr) minmax(320px, 388px);
    align-items: start;
    gap: 24px;
  }

  .hero-stats {
    margin-top: 38px;
  }
}

.subscription-hero-copy {
  display: flex;
  min-height: 100%;
  flex-direction: column;
}

.hero-eyebrow {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #8a6a00;
  line-height: 1.5;
}

.hero-summary-badge {
  margin-top: 16px;
  display: inline-flex;
  min-height: 42px;
  align-self: flex-start;
  white-space: nowrap;
  justify-content: center;
  align-items: center;
  border-radius: 9999px;
  border: 1px solid rgba(242, 210, 33, 0.2);
  background: rgba(242, 210, 33, 0.12);
  padding: 0 16px;
  font-size: 13px;
  font-weight: 800;
  color: #8a6a00;
}

.hero-title {
  margin-top: 24px;
  max-width: 18ch;
  font-size: 44px;
  font-weight: 800;
  line-height: 1.18;
  letter-spacing: -0.04em;
  color: #171717;
  word-break: keep-all;
}

.hero-description {
  margin-top: 24px;
  max-width: 72ch;
  font-size: 18px;
  line-height: 1.8;
  color: #5f5b57;
  word-break: keep-all;
}

.hero-description--sub {
  margin-top: 12px;
}

.hero-stats {
  display: grid;
  gap: 12px;
  align-content: start;
}

.hero-stat-card {
  height: 100%;
  padding: 20px;
}

.hero-stat-card--wide {
  min-height: 100px;
}

.hero-stat-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #737373;
}

.hero-stat-value {
  margin-top: 12px;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.04em;
  color: #171717;
  word-break: keep-all;
}

.hero-stat-caption {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: #737373;
  word-break: keep-all;
}

@media (min-width: 1280px) {
  .hero-layout {
    grid-template-columns: minmax(0, 1fr) minmax(320px, 388px);
    align-items: start;
    gap: 24px;
  }
}

@media (min-width: 640px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1279px) {
  .hero-title {
    max-width: none;
    font-size: 38px;
  }
}

@media (max-width: 767px) {
  .hero-title {
    font-size: 32px;
  }

  .hero-description {
    font-size: 16px;
  }

  .hero-stat-card {
    padding: 18px;
  }
}
</style>