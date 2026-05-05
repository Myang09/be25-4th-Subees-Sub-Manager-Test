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

const reportId = computed(() => route.params.recommendationId || aiStore.activeReportId || '')
const report = computed(() => aiStore.getReportById(reportId.value))
const titleInput = ref('')
const showDeleteConfirm = ref(false)
const isDeleteSubmitting = ref(false)

const mandatoryItems = computed(() => aiStore.parseItemsJson(report.value?.mandatoryItemsJson))
const optionalItems = computed(() => aiStore.parseItemsJson(report.value?.optionalItemsJson))

const hydrateReport = async () => {
  if (!reportId.value) return

  try {
    await aiStore.fetchReportDetail(reportId.value)
  } catch (error) {
    // 스토어에서 에러 상태를 관리합니다.
  }
}

onMounted(hydrateReport)
watch(reportId, async () => {
  titleInput.value = ''
  await hydrateReport()
})
watch(report, (value) => {
  if (value && !titleInput.value) {
    titleInput.value = value.reportTitle || ''
  }
})

const startEdit = () => {
  titleInput.value = report.value?.reportTitle || ''
}

const saveTitle = async () => {
  if (!report.value) return

  try {
    const saved = await aiStore.updateReportTitle(report.value.reportId, titleInput.value || report.value.reportTitle)
    titleInput.value = saved?.reportTitle || titleInput.value
  } catch (error) {
    // 스토어에서 에러 상태를 관리합니다.
  }
}

const saveReport = async () => {
  if (!report.value) return

  try {
    const saved = await aiStore.saveReport(report.value.reportId, { reportTitle: titleInput.value || report.value.reportTitle })
    titleInput.value = saved?.reportTitle || titleInput.value
  } catch (error) {
    // 스토어에서 에러 상태를 관리합니다.
  }
}

const openDeleteConfirm = () => {
  if (!report.value) return
  showDeleteConfirm.value = true
}

const closeDeleteConfirm = () => {
  if (isDeleteSubmitting.value) return
  showDeleteConfirm.value = false
}

const confirmDelete = async () => {
  if (!report.value) return

  isDeleteSubmitting.value = true

  try {
    await aiStore.deleteReport(report.value.reportId)
    showDeleteConfirm.value = false
    router.push('/ai-recommendations/history')
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
          <p class="page-eyebrow text-warning">개별 추천 기록 확인</p>
          <h1 class="mt-2 text-[30px] font-black tracking-[-0.045em] text-neutral-900 xl:text-[36px]">저장된 추천 내용을 자세히 확인하세요</h1>
          <p class="mt-3 max-w-4xl text-sm leading-7 text-neutral-500">제목 수정, 본문 확인, 삭제, 목록 복귀 흐름을 설계서 기준으로 정리했습니다.</p>
        </div>
        <div class="flex flex-wrap items-center gap-2.5 xl:justify-end">
          <RouterLink to="/ai-recommendations" class="chip-button">추천 받기</RouterLink>
          <RouterLink to="/ai-recommendations/history" class="chip-button is-selected">추천 기록</RouterLink>
        </div>
      </div>

      <div v-if="report" class="grid gap-6 px-6 py-6 xl:grid-cols-[minmax(0,1.08fr)_360px] xl:px-7 xl:py-7">
        <div class="grid gap-5">
          <article class="dense-card">
            <div class="flex flex-col gap-4 border-b border-[rgba(46,34,10,0.08)] pb-4 lg:flex-row lg:items-end lg:justify-between">
              <div class="min-w-0 flex-1">
                <p class="text-sm font-semibold text-warning">추천내용 제목</p>
                <input
                  v-model="titleInput"
                  type="text"
                  class="form-input mt-3"
                  :placeholder="report.reportTitle"
                  @focus="!titleInput && startEdit()"
                />
              </div>
              <div class="flex shrink-0 flex-wrap items-center justify-end gap-2 lg:self-end">
                <button type="button" class="secondary-button !min-h-[44px] !rounded-[16px] !px-4" :disabled="aiStore.isSubmitting" @click="saveTitle">{{ aiStore.isSubmitting ? '저장 중...' : '제목 저장' }}</button>
                <button type="button" class="primary-button !min-h-[44px] !rounded-[16px] !px-4" :disabled="aiStore.isSubmitting" @click="saveReport">{{ aiStore.isSubmitting ? '저장 중...' : '저장' }}</button>
                <button type="button" class="danger-ghost-button !min-h-[44px] !rounded-[16px] !px-4" :disabled="aiStore.isSubmitting || isDeleteSubmitting" @click="openDeleteConfirm">삭제</button>
                <button type="button" class="tertiary-button !min-h-[44px] !rounded-[16px] !px-4" @click="router.push('/ai-recommendations/history')">돌아가기</button>
              </div>
            </div>

            <div class="mt-5 rounded-[24px] border border-[rgba(46,34,10,0.08)] bg-white/84 p-5">
              <pre class="whitespace-pre-wrap break-words font-inherit text-sm leading-7 text-neutral-600">{{ report.generatedContent }}</pre>
            </div>
          </article>

          <div class="grid gap-5 lg:grid-cols-2">
            <article class="dense-card">
              <p class="text-sm font-semibold text-warning">입력한 구독 항목</p>
              <div class="mt-4 grid gap-3">
                <article v-for="item in report.subscriptionItems" :key="item.itemId" class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white/84 px-4 py-3">
                  <div class="flex items-start gap-3">
                    <span class="grid h-11 w-11 shrink-0 place-items-center rounded-[14px] bg-brand-50">
                      <AppAsset type="service" :value="item.serviceName" fallback="bot" :size="16" wrapper-class="inline-flex items-center justify-center" image-class="h-6 w-6 object-contain" icon-class="text-warning" />
                    </span>
                    <div>
                      <p class="text-sm font-bold text-neutral-900">{{ item.serviceName }}</p>
                      <p class="mt-1 text-xs text-neutral-500">{{ item.category }} · {{ Number(item.monthlyPrice || 0).toLocaleString('ko-KR') }}원</p>
                      <p class="mt-2 text-sm leading-6 text-neutral-500">{{ item.description || '설명 없음' }}</p>
                    </div>
                  </div>
                </article>
              </div>
            </article>

            <article class="dense-card">
              <p class="text-sm font-semibold text-warning">저장 정보</p>
              <div class="mt-4 grid gap-3">
                <div class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white/84 px-4 py-3">
                  <p class="text-xs font-bold uppercase tracking-[0.12em] text-neutral-300">총 가격</p>
                  <p class="mt-2 text-lg font-black text-neutral-900">{{ Number(report.totalMonthlyPrice || 0).toLocaleString('ko-KR') }}원</p>
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
            <p class="text-sm font-semibold text-warning">안내</p>
            <ul class="mt-3 grid gap-2 text-sm leading-6 text-neutral-500">
              <li>• 제목을 수정한 뒤 저장 버튼으로 기록을 갱신할 수 있습니다.</li>
              <li>• 삭제 버튼을 누르면 화면 내 확인 창이 먼저 표시됩니다.</li>
              <li>• 돌아가기 버튼으로 기록 목록으로 다시 이동할 수 있습니다.</li>
            </ul>
          </article>
        </aside>
      </div>

      <div v-else class="px-6 py-16 text-center xl:px-7">
        <p class="text-lg font-bold text-neutral-900">선택한 추천 기록을 찾을 수 없습니다.</p>
        <div class="mt-6 flex justify-center">
          <RouterLink to="/ai-recommendations/history" class="primary-button">추천 기록 목록</RouterLink>
        </div>
      </div>
    </section>

    <AppConfirmDialog
      :open="showDeleteConfirm"
      title="이 추천 기록을 삭제할까요?"
      :description="`삭제 후에는 ${report?.reportTitle || '선택한 기록'} 내용을 다시 복구할 수 없습니다.`"
      confirm-text="삭제"
      cancel-text="취소"
      tone="danger"
      :loading="isDeleteSubmitting"
      @cancel="closeDeleteConfirm"
      @confirm="confirmDelete"
    />
  </AppShell>
</template>
