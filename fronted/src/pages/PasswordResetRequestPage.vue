<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthShell from '@/components/layout/AuthShell.vue'
import AuthField from '@/components/auth/AuthField.vue'
import { getMockAuthGuide, postPasswordResetCode, postVerifyPasswordResetCode } from '@/api/auth'

const router = useRouter()
const guide = getMockAuthGuide()

const form = reactive({
  email: '',
  verificationCode: '',
})

const sent = ref(false)
const isSubmitting = ref(false)
const error = ref('')
const notice = ref('')

const canContinue = computed(() => form.email.trim() && form.verificationCode.trim())

const sendCode = async () => {
  error.value = ''
  notice.value = ''

  if (!form.email.trim()) {
    error.value = '이메일을 먼저 입력해주세요.'
    return
  }

  try {
    const response = await postPasswordResetCode(form.email.trim())
    sent.value = true
    notice.value = `${response?.data?.data?.message || '인증번호를 준비했습니다.'} 체험 코드는 ${guide.resetCode} 입니다.`
  } catch (apiError) {
    error.value = apiError?.response?.data?.message || '인증번호 발송에 실패했습니다.'
  }
}

const submit = async () => {
  error.value = ''
  if (!canContinue.value) {
    error.value = '이메일과 인증번호를 모두 입력해주세요.'
    return
  }

  isSubmitting.value = true
  try {
    await postVerifyPasswordResetCode({
      email: form.email.trim(),
      verificationCode: form.verificationCode.trim(),
    })
    router.push({ path: '/password/reset/change', query: { email: form.email.trim() } })
  } catch (apiError) {
    error.value = apiError?.response?.data?.message || '인증번호 확인에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <AuthShell eyebrow="비밀번호 찾기" title="이메일 인증" description="가입된 이메일을 확인한 뒤 인증번호를 입력해 주세요.">
    <div class="shell-card overflow-hidden p-6 lg:p-7">
      <div>
        <p class="text-[11px] font-extrabold uppercase tracking-[0.14em] text-neutral-300">RESET PASSWORD</p>
        <h2 class="mt-2 text-[28px] font-black tracking-[-0.04em] text-neutral-900">비밀번호 재설정</h2>
        <p class="mt-2 text-sm leading-6 text-neutral-500">가입된 이메일 확인 후 인증이 완료되면 새 비밀번호를 설정할 수 있습니다.</p>
      </div>

      <div v-if="error" class="mt-4 rounded-[18px] border border-danger/20 bg-red-50 px-4 py-3 text-sm font-medium text-danger">{{ error }}</div>
      <div v-if="notice" class="mt-4 rounded-[18px] border border-success/20 bg-green-50 px-4 py-3 text-sm font-medium text-success">{{ notice }}</div>

      <form class="mt-5 grid gap-4" @submit.prevent="submit">
        <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_132px] lg:items-end">
          <AuthField v-model="form.email" label="SUBEES ID" type="email" placeholder="가입된 이메일을 입력해주세요." />
          <button type="button" class="secondary-button !min-h-[50px] w-full" @click="sendCode">발송</button>
        </div>

        <AuthField v-model="form.verificationCode" label="인증번호" placeholder="인증번호를 입력해주세요." :hint="sent ? '수신한 인증번호를 입력해주세요.' : '인증번호 발송 후 입력할 수 있습니다.'" />

        <button type="submit" class="primary-button !min-h-[52px] w-full" :disabled="!canContinue || isSubmitting" :class="(!canContinue || isSubmitting) ? 'cursor-not-allowed opacity-60' : ''">
          {{ isSubmitting ? '확인 중...' : '다음' }}
        </button>
      </form>

      <div class="mt-4 text-sm text-neutral-500">
        계정이 기억났다면 <RouterLink to="/login" class="font-semibold text-brand-600">로그인</RouterLink>
      </div>
    </div>
  </AuthShell>
</template>
