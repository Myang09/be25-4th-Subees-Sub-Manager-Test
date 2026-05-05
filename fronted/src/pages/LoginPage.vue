<script setup>
import { computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import AuthShell from '@/components/layout/AuthShell.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({ email: '', password: '' })
const errors = reactive({ email: '', password: '', submit: '' })
const canSubmit = computed(() => form.email.trim() && form.password.trim())

const validate = () => {
  errors.email = form.email.trim() ? '' : '이메일을 입력해주세요.'
  errors.password = form.password.trim() ? '' : '비밀번호를 입력해주세요.'
  errors.submit = ''
  return !errors.email && !errors.password
}

const submit = async () => {
  if (!validate()) return

  try {
    await authStore.login({ email: form.email.trim(), password: form.password })
    router.push(router.currentRoute.value.query.redirect || '/dashboard')
  } catch (error) {
    errors.submit = error?.response?.data?.message || '로그인에 실패했습니다. 입력 내용을 확인해주세요.'
  }
}
</script>

<template>
  <AuthShell contentWidth="max-w-[1440px]" contentClass="h-full">
    <div class="grid h-full place-items-center">
      <section class="shell-card w-[min(100%,540px)] rounded-[28px] px-8 py-8 text-center xl:px-10 xl:py-9">
        <img src="/image/subees-logo.png" alt="Subees" class="mx-auto h-[88px] w-[88px] object-contain xl:h-[96px] xl:w-[96px]" />
        <h1 class="mt-5 text-[clamp(34px,2.5vw,44px)] font-black tracking-[-0.05em] text-neutral-900">로그인</h1>
        <p class="mt-2 text-sm text-neutral-500 xl:text-base">이메일과 비밀번호를 입력하면 대시보드로 이동합니다.</p>

        <div
          v-if="errors.submit"
          class="mt-4 rounded-[16px] border px-4 py-3 text-left text-sm font-medium text-danger"
          style="border-color: rgba(186, 107, 82, 0.28); background-color: rgba(255, 247, 244, 0.94)"
        >
          {{ errors.submit }}
        </div>

        <form class="mt-7 grid gap-4" @submit.prevent="submit">
          <div>
            <input
              v-model="form.email"
              type="email"
              placeholder="이메일을 입력해주세요."
              class="form-input auth-plain-input !min-h-[52px]"
              :class="errors.email ? 'border-danger focus:ring-0' : ''"
            />
            <p v-if="errors.email" class="mt-2 text-left text-xs font-medium text-danger">{{ errors.email }}</p>
          </div>

          <div>
            <input
              v-model="form.password"
              type="password"
              placeholder="비밀번호를 입력해주세요."
              class="form-input auth-plain-input !min-h-[52px]"
              :class="errors.password ? 'border-danger focus:ring-0' : ''"
            />
            <p v-if="errors.password" class="mt-2 text-left text-xs font-medium text-danger">{{ errors.password }}</p>
          </div>

          <button
            type="submit"
            class="primary-button mt-4 w-full !min-h-[54px] !rounded-full"
            :disabled="!canSubmit || authStore.isSubmitting"
            :class="!canSubmit || authStore.isSubmitting ? 'cursor-not-allowed opacity-60' : ''"
          >
            {{ authStore.isSubmitting ? '로그인 중...' : '로그인' }}
          </button>
        </form>

        <div class="mt-5 text-center text-sm font-medium text-neutral-400">
          계정이 없으신가요?
          <RouterLink to="/signup" class="ml-1 font-bold text-[#8A6A00] transition hover:text-neutral-900">회원가입</RouterLink>
        </div>
      </section>
    </div>
  </AuthShell>
</template>
