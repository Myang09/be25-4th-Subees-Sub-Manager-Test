<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppShell from '@/components/layout/AppShell.vue'
import AppConfirmDialog from '@/components/ui/AppConfirmDialog.vue'
import { useAiRecommendationsStore } from '@/stores/aiRecommendations'

const router = useRouter()
const aiStore = useAiRecommendationsStore()
const keyword = ref('')
const showDeleteConfirm = ref(false)
const isDeleteSubmitting = ref(false)
const targetReportId = ref(null)

const savedReports = computed(() => aiStore.savedReports)
const filteredReports = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  if (!query) return savedReports.value
  return savedReports.value.filter((report) => {
    const joinedNames = (report.subscriptionItems || []).map((item) => item.serviceName).join(' ').toLowerCase()
    return report.reportTitle.toLowerCase().includes(query) || joinedNames.includes(query)
  })
})
const targetReport = computed(() => filteredReports.value.find((item) => String(item.reportId) === String(targetReportId.value)) || savedReports.value.find((item) => String(item.reportId) === String(targetReportId.value)) || null)

onMounted(async () => {
  try {
    await aiStore.fetchSavedReports()
  } catch (error) {
    // 스토어에서 에러 상태를 관리합니다.
  }
})

const openDetail = (reportId) => {
  aiStore.setActiveReport(reportId)
  router.push(`/ai-recommendations/${reportId}`)
}

const openDeleteConfirm = (reportId) => {
  targetReportId.value = reportId
  showDeleteConfirm.value = true
}

const closeDeleteConfirm = () => {
  if (isDeleteSubmitting.value) return
  showDeleteConfirm.value = false
  targetReportId.value = null
}

const confirmDelete = async () => {
  if (!targetReportId.value) return

  isDeleteSubmitting.value = true

  try {
    await aiStore.deleteReport(targetReportId.value)
    showDeleteConfirm.value = false
    targetReportId.value = null
  } catch (error) {
    // 스토어에서 에러 상태를 관리합니다.
  } finally {
    isDeleteSubmitting.value = false
  }
}
</script>

<template>
  <AppShell title="hidden">
    <section class="shell-card overflow-hidden p-0">
      <div class="grid gap-5 border-b border-[rgba(46,34,10,0.08)] bg-[linear-gradient(180deg,rgba(240,228,186,0.34),rgba(255,253,247,0.96))] px-6 py-6 xl:grid-cols-[minmax(0,1fr)_auto] xl:items-end">
        <div>
          <p class="page-eyebrow text-warning">추천 기록 목록</p>
          <h1 class="mt-2 text-[30px] font-black tracking-[-0.045em] text-neutral-900 xl:text-[36px]">저장한 추천 기록을 한눈에 모아보세요</h1>
          <p class="mt-3 max-w-4xl text-sm leading-7 text-neutral-500">설계서의 기록 목록 화면처럼 검색, 추가, 상세 보기 흐름을 한 곳에 묶었습니다.</p>
        </div>
        <div class="flex flex-wrap items-center gap-2.5 xl:justify-end">
          <RouterLink to="/ai-recommendations" class="chip-button">추천 받기</RouterLink>
          <RouterLink to="/ai-recommendations/results" class="chip-button">추천 결과</RouterLink>
          <RouterLink to="/ai-recommendations/history" class="chip-button is-selected">추천 기록</RouterLink>
        </div>
      </div>

      <div class="px-6 py-6 xl:px-7 xl:py-7">
        <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div class="flex min-w-0 flex-1 items-center gap-3">
            <input v-model="keyword" type="text" class="form-input max-w-[360px]" placeholder="서비스 이름이나 제목으로 검색" />
            <RouterLink to="/ai-recommendations" class="secondary-button !min-h-[44px] !rounded-[16px] !px-4">추가</RouterLink>
          </div>
          <p class="text-sm text-neutral-500">총 {{ filteredReports.length }}개의 저장 목록</p>
        </div>

        <div v-if="filteredReports.length" class="mt-5 grid gap-4">
          <article v-for="report in filteredReports" :key="report.reportId" class="rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.92)] px-5 py-5 shadow-soft">
            <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div class="min-w-0 flex-1">
                <h2 class="truncate text-[22px] font-black tracking-[-0.03em] text-neutral-900">{{ report.reportTitle }}</h2>
                <p class="mt-1 text-sm text-neutral-500">등록일 {{ aiStore.toDateLabel(report.updatedAt) }}</p>
                <p class="mt-4 text-sm leading-7 text-neutral-500">{{ (report.subscriptionItems || []).map((item) => item.serviceName).join(', ') || '선택한 서비스 없음' }}</p>
              </div>

              <div class="shrink-0 rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white/84 px-4 py-3 text-right">
                <p class="text-xs font-bold uppercase tracking-[0.12em] text-neutral-300">총 가격</p>
                <p class="mt-2 text-lg font-black text-neutral-900">{{ Number(report.totalMonthlyPrice || 0).toLocaleString('ko-KR') }}원</p>
              </div>
            </div>

            <div class="mt-4 flex flex-wrap items-center justify-end gap-2">
              <button type="button" class="secondary-button !min-h-[44px] !rounded-[16px] !px-4" @click="openDetail(report.reportId)">상세 보기</button>
              <button type="button" class="danger-ghost-button !min-h-[44px] !rounded-[16px] !px-4" :disabled="isDeleteSubmitting" @click="openDeleteConfirm(report.reportId)">삭제</button>
            </div>
          </article>
        </div>

        <div v-else class="mt-6 rounded-[30px] border border-dashed border-[rgba(46,34,10,0.12)] bg-white/60 px-6 py-12 text-center">
          <p class="text-[22px] font-black tracking-[-0.03em] text-neutral-900">저장된 추천 기록이 없습니다</p>
          <p class="mt-3 text-sm leading-6 text-neutral-500">추천 결과에서 저장하면 이 목록에 표시됩니다.</p>
        </div>
      </div>
    </section>

    <AppConfirmDialog
      :open="showDeleteConfirm"
      title="이 추천 기록을 삭제할까요?"
      :description="`삭제 후에는 ${targetReport?.reportTitle || '선택한 기록'} 내용을 다시 복구할 수 없습니다.`"
      confirm-text="삭제"
      cancel-text="취소"
      tone="danger"
      :loading="isDeleteSubmitting"
      @cancel="closeDeleteConfirm"
      @confirm="confirmDelete"
    />
  </AppShell>
</template>
