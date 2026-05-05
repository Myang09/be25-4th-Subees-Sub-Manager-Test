<script setup>
const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '확인' },
  description: { type: String, default: '' },
  confirmText: { type: String, default: '확인' },
  cancelText: { type: String, default: '취소' },
  loading: { type: Boolean, default: false },
  tone: { type: String, default: 'default' },
  showCancel: { type: Boolean, default: true },
  showClose: { type: Boolean, default: true },
  persistent: { type: Boolean, default: false },
})

const emit = defineEmits(['cancel', 'confirm'])

const handleCancel = () => {
  if (props.loading || props.persistent) return
  emit('cancel')
}

const handleBackdrop = () => {
  if (props.loading || props.persistent) return
  emit('cancel')
}

const handleConfirm = () => {
  if (props.loading) return
  emit('confirm')
}
</script>

<template>
  <div
    v-if="open"
    class="fixed inset-0 z-[70] flex items-center justify-center bg-[rgba(28,22,11,0.44)] px-4 py-6 backdrop-blur-sm"
    @click.self="handleBackdrop"
  >
    <div class="w-full max-w-md rounded-modal border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,247,0.98)] p-6 shadow-floating">
      <div class="flex items-start justify-between gap-4">
        <div class="min-w-0 flex-1">
          <p class="text-sm font-semibold text-warning">확인 필요</p>
          <h2 class="mt-2 text-[24px] font-bold tracking-[-0.03em] text-neutral-900">{{ title }}</h2>
          <p v-if="description" class="mt-3 text-sm leading-6 text-neutral-500">{{ description }}</p>
        </div>
        <button
          v-if="showClose"
          type="button"
          class="tertiary-button !min-h-[38px] !rounded-[14px] !px-3"
          :disabled="loading || persistent"
          @click="handleCancel"
        >
          닫기
        </button>
      </div>

      <div class="mt-6 flex flex-wrap justify-end gap-2">
        <button
          v-if="showCancel"
          type="button"
          class="secondary-button !min-h-[44px] !rounded-[16px] !px-4"
          :disabled="loading || persistent"
          @click="handleCancel"
        >
          {{ cancelText }}
        </button>
        <button
          type="button"
          class="primary-button !min-h-[44px] !rounded-[16px] !px-4"
          :class="tone === 'danger' ? '!bg-danger !text-white !shadow-none hover:!bg-red-600 active:!bg-red-700' : ''"
          :disabled="loading"
          @click="handleConfirm"
        >
          {{ loading ? '처리 중...' : confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>
