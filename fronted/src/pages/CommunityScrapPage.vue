<script setup>
import { computed, onMounted } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppShell from '@/components/layout/AppShell.vue'
import CommunityPostCard from '@/components/community/CommunityPostCard.vue'
import { useCommunityStore } from '@/stores/community'

const route = useRoute()
const router = useRouter()
const communityStore = useCommunityStore()
const { successMessage, errorMessage, isLoading } = storeToRefs(communityStore)

const scrapPagination = communityStore.scrapPagination


onBeforeRouteLeave(() => {
  communityStore.clearMessages()
})
onMounted(() => {
  const page = Number(route.query.page) || 1
  communityStore.fetchScraps(page)
})

const pageNumbers = computed(() => {
  const total = scrapPagination.totalPages
  const current = scrapPagination.page
  if (total <= 0) return []

  const start = Math.max(1, current - 2)
  const end = Math.min(total, current + 2)
  const pages = []
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const goToPage = (page) => {
  if (page < 1 || page > scrapPagination.totalPages || page === scrapPagination.page) return
  router.push({ query: { page } })
  communityStore.fetchScraps(page)
}
</script>

<template>
  <AppShell title="hidden">
    <div class="scrap-page">
      <section class="scrap-header rounded-card border border-neutral-200 bg-white px-5 py-4 shadow-soft lg:px-6 lg:py-5">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div class="flex flex-wrap items-center gap-2">
              <span class="rounded-full bg-brand-50 px-2.5 py-1 text-[11px] font-semibold text-brand-600 ring-1 ring-brand-100">
                스크랩 목록
              </span>
            </div>
            <h1 class="mt-2 text-[22px] font-bold tracking-[-0.03em] text-neutral-900 lg:text-[28px]">
              나중에 다시 볼 게시글을 모아둔 공간
            </h1>
            <p class="mt-2 text-sm leading-6 text-neutral-500">
              저장한 게시글만 모아서 다시 확인할 수 있습니다.
            </p>
          </div>

          <div class="flex flex-wrap gap-2">
            <RouterLink to="/community" class="secondary-button !min-h-[40px] !px-4">
              커뮤니티 목록
            </RouterLink>
            <RouterLink to="/community/new" class="primary-button !min-h-[40px] !px-4">
              게시글 작성
            </RouterLink>
          </div>
        </div>
      </section>

      <div
        v-if="successMessage || errorMessage"
        class="rounded-2xl border px-4 py-2 text-sm font-medium leading-5 shadow-sm"
        :class="successMessage
          ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
          : 'border-rose-200 bg-rose-50 text-rose-700'"
      >
        {{ successMessage || errorMessage }}
      </div>

      <section class="scrap-content rounded-card border border-neutral-200 bg-white px-4 py-4 shadow-soft lg:px-5 lg:py-5">
        <template v-if="communityStore.scrapPosts.length">
          <div class="grid gap-3.5">
            <div
              v-for="item in communityStore.scrapPosts"
              :key="item.postId"
              class="relative"
            >
              <CommunityPostCard :item="item" />
            </div>
          </div>

          <div
            v-if="scrapPagination.totalPages > 1"
            class="mt-4 flex items-center justify-center gap-1.5 pt-1"
          >
            <button
              type="button"
              class="flex h-9 w-9 items-center justify-center rounded-lg border border-neutral-200 text-sm text-neutral-500 transition hover:bg-neutral-50 disabled:cursor-not-allowed disabled:opacity-40"
              :disabled="scrapPagination.page <= 1 || isLoading"
              @click="goToPage(scrapPagination.page - 1)"
            >
              &lt;
            </button>

            <template v-if="pageNumbers[0] > 1">
              <button
                type="button"
                class="flex h-9 w-9 items-center justify-center rounded-lg border border-neutral-200 text-sm transition hover:bg-neutral-50"
                @click="goToPage(1)"
              >
                1
              </button>
              <span v-if="pageNumbers[0] > 2" class="px-1 text-neutral-400">...</span>
            </template>

            <button
              v-for="p in pageNumbers"
              :key="p"
              type="button"
              class="flex h-9 w-9 items-center justify-center rounded-lg border text-sm font-medium transition"
              :class="p === scrapPagination.page
                ? 'border-brand-500 bg-brand-500 text-white'
                : 'border-neutral-200 text-neutral-700 hover:bg-neutral-50'"
              :disabled="isLoading"
              @click="goToPage(p)"
            >
              {{ p }}
            </button>

            <template v-if="pageNumbers[pageNumbers.length - 1] < scrapPagination.totalPages">
              <span
                v-if="pageNumbers[pageNumbers.length - 1] < scrapPagination.totalPages - 1"
                class="px-1 text-neutral-400"
              >
                ...
              </span>
              <button
                type="button"
                class="flex h-9 w-9 items-center justify-center rounded-lg border border-neutral-200 text-sm transition hover:bg-neutral-50"
                @click="goToPage(scrapPagination.totalPages)"
              >
                {{ scrapPagination.totalPages }}
              </button>
            </template>

            <button
              type="button"
              class="flex h-9 w-9 items-center justify-center rounded-lg border border-neutral-200 text-sm text-neutral-500 transition hover:bg-neutral-50 disabled:cursor-not-allowed disabled:opacity-40"
              :disabled="scrapPagination.page >= scrapPagination.totalPages || isLoading"
              @click="goToPage(scrapPagination.page + 1)"
            >
              &gt;
            </button>
          </div>
        </template>

        <div
          v-else-if="!isLoading"
          class="flex flex-1 flex-col items-center justify-center text-center"
        >
          <div class="flex h-14 w-14 items-center justify-center rounded-full bg-brand-50 ring-1 ring-brand-100">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.8"
              class="h-6 w-6 text-brand-600"
            >
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 10h8M8 14h5" />
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M6.5 4.75h11A1.75 1.75 0 0 1 19.25 6.5v7A1.75 1.75 0 0 1 17.5 15.25h-5.08l-3.7 3.08c-.57.47-1.47.07-1.47-.67v-2.41H6.5A1.75 1.75 0 0 1 4.75 13.5v-7A1.75 1.75 0 0 1 6.5 4.75Z"
              />
            </svg>
          </div>

          <p class="mt-5 text-lg font-semibold text-neutral-900">
            스크랩한 게시글이 없습니다
          </p>
          <p class="mt-2 text-sm leading-6 text-neutral-500">
            커뮤니티 상세 화면에서 스크랩하기 버튼을 누르면 이 목록에 저장됩니다.
          </p>
          <RouterLink to="/community" class="mt-5 inline-flex text-sm font-semibold text-brand-600">
            게시글 보러 가기
          </RouterLink>
        </div>

        <div
          v-else
          class="flex flex-1 items-center justify-center text-sm text-neutral-500"
        >
          스크랩 목록을 불러오는 중입니다.
        </div>
      </section>
    </div>
  </AppShell>
</template>

<style scoped>
.scrap-page {
  min-height: calc(100vh - 220px);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.scrap-header {
  min-height: 0 !important;
  height: auto !important;
}

.scrap-content {
  flex: 1;
  min-height: calc(100vh - 360px);
  display: flex;
  flex-direction: column;
}

@media (max-width: 1024px) {
  .scrap-page {
    min-height: auto;
  }

  .scrap-content {
    min-height: 320px;
  }
}
</style>
