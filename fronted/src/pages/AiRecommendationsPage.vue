<script setup>
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppShell from '@/components/layout/AppShell.vue'
import AppAsset from '@/components/ui/AppAsset.vue'
import AppProgressDialog from '@/components/ui/AppProgressDialog.vue'
import { useAuthStore } from '@/stores/auth'
import { useAiRecommendationsStore } from '@/stores/aiRecommendations'

const router = useRouter()
const authStore = useAuthStore()
const aiStore = useAiRecommendationsStore()

const form = reactive({
  reportTitle: aiStore.draft.reportTitle || '',
  requestNote: aiStore.draft.requestNote || '',
  maxMonthlyBudget: Number(aiStore.draft.maxMonthlyBudget || 0),
  mandatoryItems: [...(aiStore.draft.mandatoryItems || [])],
  optionalItems: [...(aiStore.draft.optionalItems || [])],
  subscriptionItems: (aiStore.draft.subscriptionItems || []).map((item) => ({ ...item })),
})

const modalOpen = ref(false)
const modalKind = ref('mandatory')
const editingName = ref('')
const getDefaultCategory = () => aiStore.CATEGORY_OPTIONS?.[0] || ''
const itemForm = reactive({ serviceName: '', category: getDefaultCategory(), monthlyPrice: 0, description: '' })

const QUICK_ADD_PRIORITY = ['Netflix', '유튜브프리미엄', 'ChatGpt', 'Spotify', '쿠팡와우', '네이버멤버십', 'Adobe', 'Notion']
const generationSteps = ['추천 요청 저장', 'AI 추천 생성', '결과 화면 이동']
const progressState = reactive({
  open: false,
  title: '',
  description: '',
  percent: 0,
  currentStep: 0,
})

onMounted(async () => {
  await aiStore.fetchSubscriptionCatalog()
  if (!itemForm.category) {
    itemForm.category = getDefaultCategory()
  }
})

watch(
  () => form,
  () => aiStore.updateDraft(form),
  { deep: true },
)

const totalPrice = computed(() => form.subscriptionItems.reduce((sum, item) => sum + Number(item.monthlyPrice || 0), 0))
const budgetDiff = computed(() => Number(form.maxMonthlyBudget || 0) - totalPrice.value)

const quickAddItems = computed(() => {
  const catalog = Array.isArray(aiStore.SERVICE_LIBRARY) ? aiStore.SERVICE_LIBRARY : []
  const byName = new Map(catalog.map((item) => [item.name, item]))
  const prioritized = QUICK_ADD_PRIORITY.map((name) => byName.get(name)).filter(Boolean)
  const remainder = catalog.filter((item) => !QUICK_ADD_PRIORITY.includes(item.name))
  return [...prioritized, ...remainder].slice(0, 8)
})

const openModal = (kind, preset = null) => {
  modalKind.value = kind
  editingName.value = ''
  itemForm.serviceName = preset?.name || ''
  itemForm.category = preset?.category || getDefaultCategory()
  itemForm.monthlyPrice = Number(preset?.monthlyPrice || 0)
  itemForm.description = preset?.description || ''
  modalOpen.value = true
}

const openEdit = (kind, name) => {
  modalKind.value = kind
  editingName.value = name
  const target = form.subscriptionItems.find((item) => item.serviceName === name)
  itemForm.serviceName = target?.serviceName || name
  itemForm.category = target?.category || getDefaultCategory()
  itemForm.monthlyPrice = Number(target?.monthlyPrice || 0)
  itemForm.description = target?.description || ''
  modalOpen.value = true
}

const upsertItem = (kind, payload, previousName = '') => {
  const name = payload.serviceName.trim()
  if (!name) return

  const addUnique = (list, value) => [...new Set([...list.filter((item) => item !== previousName), value])]
  const removeValue = (list, value) => list.filter((item) => item !== value)

  if (kind === 'mandatory') {
    form.mandatoryItems = addUnique(form.mandatoryItems, name)
    if (previousName) form.optionalItems = removeValue(form.optionalItems, previousName)
  } else {
    form.optionalItems = addUnique(form.optionalItems, name)
    if (previousName) form.mandatoryItems = removeValue(form.mandatoryItems, previousName)
  }

  const nextItem = {
    itemId: form.subscriptionItems.find((item) => item.serviceName === previousName || item.serviceName === name)?.itemId || `draft-${Date.now()}`,
    serviceName: name,
    category: payload.category,
    monthlyPrice: Number(payload.monthlyPrice || 0),
    description: payload.description.trim(),
  }

  const filtered = form.subscriptionItems.filter((item) => item.serviceName !== previousName && item.serviceName !== name)
  form.subscriptionItems = [...filtered, nextItem]
}

const saveModal = () => {
  if (!itemForm.serviceName.trim()) return
  upsertItem(modalKind.value, itemForm, editingName.value)
  modalOpen.value = false
}

const removeItem = (kind, name) => {
  const remove = (list) => list.filter((item) => item !== name)
  if (kind === 'mandatory') form.mandatoryItems = remove(form.mandatoryItems)
  else form.optionalItems = remove(form.optionalItems)

  if (![...form.mandatoryItems, ...form.optionalItems].includes(name)) {
    form.subscriptionItems = form.subscriptionItems.filter((item) => item.serviceName !== name)
  }
}

const priceOf = (name) => form.subscriptionItems.find((item) => item.serviceName === name)?.monthlyPrice || 0

const resetForm = () => {
  aiStore.resetDraft()
  form.reportTitle = ''
  form.requestNote = ''
  form.maxMonthlyBudget = 0
  form.mandatoryItems = []
  form.optionalItems = []
  form.subscriptionItems = []
}

const submitAndGenerate = async () => {
  if (!form.subscriptionItems.length) {
    aiStore.setStatusMessage('최소 1개 이상의 구독 항목을 추가해주세요.')
    return
  }

  progressState.open = true
  progressState.title = '추천 조건을 정리하고 있습니다'
  progressState.description = '입력한 구독 조건을 저장한 뒤 추천 조합 생성을 시작합니다.'
  progressState.percent = 20
  progressState.currentStep = 1

  try {
    const report = await aiStore.submitDraft()

    progressState.title = 'AI가 추천 조합을 생성하고 있습니다'
    progressState.description = '선택한 서비스와 예산을 기준으로 추천 결과를 계산하고 있습니다.'
    progressState.percent = 68
    progressState.currentStep = 2

    const generated = await aiStore.generateRecommendations(report.reportId)

    progressState.title = '결과 화면으로 이동하고 있습니다'
    progressState.description = '생성된 추천 결과를 불러오는 중입니다.'
    progressState.percent = 100
    progressState.currentStep = 3

    await new Promise((resolve) => window.setTimeout(resolve, 320))
    router.push({ path: '/ai-recommendations/results', query: { reportId: generated.reportId } })
  } catch (error) {
    // 스토어에서 상태 메시지를 관리합니다.
  } finally {
    progressState.open = false
  }
}
</script>

<template>
  <AppShell title="hidden">
    <section class="shell-card overflow-hidden p-0">
      <div class="grid gap-5 border-b border-[rgba(46,34,10,0.08)] bg-[linear-gradient(180deg,rgba(240,228,186,0.34),rgba(255,253,247,0.96))] px-6 py-6 xl:grid-cols-[minmax(0,1fr)_auto] xl:items-end">
        <div>
          <p class="page-eyebrow text-warning">AI 추천 서비스</p>
          <h1 class="mt-2 text-[30px] font-black tracking-[-0.045em] text-neutral-900 xl:text-[36px]">{{ authStore.nickname || '사용자' }}님에게 맞는 구독 조합을 정리해보세요</h1>
          <p class="mt-3 max-w-4xl text-sm leading-7 text-neutral-500">화면기능설계서의 흐름을 따라 예산 입력 → 항목 구성 → 실행 순서로 정리했습니다. 색상은 기존 서비스 톤을 그대로 유지했습니다.</p>
        </div>
        <div class="flex flex-wrap items-center gap-2.5 xl:justify-end">
          <RouterLink to="/ai-recommendations" class="chip-button is-selected">추천 받기</RouterLink>
          <RouterLink to="/ai-recommendations/results" class="chip-button">추천 결과</RouterLink>
          <RouterLink to="/ai-recommendations/history" class="chip-button">추천 기록</RouterLink>
        </div>
      </div>

      <div class="grid gap-6 px-6 py-6 xl:grid-cols-[minmax(0,1.08fr)_360px] xl:px-7 xl:py-7">
        <div class="grid gap-5">
          <article class="dense-card">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-sm font-semibold text-warning">설명 및 사용방법</p>
                <h2 class="mt-2 text-[22px] font-bold tracking-[-0.03em] text-neutral-900">리포트 정보를 먼저 입력하세요</h2>
              </div>
              <span class="guide-pill">1단계</span>
            </div>

            <div class="mt-5 grid gap-4 lg:grid-cols-[minmax(0,1fr)_200px]">
              <label class="grid gap-2">
                <span class="form-label">추천 제목</span>
                <input v-model="form.reportTitle" type="text" class="form-input" placeholder="예: 콘텐츠팀 AI 도구 구성안" />
              </label>
              <label class="grid gap-2">
                <span class="form-label">가용 금액 입력</span>
                <div class="relative">
                  <input v-model="form.maxMonthlyBudget" type="number" min="0" class="form-input pr-12" placeholder="0" />
                  <span class="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-sm font-semibold text-neutral-400">원</span>
                </div>
              </label>
            </div>

            <label class="mt-4 grid gap-2">
              <span class="form-label">요청 메모</span>
              <textarea v-model="form.requestNote" rows="4" class="form-input min-h-[120px] resize-none py-4" placeholder="꼭 유지하고 싶은 도구나, 줄이고 싶은 중복 기능을 적어주세요."></textarea>
            </label>
          </article>

          <div class="grid gap-5 lg:grid-cols-2">
            <article class="dense-card">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-sm font-semibold text-warning">필수 구독 항목</p>
                  <p class="mt-1 text-sm text-neutral-500">반드시 유지하거나 포함해야 하는 서비스입니다.</p>
                </div>
                <button type="button" class="secondary-button !min-h-[44px] !rounded-[16px] !px-4" @click="openModal('mandatory')">추가</button>
              </div>

              <div class="mt-4 grid gap-3">
                <article v-for="name in form.mandatoryItems" :key="`m-${name}`" class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white/84 px-4 py-3">
                  <div class="flex items-center justify-between gap-3">
                    <div>
                      <p class="text-sm font-bold text-neutral-900">{{ name }}</p>
                      <p class="mt-1 text-xs text-neutral-500">{{ Number(priceOf(name)).toLocaleString('ko-KR') }}원</p>
                    </div>
                    <div class="flex gap-2">
                      <button type="button" class="tertiary-button !min-h-[36px] !rounded-[12px] !px-3" @click="openEdit('mandatory', name)">수정</button>
                      <button type="button" class="danger-ghost-button !min-h-[36px] !rounded-[12px] !px-3" @click="removeItem('mandatory', name)">삭제</button>
                    </div>
                  </div>
                </article>
                <p v-if="!form.mandatoryItems.length" class="rounded-[20px] border border-dashed border-[rgba(46,34,10,0.12)] bg-white/60 px-4 py-5 text-sm text-neutral-500">필수 항목을 추가해보세요.</p>
              </div>
            </article>

            <article class="dense-card">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-sm font-semibold text-warning">선택 구독 항목</p>
                  <p class="mt-1 text-sm text-neutral-500">비교하거나 함께 검토할 서비스입니다.</p>
                </div>
                <button type="button" class="secondary-button !min-h-[44px] !rounded-[16px] !px-4" @click="openModal('optional')">추가</button>
              </div>

              <div class="mt-4 grid gap-3">
                <article v-for="name in form.optionalItems" :key="`o-${name}`" class="rounded-[20px] border border-[rgba(46,34,10,0.08)] bg-white/84 px-4 py-3">
                  <div class="flex items-center justify-between gap-3">
                    <div>
                      <p class="text-sm font-bold text-neutral-900">{{ name }}</p>
                      <p class="mt-1 text-xs text-neutral-500">{{ Number(priceOf(name)).toLocaleString('ko-KR') }}원</p>
                    </div>
                    <div class="flex gap-2">
                      <button type="button" class="tertiary-button !min-h-[36px] !rounded-[12px] !px-3" @click="openEdit('optional', name)">수정</button>
                      <button type="button" class="danger-ghost-button !min-h-[36px] !rounded-[12px] !px-3" @click="removeItem('optional', name)">삭제</button>
                    </div>
                  </div>
                </article>
                <p v-if="!form.optionalItems.length" class="rounded-[20px] border border-dashed border-[rgba(46,34,10,0.12)] bg-white/60 px-4 py-5 text-sm text-neutral-500">선택 항목을 추가해보세요.</p>
              </div>
            </article>
          </div>

          <article class="dense-card">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-sm font-semibold text-warning">빠른 추가</p>
                <p class="mt-1 text-sm text-neutral-500">자주 쓰는 서비스는 바로 선택해서 채울 수 있습니다.</p>
              </div>
            </div>
            <div class="mt-4 flex flex-wrap gap-3">
              <button
                v-for="item in quickAddItems"
                :key="item.name"
                type="button"
                class="rounded-[18px] border border-[rgba(46,34,10,0.08)] bg-white px-4 py-3 text-left shadow-soft transition hover:border-[rgba(138,106,0,0.34)]"
                @click="openModal('optional', item)"
              >
                <div class="flex items-center gap-3">
                  <span class="grid h-11 w-11 place-items-center rounded-[14px] bg-brand-50">
                    <AppAsset type="service" :value="item.name" fallback="bot" :size="18" wrapper-class="inline-flex items-center justify-center" image-class="h-7 w-7 object-contain" icon-class="text-warning" />
                  </span>
                  <div>
                    <p class="text-sm font-bold text-neutral-900">{{ item.name }}</p>
                    <p class="text-xs text-neutral-500">{{ item.monthlyPrice.toLocaleString('ko-KR') }}원 · {{ item.category }}</p>
                  </div>
                </div>
              </button>
            </div>
          </article>
        </div>

        <aside class="grid gap-5 xl:sticky xl:top-6 xl:self-start">
          <article class="guide-card">
            <p class="text-sm font-semibold text-warning">입력 요약</p>
            <div class="mt-4 grid gap-3 text-sm">
              <div class="rounded-[20px] bg-white/84 px-4 py-3">
                <div class="flex items-center justify-between">
                  <span class="text-neutral-500">현재 총 비용</span>
                  <strong class="text-neutral-900">{{ totalPrice.toLocaleString('ko-KR') }}원</strong>
                </div>
                <div class="mt-2 flex items-center justify-between">
                  <span class="text-neutral-500">예산 차이</span>
                  <strong :class="budgetDiff >= 0 ? 'status-success' : 'status-danger'">{{ budgetDiff >= 0 ? '+' : '' }}{{ budgetDiff.toLocaleString('ko-KR') }}원</strong>
                </div>
              </div>

              <div class="rounded-[20px] bg-white/84 px-4 py-3">
                <p class="text-xs font-bold uppercase tracking-[0.12em] text-neutral-300">선택된 서비스</p>
                <div class="mt-3 grid gap-2">
                  <div v-for="item in form.subscriptionItems" :key="item.itemId" class="flex items-center justify-between gap-3 text-sm">
                    <span class="font-semibold text-neutral-900">{{ item.serviceName }}</span>
                    <span class="text-neutral-500">{{ Number(item.monthlyPrice || 0).toLocaleString('ko-KR') }}원</span>
                  </div>
                </div>
              </div>
            </div>

            <button type="button" class="primary-button mt-5 w-full" :disabled="aiStore.isSubmitting" @click="submitAndGenerate">{{ aiStore.isSubmitting ? '실행 중...' : '실행' }}</button>
            <button type="button" class="secondary-button mt-3 w-full" @click="resetForm">초기화</button>
          </article>
        </aside>
      </div>
    </section>


    <AppProgressDialog
      :open="progressState.open"
      :title="progressState.title"
      :description="progressState.description"
      :percent="progressState.percent"
      :steps="generationSteps"
      :current-step="progressState.currentStep"
    />

    <div v-if="modalOpen" class="fixed inset-0 z-50 grid place-items-center bg-[rgba(30,24,13,0.38)] px-4">
      <div class="w-full max-w-[440px] rounded-modal border border-[rgba(46,34,10,0.08)] bg-neutral-25 p-5 shadow-floating">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-sm font-semibold text-warning">구독 항목 {{ modalKind === 'mandatory' ? '추가' : '추가' }}</p>
            <h2 class="mt-2 text-[22px] font-bold tracking-[-0.03em] text-neutral-900">서비스 이름과 가격을 입력하세요</h2>
          </div>
          <button type="button" class="tertiary-button !min-h-[36px] !rounded-[12px] !px-3" @click="modalOpen = false">닫기</button>
        </div>

        <div class="mt-5 grid gap-4">
          <label class="grid gap-2">
            <span class="form-label">서비스 이름</span>
            <input v-model="itemForm.serviceName" type="text" class="form-input" placeholder="서비스 이름 입력" autocomplete="off" />
          </label>

          <label class="grid gap-2">
            <span class="form-label">카테고리</span>
            <select v-model="itemForm.category" class="form-input appearance-none">
              <option v-for="category in aiStore.CATEGORY_OPTIONS" :key="category" :value="category">{{ category }}</option>
            </select>
          </label>

          <label class="grid gap-2">
            <span class="form-label">가격 (선택)</span>
            <input v-model="itemForm.monthlyPrice" type="number" min="0" class="form-input" placeholder="가격 직접 입력" />
          </label>

          <label class="grid gap-2">
            <span class="form-label">설명</span>
            <textarea v-model="itemForm.description" rows="4" class="form-input min-h-[112px] resize-none py-4" placeholder="간단한 사용 목적을 적어주세요."></textarea>
          </label>
        </div>

        <div class="mt-5 flex flex-wrap justify-end gap-2">
          <button type="button" class="secondary-button !min-h-[44px] !rounded-[16px] !px-4" @click="modalOpen = false">취소</button>
          <button type="button" class="primary-button !min-h-[44px] !rounded-[16px] !px-4" @click="saveModal">저장</button>
        </div>

      </div>
    </div>
  </AppShell>
</template>
