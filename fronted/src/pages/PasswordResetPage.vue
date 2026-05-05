<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthShell from '@/components/layout/AuthShell.vue'
import AuthField from '@/components/auth/AuthField.vue'
import { postResetPassword } from '@/api/auth'

const route = useRoute()
const router = useRouter()

const form = reactive({
  password: '',
  passwordConfirm: '',
})

const errors = reactive({
  password: '',
  passwordConfirm: '',
  submit: '',
})

const success = ref('')
const isSubmitting = ref(false)
const email = computed(() => route.query.email || '가입 이메일')

const validate = () => {
  errors.password = ''
  errors.passwordConfirm = ''
  errors.submit = ''

  if (!form.password) {
    errors.password = '새 비밀번호를 입력해주세요.'
  } else if (form.password.length < 8 || form.password.length > 20) {
    errors.password = '비밀번호는 8자 이상 20자 이하입니다.'
  }

  if (!form.passwordConfirm) {
    errors.passwordConfirm = '비밀번호를 다시 입력해주세요.'
  } else if (form.password !== form.passwordConfirm) {
    errors.passwordConfirm = '비밀번호가 일치하지 않습니다.'
  }

  return !errors.password && !errors.passwordConfirm
}

const canSubmit = computed(() => form.password && form.passwordConfirm)

const submit = async () => {
  success.value = ''
  if (!validate()) return

  isSubmitting.value = true
  try {
    await postResetPassword({
      email: email.value,
      password: form.password,
    })
    success.value = '비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인해 주세요.'
    setTimeout(() => router.push('/login'), 700)
  } catch (apiError) {
    errors.submit = apiError?.response?.data?.message || '비밀번호 재설정에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <AuthShell eyebrow="비밀번호 재설정" title="새 비밀번호 설정" description="인증이 완료된 계정의 비밀번호를 새로 저장합니다.">
    <div class="shell-card overflow-hidden p-6 lg:p-7">
      <div>
        <p class="text-[11px] font-extrabold uppercase tracking-[0.14em] text-neutral-300">NEW PASSWORD</p>
        <h2 class="mt-2 text-[28px] font-black tracking-[-0.04em] text-neutral-900">새 비밀번호를 입력하세요</h2>
        <p class="mt-2 text-sm leading-6 text-neutral-500">{{ email }} 계정으로 새 비밀번호를 저장합니다.</p>
      </div>

      <div v-if="errors.submit" class="mt-4 rounded-[18px] border border-danger/20 bg-red-50 px-4 py-3 text-sm font-medium text-danger">{{ errors.submit }}</div>
      <div v-if="success" class="mt-4 rounded-[18px] border border-success/20 bg-green-50 px-4 py-3 text-sm font-medium text-success">{{ success }}</div>

      <form class="mt-5 grid gap-4" @submit.prevent="submit">
        <div class="grid gap-4 lg:grid-cols-2">
          <AuthField v-model="form.password" label="비밀번호 (8자 이상)" type="password" placeholder="새 비밀번호를 입력해주세요." :error="errors.password" />
          <AuthField v-model="form.passwordConfirm" label="비밀번호 재확인" type="password" placeholder="비밀번호를 다시 입력해주세요." :error="errors.passwordConfirm" />
        </div>

        <button type="submit" class="primary-button !min-h-[52px] w-full" :disabled="!canSubmit || isSubmitting" :class="(!canSubmit || isSubmitting) ? 'cursor-not-allowed opacity-60' : ''">
          {{ isSubmitting ? '재설정 중...' : '재설정' }}
        </button>
      </form>

      <div class="mt-4 text-sm text-neutral-500">
        이전 단계로 돌아가기 · <RouterLink to="/password/reset/request" class="font-semibold text-brand-600">이메일 인증</RouterLink>
      </div>
    </div>
  </AuthShell>
</template>
