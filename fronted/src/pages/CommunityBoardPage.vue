<script setup>
import { computed, onMounted } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppShell from '@/components/layout/AppShell.vue'
import CommunityToolbar from '@/components/community/CommunityToolbar.vue'
import CommunityPostCard from '@/components/community/CommunityPostCard.vue'
import { useCommunityStore } from '@/stores/community'
import AppStatusBanner from '@/components/ui/AppStatusBanner.vue'

onBeforeRouteLeave(() => {
  communityStore.clearMessages()
})
const route = useRoute()
const router = useRouter()
const communityStore = useCommunityStore()
const { filteredPosts, successMessage, errorMessage, isLoading } = storeToRefs(communityStore)

const pagination = communityStore.pagination

onMounted(() => {
  const page = Number(route.query.page) || 1
  communityStore.fetchPosts(page)
})

const pageNumbers = computed(() => {
  const total = pagination.totalPages
  const current = pagination.page
  if (total <= 0) return []

  const start = Math.max(1, current - 1)
  const end = Math.min(total, current + 1)
  const pages = []
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const goToPage = (page) => {
  if (page < 1 || page > pagination.totalPages || page === pagination.page) return
  router.push({ query: { page } })
  communityStore.fetchPosts(page)
}
</script>

<template>
  <AppShell title="hidden">
    <div class="community-page flex flex-col gap-3">
      <section class="community-header rounded-card border border-neutral-200 bg-white px-5 py-4 shadow-soft lg:px-6 lg:py-5">
        <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <div class="flex flex-wrap items-center gap-2">
              <span class="rounded-full bg-brand-50 px-2.5 py-1 text-[11px] font-semibold text-brand-600 ring-1 ring-brand-100">
                커뮤니티
              </span>
              <span class="text-sm text-neutral-500">구독 관리 경험과 절감 팁을 공유하세요</span>
            </div>
            <h1 class="mt-2 text-[20px] font-black tracking-[-0.03em] text-neutral-900 lg:text-[26px]">
              구독 관리 커뮤니티
            </h1>
          </div>

          <div class="flex flex-wrap gap-2">
            <RouterLink to="/community/scraps" class="secondary-button !min-h-[40px] !px-4">
              스크랩
            </RouterLink>
            <RouterLink to="/community/new" class="primary-button !min-h-[40px] !px-4">
              게시글 작성
            </RouterLink>
          </div>
        </div>
      </section>

      <AppStatusBanner
        v-if="successMessage || errorMessage"
        :tone="successMessage ? 'success' : 'error'"
        :title="successMessage ? '커뮤니티 상태가 업데이트되었습니다' : '커뮤니티 데이터를 불러오지 못했습니다'"
        :message="successMessage || errorMessage"
      />

      <div class="flex justify-end">
        <CommunityToolbar
          :selected-sort="communityStore.filters.sortBy"
          @select-sort="communityStore.setSort"
        />
      </div>

      <section class="community-content flex flex-col rounded-card border border-neutral-200 bg-white px-4 py-4 shadow-soft lg:px-5 lg:py-5">
        <div v-if="filteredPosts.length" class="grid gap-3.5">
          <CommunityPostCard
            v-for="item in filteredPosts"
            :key="item.postId"
            :item="item"
          />
        </div>

        <div
          v-else-if="isLoading"
          class="flex flex-1 items-center justify-center text-sm text-neutral-500"
        >
          게시글을 불러오는 중입니다.
        </div>

        <div
          v-else
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

          <h2 class="mt-5 text-[22px] font-bold tracking-[-0.03em] text-neutral-900">
            조건에 맞는 게시글이 없습니다
          </h2>
          <p class="mt-2 text-sm leading-6 text-neutral-500">
            첫 게시글을 작성해서 커뮤니티를 시작해보세요.
          </p>

          <RouterLink to="/community/new" class="primary-button mt-6 !min-h-[44px] !px-5">
            첫 게시글 작성
          </RouterLink>
        </div>

        <div
          v-if="pagination.totalPages > 1 && filteredPosts.length"
          class="mt-4 flex items-center justify-center gap-1.5 pt-1"
        >
          <button
            type="button"
            class="flex h-9 w-9 items-center justify-center rounded-lg border border-neutral-200 text-sm text-neutral-500 transition hover:bg-neutral-50 disabled:cursor-not-allowed disabled:opacity-40"
            :disabled="pagination.page <= 1 || isLoading"
            @click="goToPage(pagination.page - 1)"
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
            :class="p === pagination.page
              ? 'border-brand-500 bg-brand-500 text-white'
              : 'border-neutral-200 text-neutral-700 hover:bg-neutral-50'"
            :disabled="isLoading"
            @click="goToPage(p)"
          >
            {{ p }}
          </button>

          <template v-if="pageNumbers[pageNumbers.length - 1] < pagination.totalPages">
            <span
              v-if="pageNumbers[pageNumbers.length - 1] < pagination.totalPages - 1"
              class="px-1 text-neutral-400"
            >
              ...
            </span>
            <button
              type="button"
              class="flex h-9 w-9 items-center justify-center rounded-lg border border-neutral-200 text-sm transition hover:bg-neutral-50"
              @click="goToPage(pagination.totalPages)"
            >
              {{ pagination.totalPages }}
            </button>
          </template>

          <button
            type="button"
            class="flex h-9 w-9 items-center justify-center rounded-lg border border-neutral-200 text-sm text-neutral-500 transition hover:bg-neutral-50 disabled:cursor-not-allowed disabled:opacity-40"
            :disabled="pagination.page >= pagination.totalPages || isLoading"
            @click="goToPage(pagination.page + 1)"
          >
            &gt;
          </button>
        </div>
      </section>
    </div>
  </AppShell>
</template>

<style scoped>
.community-page {
  min-height: calc(100vh - 220px);
}

.community-header {
  min-height: 0 !important;
  height: auto !important;
}

.community-content {
  flex: 1;
  min-height: calc(100vh - 360px);
}
</style>
