<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppShell from '@/components/layout/AppShell.vue'
import { useCommunityStore } from '@/stores/community'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const communityStore = useCommunityStore()
const authStore = useAuthStore()
const { successMessage, errorMessage } = storeToRefs(communityStore)

const isEditMode = computed(() => route.name === 'community-edit')
const currentPostId = computed(() => Number(route.params.postId))

const form = reactive({
  title: '',
  content: '',
  tagsInput: '',
})

const pageTitle = computed(() => (isEditMode.value ? '게시글 수정' : '게시글 작성'))
const pageDescription = computed(() =>
  isEditMode.value
    ? '제목과 내용을 수정하면 상세 화면에 즉시 반영됩니다.'
    : '구독, 지출, 절감 팁, 서비스 비교 등 다른 사용자에게 도움이 되는 내용을 작성하세요.',
)

const hydrateForm = async () => {
  if (!isEditMode.value) return
  let target = communityStore.currentPost
  if (!target || target.postId !== currentPostId.value) {
    await communityStore.fetchPostDetail(currentPostId.value)
    target = communityStore.currentPost
  }
  if (!target) return

  form.title = target.title
  form.content = target.content
  form.tagsInput = (target.tags ?? []).join(', ')
}

onMounted(hydrateForm)

const parseTags = () => {
  return form.tagsInput
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean)
}

const handleSubmit = async () => {
  if (!authStore.isLoggedIn) {
    communityStore.setError('게시글 작성 및 수정은 로그인 후 이용할 수 있습니다.')
    return
  }

  if (!form.title.trim() || !form.content.trim()) {
    communityStore.setError('제목과 내용을 모두 입력해주세요.')
    return
  }

  const payload = {
    title: form.title,
    content: form.content,
    tags: parseTags(),
  }

  if (isEditMode.value) {
    const updated = await communityStore.updatePost(currentPostId.value, payload)
    if (updated) {
      router.push(`/community/${updated.postId}`)
    }
    return
  }

  const created = await communityStore.createPost(payload)
  if (created) {
    router.push(`/community/${created.postId}`)
  }
}
</script>

<template>
  <AppShell title="hidden">
    <div class="grid gap-6">
      <section class="shell-card p-8">
        <div class="flex flex-col gap-6 xl:flex-row xl:items-end xl:justify-between">
          <div>
            <div class="flex flex-wrap items-center gap-3">
              <!-- <span class="rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-600 ring-1 ring-brand-100">{{ pageTitle }}</span> -->
              <!-- <span class="text-sm text-neutral-500">커뮤니티 작성 폼</span> -->
            </div>
            <h1 class="mt-4 text-[32px] font-bold tracking-[-0.03em] text-neutral-900">{{ pageTitle }}</h1>
            <p class="mt-3 max-w-3xl text-sm leading-7 text-neutral-500">{{ pageDescription }}</p>
          </div>

          <div class="flex flex-wrap gap-3">
            <RouterLink to="/community" class="secondary-button">목록</RouterLink>
            <RouterLink v-if="!authStore.isLoggedIn" to="/login" class="primary-button">로그인</RouterLink>
          </div>
        </div>
      </section>

      <section v-if="successMessage || errorMessage" class="rounded-card border px-5 py-4 text-sm font-medium"
        :class="successMessage ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-rose-200 bg-rose-50 text-rose-700'">
        {{ successMessage || errorMessage }}
      </section>

      <section class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
        <form class="shell-card p-8" @submit.prevent="handleSubmit">
          <div class="grid gap-6">
            <div>
              <label class="form-label" for="community-title">제목</label>
              <input id="community-title" v-model="form.title" type="text" class="form-input mt-2" placeholder="예: AI 구독 조합 추천 부탁드립니다" />
            </div>

            <!-- <div>
              <label class="form-label" for="community-tags">태그</label>
              <input id="community-tags" v-model="form.tagsInput" type="text" class="form-input mt-2" placeholder="예: AI, OTT, 절감" />
              <p class="mt-2 text-xs text-neutral-500">쉼표로 구분해서 입력하면 카드와 상세 화면에서 태그로 표시됩니다.</p>
            </div> -->

            <div>
              <label class="form-label" for="community-content">내용</label>
              <textarea
                id="community-content"
                v-model="form.content"
                class="form-input mt-2 min-h-[320px] resize-none py-4"
                placeholder="구독 서비스 이름, 가격, 사용 목적, 고민 지점을 구체적으로 적어보세요."
              ></textarea>
            </div>

            <div class="flex flex-wrap justify-end gap-3">
              <RouterLink to="/community" class="secondary-button">취소</RouterLink>
              <button type="submit" class="primary-button" :disabled="!authStore.isLoggedIn">{{ isEditMode ? '수정 저장' : '등록하기' }}</button>
            </div>
          </div>
        </form>

        <aside class="grid gap-6 xl:sticky xl:top-6 xl:self-start">
          <article class="shell-card p-6">
            <p class="text-sm font-semibold text-brand-600">작성 가이드</p>
            <ul class="mt-4 grid gap-3 text-sm leading-6 text-neutral-500">
              <li>제목에는 질문 또는 공유 목적을 명확하게 적습니다.</li>
              <li>금액, 주기, 사용 빈도를 함께 적으면 답변이 더 구체적입니다.</li>
              <li>비교 요청인 경우 현재 쓰는 서비스와 바꾸고 싶은 이유를 같이 적어주세요.</li>
            </ul>
          </article>

          <article class="shell-card p-6">
            <p class="text-sm font-semibold text-brand-600">현재 작성자</p>
            <p class="mt-3 text-lg font-bold tracking-[-0.02em] text-neutral-900">{{ authStore.isLoggedIn ? `${authStore.nickname}님` : '로그인 필요' }}</p>
            <p class="mt-2 text-sm leading-6 text-neutral-500">로그인 후 작성하면 본인 글에 한해 수정/삭제가 가능합니다.</p>
          </article>
        </aside>
      </section>
    </div>
  </AppShell>
</template>
