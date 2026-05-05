<script setup>
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import AppShell from '@/components/layout/AppShell.vue'
import { useSupportStore } from '@/stores/support'
import { coreImage } from '@/utils/imageAssets'
import AppStatePanel from '@/components/ui/AppStatePanel.vue'

const supportStore = useSupportStore()
const { faqCategories, filteredFaqItems, faqSearchKeyword, selectedFaqCategory } = storeToRefs(supportStore)

const openedId = ref(filteredFaqItems.value[0]?.id ?? null)

const faqCountLabel = computed(() => `${filteredFaqItems.value.length}개의 질문`)
const supportCards = [
  { title: '결제 캘린더 보기', to: '/calendar', icon: coreImage('calendar') },
  { title: '구독 등록하러 가기', to: '/subscriptions/new', icon: coreImage('registration') },
  { title: '마이페이지 열기', to: '/mypage', icon: coreImage('inventory') },
]

const toggleItem = (id) => {
  openedId.value = openedId.value === id ? null : id
}
</script>

<template>
  <AppShell>
    <div class="grid gap-6">
      <section class="shell-card p-6 lg:p-8">
        <div class="flex flex-col gap-6 xl:flex-row xl:items-end xl:justify-between">
          <div>
            <p class="text-sm font-semibold text-brand-600">FAQ</p>
            <h2 class="mt-3 text-3xl font-bold tracking-[-0.03em] text-neutral-900">자주 묻는 질문</h2>
            <p class="mt-3 text-sm leading-7 text-neutral-500">가입, 구독 등록, 결제 캘린더, 마이페이지, 커뮤니티까지 기능 흐름 중심으로 묶었습니다.</p>
          </div>
          <div class="rounded-card border border-neutral-200 bg-neutral-50 px-5 py-4 text-sm font-semibold text-neutral-700">
            {{ faqCountLabel }} · {{ selectedFaqCategory }}
          </div>
        </div>

        <div class="mt-6 grid gap-4 xl:grid-cols-[1fr_auto] xl:items-center">
          <input
            :value="faqSearchKeyword"
            type="text"
            class="form-input"
            placeholder="질문이나 키워드 검색 (예: 구독 등록, 알림, 캘린더)"
            @input="supportStore.setFaqSearchKeyword($event.target.value)"
          />

          <div class="flex flex-wrap gap-2">
            <button
              v-for="category in faqCategories"
              :key="category"
              type="button"
              class="chip-button"
              :class="{ 'is-selected': selectedFaqCategory === category }"
              @click="supportStore.setFaqCategory(category)"
            >
              {{ category }}
            </button>
          </div>
        </div>
      </section>

      <section class="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
        <div class="shell-card p-6">
          <div v-if="filteredFaqItems.length" class="space-y-4">
            <article
              v-for="item in filteredFaqItems"
              :key="item.id"
              class="rounded-card border border-neutral-200 p-5"
            >
              <button type="button" class="flex w-full items-start justify-between gap-4 text-left" @click="toggleItem(item.id)">
                <div>
                  <div class="flex flex-wrap items-center gap-2">
                    <span class="rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-600">{{ item.category }}</span>
                    <span v-if="item.isPopular" class="rounded-full bg-neutral-100 px-3 py-1 text-xs font-semibold text-neutral-700">인기</span>
                  </div>
                  <h3 class="mt-3 text-base font-semibold text-neutral-900">{{ item.question }}</h3>
                </div>
                <span class="mt-1 text-sm font-semibold text-neutral-400">{{ openedId === item.id ? '−' : '+' }}</span>
              </button>

              <div v-if="openedId === item.id" class="mt-4 border-t border-neutral-200 pt-4">
                <p class="text-sm leading-7 text-neutral-500">{{ item.answer }}</p>
                <div class="mt-4 flex flex-wrap gap-2">
                  <span v-for="tag in item.tags" :key="tag" class="rounded-full bg-neutral-100 px-3 py-1 text-xs font-medium text-neutral-500">#{{ tag }}</span>
                </div>
              </div>
            </article>
          </div>
          <AppStatePanel
            v-else
            title="조건에 맞는 질문이 없습니다"
            description="검색어를 조금 줄이거나 카테고리를 전체로 바꾸면 관련 질문을 다시 찾을 수 있습니다."
            icon="faq"
          >
            <template #actions>
              <button type="button" class="secondary-button" @click="supportStore.setFaqSearchKeyword(''); supportStore.setFaqCategory('전체')">검색 초기화</button>
            </template>
          </AppStatePanel>
        </div>

        <div class="grid gap-6">
          <div class="shell-card p-6">
            <h3 class="section-title">바로 이동 가능한 화면</h3>
            <p class="section-subtitle mt-2">자주 찾는 메뉴로 바로 이동할 수 있습니다.</p>
            <div class="mt-6 grid gap-3">
              <RouterLink v-for="item in supportCards" :key="item.to" :to="item.to" class="secondary-button !justify-start !gap-3">
                <img v-if="item.icon" :src="item.icon" :alt="item.title" class="h-5 w-5 object-contain" />
                <span>{{ item.title }}</span>
              </RouterLink>
            </div>
          </div>

          <div class="shell-card p-6">
            <h3 class="section-title">자주 묶이는 주제</h3>
            <div class="mt-5 grid gap-3">
              <div class="rounded-card border border-neutral-200 p-4">
                <p class="text-sm font-semibold text-neutral-900">회원가입과 로그인</p>
                <p class="mt-2 text-sm leading-6 text-neutral-500">중복 확인, 로그인 실패, 로그아웃 관련 문의가 가장 많습니다.</p>
              </div>
              <div class="rounded-card border border-neutral-200 p-4">
                <p class="text-sm font-semibold text-neutral-900">구독 등록과 카드 선택</p>
                <p class="mt-2 text-sm leading-6 text-neutral-500">서비스 직접 입력, 결제 주기, 카드 연결 방식 관련 질문이 자주 나옵니다.</p>
              </div>
              <div class="rounded-card border border-neutral-200 p-4">
                <p class="text-sm font-semibold text-neutral-900">결제 캘린더 읽는 법</p>
                <p class="mt-2 text-sm leading-6 text-neutral-500">월 전체 흐름, 날짜별 결제 항목, 카테고리별 소비 현황을 함께 보는 질문이 많습니다.</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </AppShell>
</template>
