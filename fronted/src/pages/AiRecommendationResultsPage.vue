<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppShell from '@/components/layout/AppShell.vue'
import AppAsset from '@/components/ui/AppAsset.vue'
import AppConfirmDialog from '@/components/ui/AppConfirmDialog.vue'
import { useAiRecommendationsStore } from '@/stores/aiRecommendations'

const route = useRoute()
const router = useRouter()
const aiStore = useAiRecommendationsStore()

const showSaveSuccessModal = ref(false)

const reportId = computed(() => route.query.reportId || aiStore.activeReportId || '')
const report = computed(() => aiStore.getReportById(reportId.value) || aiStore.currentReport || aiStore.generatedReports[0] || null)
const mandatoryItems = computed(() => aiStore.parseItemsJson(report.value?.mandatoryItemsJson))
const optionalItems = computed(() => aiStore.parseItemsJson(report.value?.optionalItemsJson))
const budgetRemaining = computed(() => Number(report.value?.maxMonthlyBudget || 0) - Number(report.value?.totalMonthlyPrice || 0))
const previewRows = computed(() => (report.value?.subscriptionItems || []).slice(0, 4))

const hydrateReport = async () => {
  if (!reportId.value) return

  try {
    await aiStore.fetchReportDetail(reportId.value)
  } catch (error) {
    // 스토어에서 에러 상태를 관리합니다.
  }
}

onMounted(hydrateReport)
watch(reportId, hydrateReport)

const saveReport = async () => {
  if (!report.value) return

  try {
    await aiStore.saveReport(report.value.reportId)
    showSaveSuccessModal.value = true
  } catch (error) {
    // 스토어에서 에러 상태를 관리합니다.
  }
}

const moveToHistory = () => {
  showSaveSuccessModal.value = false
  aiStore.clearRecommendationSession()
  router.push('/ai-recommendations/history')
}
</script>

<template>
  <AppShell title="hidden">
    <section class="shell-card overflow-hidden p-0">
      <div class="grid gap-5 border-b border-[rgba(46,34,10,0.08)] bg-[linear-gradient(180deg,rgba(240,228,186,0.34),rgba(255,253,247,0.96))] px-6 py-6 xl:grid-cols-[minmax(0,1fr)_auto] xl:items-end">
        <div>
          <p class="page-eyebrow text-warning">AI 추천 결과</p>
          <h1 class="mt-2 text-[30px] font-black tracking-[-0.045em] text-neutral-900 xl:text-[36px]">추천 결과를 확인하고 저장할 수 있습니다</h1>
          <p class="mt-3 max-w-4xl text-sm leading-7 text-neutral-500">화면기능설계서의 결과 확인 화면에 맞춰 제목, 총 비용, 추천 내용, 저장 버튼을 한 화면에 정리했습니다.</p>
        </div>
        <div class="flex flex-wrap items-center gap-2.5 xl:justify-end">
          <RouterLink to="/ai-recommendations" class="chip-button">추천 받기</RouterLink>
          <RouterLink to="/ai-recommendations/results" class="chip-button is-selected">추천 결과</RouterLink>
          <RouterLink to="/ai-recommendations/history" class="chip-button">추천 기록</RouterLink>
        </div>
      </div>

      <div v-if="report" class="grid gap-6 px-6 py-6 xl:grid-cols-[minmax(0,1.08fr)_360px] xl:px-7 xl:py-7">
        <div class="grid gap-5">
          <article class="dense-card">
            <div class="flex flex-col gap-4 border-b border-[rgba(46,34,10,0.08)] pb-4 lg:flex-row lg:items-end lg:justify-between">
              <div class="min-w-0 flex-1">
                <p class="text-sm font-semibold text-warning">추천 결과 제목</p>
                <h2 class="mt-2 text-[28px] font-black tracking-[-0.03em] text-neutral-900">{{ report.reportTitle }}</h2>
                <p class="mt-2 text-sm text-neutral-500">생성일 {{ aiStore.toDateLabel(report.updatedAt) }}</p>
              </div>
              <div class="flex shrink-0 flex-wrap items-center justify-end gap-2 lg:self-end">
                <button type="button" class="secondary-button !min-h-[44px] !rounded-[16px] !px-4" @click="router.push('/ai-recommendations')">돌아가기</button>
                <button type="button" class="primary-button !min-h-[44px] !rounded-[16px] !px-4" :disabled="aiStore.isSubmitting" @click="saveReport">
                  {{ aiStore.isSubmitting ? '저장 중...' : '저장' }}
                </button>
              </div>
            </div>

            <div class="mt-5 rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-white/84 p-5">
              <pre class="whitespace-pre-wrap break-words font-inherit text-sm leading-7 text-neutral-600">{{ report.generatedContent }}</pre>
            </div>
          </article>

          <div class="grid gap-5 lg:grid-cols-2">
            <article class="dense-card">
              <p class="text-sm font-semibold text-warning">서비스 추천 요약</p>
              <div class="mt-4 overflow-hidden rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white/84">
                <div class="grid grid-cols-[minmax(0,1fr)_110px] border-b border-[rgba(46,34,10,0.08)] px-4 py-3 text-xs font-bold uppercase tracking-[0.12em] text-neutral-300">
                  <span>서비스 명</span>
                  <span class="text-right">가격(원)</span>
                </div>
                <div v-for="item in previewRows" :key="item.itemId" class="grid grid-cols-[minmax(0,1fr)_110px] items-center gap-3 border-b border-[rgba(46,34,10,0.06)] px-4 py-3 last:border-b-0">
                  <div class="flex items-center gap-3">
                    <span class="grid h-10 w-10 place-items-center rounded-[14px] bg-brand-50">
                      <AppAsset type="service" :value="item.serviceName" fallback="bot" :size="16" wrapper-class="inline-flex items-center justify-center" image-class="h-6 w-6 object-contain" icon-class="text-warning" />
                    </span>
                    <div class="min-w-0">
                      <p class="truncate text-sm font-bold text-neutral-900">{{ item.serviceName }}</p>
                      <p class="text-xs text-neutral-500">{{ item.description || '추천 사유 요약' }}</p>
                    </div>
                  </div>
                  <span class="text-right text-sm font-semibold text-neutral-900">{{ Number(item.monthlyPrice || 0).toLocaleString('ko-KR') }}</span>
                </div>
              </div>
            </article>

            <article class="dense-card">
              <p class="text-sm font-semibold text-warning">조건 확인</p>
              <div class="mt-4 grid gap-3">
                <div class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white/84 px-4 py-3">
                  <div class="flex items-center justify-between gap-3 text-sm">
                    <span class="text-neutral-500">총 비용</span>
                    <strong class="text-neutral-900">{{ Number(report.totalMonthlyPrice || 0).toLocaleString('ko-KR') }}원</strong>
                  </div>
                  <div class="mt-2 flex items-center justify-between gap-3 text-sm">
                    <span class="text-neutral-500">예산 차이</span>
                    <strong :class="budgetRemaining >= 0 ? 'status-success' : 'status-danger'">{{ budgetRemaining >= 0 ? '+' : '' }}{{ budgetRemaining.toLocaleString('ko-KR') }}원</strong>
                  </div>
                </div>
                <div class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white/84 px-4 py-3">
                  <p class="text-xs font-bold uppercase tracking-[0.12em] text-neutral-300">필수 구독 항목</p>
                  <div class="mt-3 flex flex-wrap gap-2">
                    <span v-for="item in mandatoryItems" :key="item" class="rounded-full bg-brand-100 px-3 py-1 text-xs font-bold text-warning">{{ item }}</span>
                  </div>
                </div>
                <div class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white/84 px-4 py-3">
                  <p class="text-xs font-bold uppercase tracking-[0.12em] text-neutral-300">선택 구독 항목</p>
                  <div class="mt-3 flex flex-wrap gap-2">
                    <span v-for="item in optionalItems" :key="item" class="rounded-full bg-neutral-100 px-3 py-1 text-xs font-bold text-neutral-500">{{ item }}</span>
                  </div>
                </div>
              </div>
            </article>
          </div>
        </div>

        <aside class="grid gap-5 xl:sticky xl:top-6 xl:self-start">
          <article class="guide-card">
            <p class="text-sm font-semibold text-warning">다음 단계</p>
            <h2 class="mt-2 text-[22px] font-black tracking-[-0.03em] text-neutral-900">저장 후에는 추천 기록 목록에서 바로 확인할 수 있습니다</h2>
            <div class="mt-5 grid gap-2.5">
              <RouterLink v-if="report" :to="`/ai-recommendations/${report.reportId}`" class="secondary-button w-full text-center">상세 보기</RouterLink>
              <RouterLink to="/ai-recommendations/history" class="secondary-button w-full text-center">추천 기록 보기</RouterLink>
            </div>
          </article>
        </aside>
      </div>

      <div v-else class="px-6 py-16 text-center xl:px-7">
        <p class="text-lg font-bold text-neutral-900">생성된 추천 결과가 없습니다.</p>
        <div class="mt-6 flex justify-center">
          <RouterLink to="/ai-recommendations" class="primary-button">추천 받기</RouterLink>
        </div>
      </div>
    </section>

    <AppConfirmDialog
      :open="showSaveSuccessModal"
      title="추천 기록이 저장되었습니다"
      description="확인 버튼을 누르면 추천 기록 목록으로 이동합니다."
      confirm-text="확인"
      :show-cancel="false"
      :show-close="false"
      :persistent="true"
      @confirm="moveToHistory"
    />
  </AppShell>
</template>
