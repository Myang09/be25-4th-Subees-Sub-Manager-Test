<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppShell from '@/components/layout/AppShell.vue'
import { useCommunityStore } from '@/stores/community'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const communityStore = useCommunityStore()
const authStore = useAuthStore()
const { successMessage, errorMessage, currentPost: post, isLoading } = storeToRefs(communityStore)
const showDeleteConfirm = ref(false)

const postId = computed(() => Number(route.params.postId))

const loadPost = async () => {
  if (Number.isNaN(postId.value)) return
  await communityStore.fetchPostDetail(postId.value)
}

onBeforeRouteLeave(() => {
  communityStore.clearMessages()
})

onMounted(loadPost)
watch(postId, loadPost)

const handleDelete = async () => {
  const success = await communityStore.deletePost(postId.value)
  if (success) {
    router.push('/community')
  }
}

const handleToggleScrap = async () => {
  await communityStore.toggleScrap(postId.value)
}
</script>

<template>
  <AppShell title="hidden">
    <div v-if="post" class="community-detail-page">
      <section class="community-detail-header rounded-card border border-neutral-200 bg-white px-5 py-5 shadow-soft lg:px-6">
        <div class="flex flex-col gap-3">
          <div class="flex flex-wrap items-center gap-2">
            <span
              v-if="post.isMine"
              class="inline-flex items-center rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-600 ring-1 ring-emerald-100"
            >
              내 게시글
            </span>
          </div>

          <h1 class="text-[24px] font-bold tracking-[-0.03em] text-neutral-900 lg:text-[30px]">
            {{ post.title }}
          </h1>

          <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div class="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-neutral-500">
              <span>{{ post.createdAtLabel }}</span>
              <span>조회수 {{ post.viewCount }}</span>
              <span>스크랩 {{ post.scrapCount }}</span>
              <span>{{ post.authorNickname }}</span>
            </div>

            <div v-if="authStore.isLoggedIn && !post.isMine" class="flex justify-end">
              <button
                type="button"
                class="primary-button !px-4 !py-3"
                @click="handleToggleScrap"
              >
                {{ post.isScrapped ? '스크랩 해제' : '스크랩하기' }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <div
        v-if="successMessage || errorMessage"
        class="w-full rounded-2xl border px-4 py-2 text-sm font-medium leading-5 shadow-sm"
        :class="successMessage
          ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
          : 'border-rose-200 bg-rose-50 text-rose-700'"
      >
        {{ successMessage || errorMessage }}
      </div>

      <section class="community-detail-body rounded-card border border-neutral-200 bg-white px-5 py-5 shadow-soft lg:px-6">
        <div class="flex flex-1 flex-col">
          <div class="community-detail-content flex-1 rounded-2xl border border-neutral-200 bg-neutral-50/50 px-5 py-5">
            <p class="whitespace-pre-line text-[15px] leading-8 text-neutral-700">
              {{ post.content }}
            </p>
          </div>

          <div class="mt-6 flex flex-wrap justify-end gap-3">
            <RouterLink to="/community" class="secondary-button">목록</RouterLink>
            <RouterLink
              v-if="post.isMine"
              :to="`/community/${post.postId}/edit`"
              class="secondary-button"
            >
              수정
            </RouterLink>
            <button
              v-if="post.isMine"
              type="button"
              class="secondary-button border-rose-200 text-rose-600 hover:bg-rose-50"
              @click="showDeleteConfirm = true"
            >
              삭제
            </button>
          </div>
        </div>
      </section>
    </div>

    <section
      v-else-if="isLoading"
      class="rounded-card border border-neutral-200 bg-white px-6 py-10 text-center shadow-soft"
    >
      <p class="text-base font-semibold text-neutral-900">게시글을 불러오는 중입니다.</p>
    </section>

    <section
      v-else
      class="rounded-card border border-neutral-200 bg-white px-6 py-10 text-center shadow-soft"
    >
      <p class="text-lg font-semibold text-neutral-900">해당 게시글을 찾을 수 없습니다.</p>
      <RouterLink to="/community" class="mt-4 inline-flex text-sm font-semibold text-brand-600">
        목록으로 돌아가기
      </RouterLink>
    </section>

    <div
      v-if="showDeleteConfirm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/40 px-4"
    >
      <div class="w-full max-w-md rounded-modal bg-white p-6 shadow-floating">
        <h3 class="mt-2 text-xl font-bold tracking-[-0.02em] text-neutral-900">
          정말로 이 게시글을 삭제하시겠습니까?
        </h3>
        <p class="mt-3 text-sm leading-6 text-neutral-500">
          삭제 후에는 복구할 수 없으며, 스크랩 목록에서도 함께 제거됩니다.
        </p>
        <div class="mt-6 flex justify-end gap-3">
          <button type="button" class="secondary-button" @click="showDeleteConfirm = false">
            취소
          </button>
          <button
            type="button"
            class="primary-button bg-danger hover:bg-red-600 active:bg-red-700"
            @click="handleDelete"
          >
            삭제
          </button>
        </div>
      </div>
    </div>
  </AppShell>
</template>

<style scoped>
.community-detail-page {
  min-height: calc(100vh - 220px);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.community-detail-header {
  min-height: 0 !important;
  height: auto !important;
}

.community-detail-body {
  flex: 1;
  min-height: calc(100vh - 390px);
  display: flex;
  flex-direction: column;
}

.community-detail-content {
  min-height: 280px;
  box-shadow: inset 0 0 0 1px rgba(23, 23, 23, 0.02);
}

@media (max-width: 1024px) {
  .community-detail-page {
    min-height: auto;
  }

  .community-detail-body {
    min-height: 320px;
  }

  .community-detail-content {
    min-height: 220px;
  }
}
</style>
