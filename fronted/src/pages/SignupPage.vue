<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AuthShell from '@/components/layout/AuthShell.vue'
import AuthField from '@/components/auth/AuthField.vue'
import TermsAgreementModal from '@/components/auth/TermsAgreementModal.vue'
import { getCheckEmail, getCheckNickname, postSignup } from '@/api/auth'

const router = useRouter()

const emptyCheck = () => ({ loading: false, available: null, message: '', checkedValue: '' })
const isEmail = (value) => /\S+@\S+\.\S+/.test(value)

const form = reactive({ email: '', nickname: '', password: '', passwordConfirm: '' })
const errors = reactive({ email: '', nickname: '', password: '', passwordConfirm: '', submit: '' })
const agreements = reactive({ terms: false, privacy: false })
const modalState = ref({ open: false, type: 'terms' })
const emailCheck = ref(emptyCheck())
const nicknameCheck = ref(emptyCheck())
const isSubmitting = ref(false)

const resetCheck = (target) => { target.value = emptyCheck() }
const setCheck = (target, available, message, checkedValue = '') => {
  target.value = { loading: false, available, message, checkedValue }
}

watch(() => form.email, (value) => {
  errors.submit = ''
  if (emailCheck.value.checkedValue !== value.trim()) resetCheck(emailCheck)
})

watch(() => form.nickname, (value) => {
  errors.submit = ''
  if (nicknameCheck.value.checkedValue !== value.trim()) resetCheck(nicknameCheck)
})

watch(() => [form.password, form.passwordConfirm], () => {
  errors.submit = ''
})

const canSubmit = computed(() => (
  form.email.trim()
  && form.nickname.trim()
  && form.password
  && form.passwordConfirm
  && agreements.terms
  && agreements.privacy
))

const openAgreement = (type) => {
  modalState.value = { open: true, type }
}

const closeAgreement = () => {
  modalState.value.open = false
}

const acceptAgreement = (type) => {
  agreements[type] = true
  closeAgreement()
}

const checkAvailability = async (type) => {
  const value = form[type].trim()
  const target = type === 'email' ? emailCheck : nicknameCheck

  errors[type] = ''
  resetCheck(target)

  if (!value) {
    errors[type] = `${type === 'email' ? '이메일' : '닉네임'}을 입력해주세요.`
    return
  }

  if (type === 'email' && !isEmail(value)) {
    errors.email = '올바른 이메일 형식이어야 합니다.'
    return
  }

  target.value.loading = true

  try {
    const response = type === 'email' ? await getCheckEmail(value) : await getCheckNickname(value)
    const data = response?.data?.data ?? {}
    setCheck(target, Boolean(data.available), data.message || (data.available ? '사용 가능한 값입니다.' : '이미 사용 중인 값입니다.'), value)
  } catch {
    setCheck(target, false, '중복 확인에 실패했습니다.')
  }
}

const validate = () => {
  errors.email = !form.email.trim()
    ? '이메일을 입력해주세요.'
    : !isEmail(form.email.trim())
      ? '올바른 이메일 형식이어야 합니다.'
      : ''
  errors.nickname = form.nickname.trim() ? '' : '닉네임을 입력해주세요.'
  errors.password = !form.password
    ? '비밀번호를 입력해주세요.'
    : form.password.length < 8 || form.password.length > 20
      ? '비밀번호는 8자 이상 20자 이하입니다.'
      : ''
  errors.passwordConfirm = !form.passwordConfirm
    ? '비밀번호를 다시 입력해주세요.'
    : form.password !== form.passwordConfirm
      ? '비밀번호가 일치하지 않습니다.'
      : ''
  errors.submit = agreements.terms && agreements.privacy ? '' : '필수 약관에 동의해주세요.'

  return !Object.values(errors).some(Boolean)
}

const submit = async () => {
  if (!validate()) return

  isSubmitting.value = true
  try {
    await postSignup({
      email: form.email.trim(),
      nickname: form.nickname.trim(),
      password: form.password,
    })
    window.localStorage.setItem('subees-last-email', form.email.trim())
    router.push('/login')
  } catch (error) {
    errors.submit = error?.response?.data?.message || '회원가입에 실패했습니다. 입력 내용을 다시 확인해주세요.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <AuthShell contentWidth="max-w-[1440px]" contentClass="h-full">
    <div class="grid h-full items-center gap-[clamp(28px,3vw,56px)] xl:grid-cols-[minmax(380px,0.95fr)_minmax(560px,1fr)]">
      <section class="flex items-center justify-center xl:justify-start">
        <div class="w-full max-w-[520px]">
          <p class="text-[clamp(30px,2.4vw,42px)] font-black leading-[1.2] tracking-[-0.05em] text-neutral-900">
            반가워요, 편한 구독관리 플랫폼<br />SUBEES입니다.
          </p>

          <div class="mt-8 grid min-h-[clamp(300px,42vh,420px)] place-items-center rounded-[32px] bg-white p-8 shadow-soft">
            <img src="/image/subees-logo.png" alt="Subees" class="h-[clamp(260px,30vw,360px)] w-[clamp(260px,30vw,360px)] object-contain" />
          </div>
        </div>
      </section>

      <section class="shell-card w-full max-w-[640px] justify-self-end rounded-[30px] p-6 xl:p-7">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="eyebrow-label">회원가입</p>
            <h1 class="mt-1 text-[clamp(30px,2vw,40px)] font-black tracking-[-0.05em] text-neutral-900">이메일 회원가입</h1>
          </div>
          <RouterLink to="/login" class="secondary-button !min-h-10 !rounded-full !px-4">로그인</RouterLink>
        </div>

        <div
          v-if="errors.submit"
          class="mt-3 rounded-[16px] border px-4 py-3 text-sm font-medium text-danger"
          style="border-color: rgba(186, 107, 82, 0.28); background-color: rgba(255, 247, 244, 0.94)"
        >
          {{ errors.submit }}
        </div>

        <form class="mt-4 grid gap-3" @submit.prevent="submit">
          <div class="grid gap-3 xl:grid-cols-[minmax(0,1fr)_118px] xl:items-start">
            <div>
              <AuthField
                v-model="form.email"
                label="이메일"
                type="email"
                placeholder="이메일을 입력해주세요."
                :error="errors.email"
                input-class="!min-h-[48px] !rounded-[18px]"
              />
              <p v-if="emailCheck.message && !errors.email" class="mt-1 text-xs" :class="emailCheck.available ? 'status-success' : 'status-danger'">
                {{ emailCheck.message }}
              </p>
            </div>
            <button type="button" class="secondary-button w-full !min-h-[48px] !rounded-[18px] xl:mt-[26px]" @click="checkAvailability('email')">
              {{ emailCheck.loading ? '확인 중' : '중복 확인' }}
            </button>
          </div>

          <div class="grid gap-3 xl:grid-cols-[minmax(0,1fr)_118px] xl:items-start">
            <div>
              <AuthField
                v-model="form.nickname"
                label="닉네임"
                placeholder="닉네임을 입력해주세요."
                :error="errors.nickname"
                input-class="!min-h-[48px] !rounded-[18px]"
              />
              <p v-if="nicknameCheck.message && !errors.nickname" class="mt-1 text-xs" :class="nicknameCheck.available ? 'status-success' : 'status-danger'">
                {{ nicknameCheck.message }}
              </p>
            </div>
            <button type="button" class="secondary-button w-full !min-h-[48px] !rounded-[18px] xl:mt-[26px]" @click="checkAvailability('nickname')">
              {{ nicknameCheck.loading ? '확인 중' : '중복 확인' }}
            </button>
          </div>

          <AuthField
            v-model="form.password"
            label="비밀번호"
            type="password"
            placeholder="비밀번호를 입력해주세요."
            :error="errors.password"
            hint="8자 이상 20자 이하로 입력해주세요."
            input-class="!min-h-[48px] !rounded-[18px]"
          />
          <AuthField
            v-model="form.passwordConfirm"
            label="비밀번호 확인"
            type="password"
            placeholder="비밀번호를 다시 입력해주세요."
            :error="errors.passwordConfirm"
            input-class="!min-h-[48px] !rounded-[18px]"
          />

          <section class="rounded-[22px] border border-[rgba(46,34,10,0.08)] bg-brand-50 px-4 py-4">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-sm font-bold text-neutral-900">필수 약관 동의</p>
                <p class="mt-0.5 text-xs text-neutral-500">회원가입에 필요한 약관만 확인합니다.</p>
              </div>
              <button
                type="button"
                class="chip-button !min-h-9 !px-3.5"
                :class="{ 'is-selected': agreements.terms && agreements.privacy }"
                @click="agreements.terms = !agreements.terms; agreements.privacy = agreements.terms"
              >
                전체 동의
              </button>
            </div>

            <div class="mt-3 grid gap-2.5">
              <label class="flex items-center justify-between gap-3 rounded-[16px] border border-[rgba(46,34,10,0.08)] bg-white px-4 py-3">
                <span class="flex items-center gap-3 text-sm font-semibold text-neutral-900">
                  <input v-model="agreements.terms" type="checkbox" class="h-4 w-4 rounded border-neutral-300 text-brand-500 focus:ring-brand-300" />
                  이용약관 동의
                </span>
                <button type="button" class="tertiary-button !min-h-[34px] !rounded-[12px] !px-3" @click="openAgreement('terms')">전문 보기</button>
              </label>

              <label class="flex items-center justify-between gap-3 rounded-[16px] border border-[rgba(46,34,10,0.08)] bg-white px-4 py-3">
                <span class="flex items-center gap-3 text-sm font-semibold text-neutral-900">
                  <input v-model="agreements.privacy" type="checkbox" class="h-4 w-4 rounded border-neutral-300 text-brand-500 focus:ring-brand-300" />
                  개인정보 수집 및 이용 동의
                </span>
                <button type="button" class="tertiary-button !min-h-[34px] !rounded-[12px] !px-3" @click="openAgreement('privacy')">전문 보기</button>
              </label>
            </div>
          </section>

          <button
            type="submit"
            class="primary-button mt-1 w-full !min-h-[52px] !rounded-full"
            :disabled="!canSubmit || isSubmitting"
            :class="!canSubmit || isSubmitting ? 'cursor-not-allowed opacity-60' : ''"
          >
            {{ isSubmitting ? '회원가입 처리 중...' : '회원가입' }}
          </button>
        </form>
      </section>
    </div>

    <TermsAgreementModal :open="modalState.open" :type="modalState.type" @close="closeAgreement" @accept="acceptAgreement" />
  </AuthShell>
</template>
