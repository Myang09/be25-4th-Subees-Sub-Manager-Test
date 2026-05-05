<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import AppShell from '@/components/layout/AppShell.vue'
import AppConfirmDialog from '@/components/ui/AppConfirmDialog.vue'
import { useMyPageStore } from '@/stores/myPage'
import { getCheckNickname } from '@/api/auth'

const router = useRouter()
const myPageStore = useMyPageStore()
const fileInput = ref(null)

const saveNotificationPreference = async () => {
  await myPageStore.savePreferenceSettings()

  const hasAnyNotificationEnabled =
    preferences.value.messageNotificationEnabled ||
    preferences.value.paymentReminderEnabled

  localStorage.setItem('notificationAllEnabled', String(hasAnyNotificationEnabled))
  localStorage.setItem('notificationD3Enabled', String(preferences.value.messageNotificationEnabled))
  localStorage.setItem('notificationD0Enabled', String(preferences.value.paymentReminderEnabled))
}



const {
  profile,
  profileImageUrl,
  preferences,
  successMessage,
  errorMessage,
  isProfileSubmitting,
  isPasswordSubmitting,
  isImageSubmitting,
  isWithdrawing,
  isLoggedIn,
  joinedAtLabel,
  verifiedAtLabel,
  isEmailVerified,
} = storeToRefs(myPageStore)

const profileForm = reactive({
  nickname: '',
})

const nicknameCheck = ref({ loading: false, available: null, message: '', checkedValue: '' })

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  newPasswordConfirm: '',
})

const passwordErrors = reactive({
  currentPassword: '',
  newPassword: '',
  newPasswordConfirm: '',
  general: '',
})

const showWithdrawDialog = ref(false)

const profileInitial = computed(() => {
  const nickname = profile.value?.nickname || 'S'
  return nickname.slice(0, 1).toUpperCase()
})

const pageDescription = computed(() => {
  if (!isLoggedIn.value) {
    return '닉네임, 비밀번호, 프로필 이미지, 알림 설정을 한 화면에서 관리할 수 있습니다.'
  }
  return '사용자 정보, 결제 관련 알림, 보안 설정을 한 화면에서 빠르게 관리할 수 있습니다.'
})

const profileTitle = computed(() => `${profile.value?.nickname || '사용자'}님`)
const accountStatusLabel = computed(() => (isLoggedIn.value ? '활성 계정' : '로그인 필요'))

const allNotificationEnabled = computed({
  get: () => Boolean(
    preferences.value.emailNotificationEnabled
      && preferences.value.messageNotificationEnabled
      && preferences.value.paymentReminderEnabled,
  ),
  set: (value) => {
    preferences.value.emailNotificationEnabled = value
    preferences.value.messageNotificationEnabled = value
    preferences.value.paymentReminderEnabled = value
  },
})

const syncForms = () => {
  profileForm.nickname = profile.value?.nickname || ''
  nicknameCheck.value = { loading: false, available: null, message: '', checkedValue: '' }
}

watch(() => profileForm.nickname, (value) => {
  if (nicknameCheck.value.checkedValue !== value.trim()) {
    nicknameCheck.value = { loading: false, available: null, message: '', checkedValue: '' }
  }
})

const clearPasswordErrors = () => {
  passwordErrors.currentPassword = ''
  passwordErrors.newPassword = ''
  passwordErrors.newPasswordConfirm = ''
  passwordErrors.general = ''
}

const mapPasswordErrorMessage = (message) => {
  const mapped = {
    currentPassword: '',
    newPassword: '',
    newPasswordConfirm: '',
    general: '',
  }

  if (!message) return mapped

  const fieldMatches = [...String(message).matchAll(/([a-zA-Z]+)\(([^()]+)\)/g)]
  if (fieldMatches.length) {
    fieldMatches.forEach(([, field, fieldMessage]) => {
      if (field === 'currentPassword') mapped.currentPassword = fieldMessage
      if (field === 'newPassword') mapped.newPassword = fieldMessage
      if (field === 'newPasswordConfirm') mapped.newPasswordConfirm = fieldMessage
    })
    return mapped
  }

  if (String(message).includes('현재 비밀번호')) {
    mapped.currentPassword = message
    return mapped
  }

  if (String(message).includes('새 비밀번호')) {
    mapped.newPassword = message
    return mapped
  }

  mapped.general = message
  return mapped
}

watch(() => passwordForm.currentPassword, () => {
  passwordErrors.currentPassword = ''
  passwordErrors.general = ''
})

watch(() => passwordForm.newPassword, () => {
  passwordErrors.newPassword = ''
  passwordErrors.general = ''
})

watch(() => passwordForm.newPasswordConfirm, () => {
  passwordErrors.newPasswordConfirm = ''
  passwordErrors.general = ''
})

onMounted(async () => {
  await myPageStore.hydrate()
  syncForms()
})

const triggerFileInput = () => {
  fileInput.value?.click()
}

const onSelectImage = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  const success = await myPageStore.uploadProfileImage(file)
  if (success) {
    event.target.value = ''
  }
}

const checkNickname = async () => {
  const nextNickname = profileForm.nickname.trim()
  if (!nextNickname) {
    myPageStore.setError('닉네임을 입력해주세요.')
    return
  }

  if (nextNickname === String(profile.value?.nickname || '').trim()) {
    nicknameCheck.value = {
      loading: false,
      available: true,
      message: '현재 사용 중인 닉네임입니다.',
      checkedValue: nextNickname,
    }
    return
  }

  nicknameCheck.value = { loading: true, available: null, message: '', checkedValue: '' }
  try {
    const response = await getCheckNickname(nextNickname)
    const data = response?.data?.data ?? {}
    nicknameCheck.value = {
      loading: false,
      available: Boolean(data.available),
      message: data.message || (data.available ? '사용 가능한 닉네임입니다.' : '이미 사용 중인 닉네임입니다.'),
      checkedValue: nextNickname,
    }
  } catch (error) {
    nicknameCheck.value = {
      loading: false,
      available: false,
      message: '닉네임 확인에 실패했습니다.',
      checkedValue: '',
    }
  }
}

const handleProfileSubmit = async () => {
  const nextNickname = profileForm.nickname.trim()
  if (!nextNickname) {
    myPageStore.setError('닉네임을 입력해주세요.')
    return
  }

  const success = await myPageStore.updateProfile({ nickname: nextNickname })
  if (success) {
    syncForms()
  }
}

const handlePasswordSubmit = async () => {
  myPageStore.clearStatus()
  clearPasswordErrors()

  if (!passwordForm.currentPassword) {
    passwordErrors.currentPassword = '현재 비밀번호를 입력해주세요.'
  }

  if (!passwordForm.newPassword) {
    passwordErrors.newPassword = '새 비밀번호를 입력해주세요.'
  } else if (passwordForm.newPassword.length < 8 || passwordForm.newPassword.length > 20) {
    passwordErrors.newPassword = '새 비밀번호는 8자 이상 20자 이하로 입력해주세요.'
  }

  if (!passwordForm.newPasswordConfirm) {
    passwordErrors.newPasswordConfirm = '새 비밀번호를 한 번 더 입력해주세요.'
  } else if (passwordForm.newPassword !== passwordForm.newPasswordConfirm) {
    passwordErrors.newPasswordConfirm = '새 비밀번호가 서로 일치하지 않습니다.'
  }

  if (passwordErrors.currentPassword || passwordErrors.newPassword || passwordErrors.newPasswordConfirm) {
    return
  }

  const success = await myPageStore.changePassword({
    currentPassword: passwordForm.currentPassword,
    newPassword: passwordForm.newPassword,
  })

  if (success) {
    clearPasswordErrors()
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.newPasswordConfirm = ''
    return
  }

  const mappedErrors = mapPasswordErrorMessage(errorMessage.value)
  passwordErrors.currentPassword = mappedErrors.currentPassword
  passwordErrors.newPassword = mappedErrors.newPassword
  passwordErrors.newPasswordConfirm = mappedErrors.newPasswordConfirm
  passwordErrors.general = mappedErrors.general

  if (passwordErrors.currentPassword || passwordErrors.newPassword || passwordErrors.newPasswordConfirm || passwordErrors.general) {
    myPageStore.clearStatus()
  }
}

const openWithdrawDialog = () => {
  showWithdrawDialog.value = true
}

const closeWithdrawDialog = () => {
  if (isWithdrawing.value) return
  showWithdrawDialog.value = false
}

const handleWithdraw = async () => {
  const success = await myPageStore.withdraw()
  if (!success) return

  showWithdrawDialog.value = false
  router.push('/login')
}
</script>

<template>
  <AppShell title="hidden">
    <div class="grid gap-6">
      <section class="shell-card overflow-hidden p-0">
        <div class="grid gap-6 px-6 py-6 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-center lg:px-8">
          <div>
            <div class="flex flex-wrap items-center gap-3">
              <span class="rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-600 ring-1 ring-brand-100">마이페이지</span>
              <span class="text-sm text-neutral-500">프로필 · 보안 · 알림 설정</span>
            </div>
            <h1 class="mt-4 text-[28px] font-bold tracking-[-0.03em] text-neutral-900">{{ profileTitle }} 계정을 한 화면에서 관리하세요</h1>
            <p class="mt-3 max-w-3xl text-sm leading-7 text-neutral-500">{{ pageDescription }}</p>
          </div>

          <div class="flex flex-wrap gap-3 lg:justify-end">
            <RouterLink to="/payment-cards" class="secondary-button">결제 카드</RouterLink>
            <button type="button" class="secondary-button" @click="myPageStore.hydrate">새로고침</button>
            <RouterLink v-if="!isLoggedIn" to="/login" class="primary-button">로그인</RouterLink>
            <button
              v-else
              type="button"
              class="inline-flex min-h-12 items-center justify-center rounded-[14px] bg-rose-600 px-5 text-sm font-semibold text-white transition hover:bg-rose-700"
              :disabled="isWithdrawing"
              @click="openWithdrawDialog"
            >
              {{ isWithdrawing ? '처리 중...' : '회원탈퇴' }}
            </button>
          </div>
        </div>
      </section>

      <section
        v-if="successMessage || errorMessage"
        class="rounded-card border px-5 py-4 text-sm font-medium"
        :class="successMessage ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-rose-200 bg-rose-50 text-rose-700'"
      >
        {{ successMessage || errorMessage }}
      </section>

      <section class="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
        <aside class="grid gap-6 xl:sticky xl:top-6 xl:self-start">
          <article class="shell-card overflow-hidden p-0">
            <div class="bg-brand-50 p-6">
              <div class="flex flex-col items-center text-center">
                <div class="relative">
                  <img
                    v-if="profileImageUrl"
                    :src="profileImageUrl"
                    alt="프로필 이미지"
                    class="h-32 w-32 rounded-[28px] border border-brand-100 object-cover shadow-soft"
                  />
                  <div
                    v-else
                    class="flex h-32 w-32 items-center justify-center rounded-[28px] bg-white text-3xl font-bold text-brand-600 ring-1 ring-brand-100 shadow-soft"
                  >
                    {{ profileInitial }}
                  </div>
                </div>

                <h2 class="mt-5 text-2xl font-bold tracking-[-0.03em] text-neutral-900">{{ profile.nickname || '사용자' }}</h2>
                <p class="mt-2 text-sm text-neutral-500">{{ profile.email || '로그인 후 계정 정보를 확인할 수 있습니다.' }}</p>

                <div class="mt-6 grid w-full gap-3 sm:grid-cols-2 xl:grid-cols-1">
                  <button type="button" class="secondary-button" :disabled="!isLoggedIn || isImageSubmitting" @click="triggerFileInput">이미지 업로드</button>
                  <button type="button" class="secondary-button" :disabled="!isLoggedIn || isImageSubmitting" @click="myPageStore.resetProfileImage">기본 이미지</button>
                  <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="onSelectImage" />
                </div>
              </div>
            </div>

            <div class="grid gap-3 p-6">
              <div class="rounded-card border border-neutral-200 bg-white px-4 py-4">
                <p class="text-xs font-semibold uppercase tracking-[0.08em] text-neutral-500">계정 상태</p>
                <p class="mt-2 text-sm font-semibold text-neutral-900">{{ accountStatusLabel }}</p>
              </div>
              <div class="rounded-card border border-neutral-200 bg-white px-4 py-4">
                <p class="text-xs font-semibold uppercase tracking-[0.08em] text-neutral-500">이메일 인증</p>
                <p class="mt-2 text-sm font-semibold text-neutral-900">{{ isEmailVerified ? '인증 완료' : '인증 필요' }}</p>
                <p class="mt-1 text-xs text-neutral-500">{{ verifiedAtLabel }}</p>
              </div>
              <div class="rounded-card border border-neutral-200 bg-white px-4 py-4">
                <p class="text-xs font-semibold uppercase tracking-[0.08em] text-neutral-500">가입일</p>
                <p class="mt-2 text-sm font-semibold text-neutral-900">{{ joinedAtLabel }}</p>
              </div>
            </div>
          </article>
        </aside>

        <div class="grid gap-6">
          <section class="shell-card overflow-hidden p-0">
            <div class="bg-brand-50 px-6 py-6 lg:px-8">
              <div class="flex flex-col gap-3 border-b border-brand-100 pb-5 lg:flex-row lg:items-end lg:justify-between">
                <div>
                  <h2 class="text-[28px] font-bold tracking-[-0.03em] text-neutral-900">{{ profileTitle }}</h2>
                  <p class="mt-2 text-sm font-semibold text-brand-600">프로필 정보 관리</p>
                </div>
                <p class="max-w-xl text-sm leading-6 text-neutral-500">현재 로그인된 이메일은 수정할 수 없고, 닉네임만 변경할 수 있습니다.</p>
              </div>

              <div class="mt-6 grid gap-4">
                <label class="grid gap-2">
                  <span class="form-label">로그인된 이메일</span>
                  <input :value="profile.email" type="email" class="form-input bg-neutral-50" readonly />
                </label>

                <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_132px] lg:items-end">
                  <div>
                    <label class="grid gap-2">
                      <span class="form-label">닉네임</span>
                      <input v-model="profileForm.nickname" type="text" class="form-input bg-white" placeholder="변경할 닉네임을 입력해주세요." :disabled="!isLoggedIn" />
                    </label>
                    <p v-if="nicknameCheck.message" class="mt-2 text-xs" :class="nicknameCheck.available ? 'text-emerald-600' : 'text-rose-500'">{{ nicknameCheck.message }}</p>
                  </div>
                  <button type="button" class="secondary-button !min-h-[50px] w-full" :disabled="!isLoggedIn" @click="checkNickname">{{ nicknameCheck.loading ? '확인 중...' : '중복 확인' }}</button>
                </div>
              </div>

              <div class="mt-6 flex flex-wrap justify-end gap-3">
                <button type="button" class="secondary-button" @click="syncForms">변경 취소</button>
                <button type="button" class="primary-button" :disabled="!isLoggedIn || isProfileSubmitting" @click="handleProfileSubmit">
                  {{ isProfileSubmitting ? '저장 중...' : '프로필 저장' }}
                </button>
              </div>
            </div>
          </section>

          <section class="grid gap-6 lg:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]">
            <article class="shell-card overflow-hidden p-0">
              <div class="bg-brand-50 px-6 py-6">
                <div class="border-b border-brand-100 pb-5">
                  <p class="text-sm font-semibold text-brand-600">알림 설정</p>
                  <h2 class="mt-2 text-xl font-bold tracking-[-0.02em] text-neutral-900">필요한 알림만 간단하게 관리하세요</h2>
                </div>

                <div class="mt-6 grid gap-3">
                  <div class="rounded-card border border-neutral-200 bg-white px-4 py-4">
                    <div class="flex items-center justify-between gap-4">
                      <div>
                        <p class="text-sm font-semibold text-neutral-900">전체 알림받기</p>
                        <p class="mt-1 text-xs leading-5 text-neutral-500">결제 3일 전 알림과 결제일 당일 알림을 한 번에 켜고 끕니다.</p>
                      </div>
                      <button type="button" class="relative inline-flex h-8 w-14 items-center rounded-full transition" :class="allNotificationEnabled ? 'bg-brand-500' : 'bg-neutral-300'" @click="allNotificationEnabled = !allNotificationEnabled">
                        <span class="inline-block h-6 w-6 rounded-full bg-white shadow transition" :class="allNotificationEnabled ? 'translate-x-7' : 'translate-x-1'"></span>
                      </button>
                    </div>
                  </div>

                  <div class="rounded-card border border-neutral-200 bg-white px-4 py-4">
                    <div class="flex items-center justify-between gap-4">
                      <div>
                        <p class="text-sm font-semibold text-neutral-900">결제 3일전 알림</p>
                        <p class="mt-1 text-xs leading-5 text-neutral-500">다가오는 결제일을 미리 확인할 수 있도록 알려드립니다.</p>
                      </div>
                      <button type="button" class="relative inline-flex h-8 w-14 items-center rounded-full transition" :class="preferences.messageNotificationEnabled ? 'bg-brand-500' : 'bg-neutral-300'" @click="preferences.messageNotificationEnabled = !preferences.messageNotificationEnabled">
                        <span class="inline-block h-6 w-6 rounded-full bg-white shadow transition" :class="preferences.messageNotificationEnabled ? 'translate-x-7' : 'translate-x-1'"></span>
                      </button>
                    </div>
                  </div>

                  <div class="rounded-card border border-neutral-200 bg-white px-4 py-4">
                    <div class="flex items-center justify-between gap-4">
                      <div>
                        <p class="text-sm font-semibold text-neutral-900">결제일 당일 알림</p>
                        <p class="mt-1 text-xs leading-5 text-neutral-500">결제일이 된 구독 항목을 바로 확인할 수 있도록 알려드립니다.</p>
                      </div>
                      <button type="button" class="relative inline-flex h-8 w-14 items-center rounded-full transition" :class="preferences.paymentReminderEnabled ? 'bg-brand-500' : 'bg-neutral-300'" @click="preferences.paymentReminderEnabled = !preferences.paymentReminderEnabled">
                        <span class="inline-block h-6 w-6 rounded-full bg-white shadow transition" :class="preferences.paymentReminderEnabled ? 'translate-x-7' : 'translate-x-1'"></span>
                      </button>
                    </div>
                  </div>
                </div>

                <div class="mt-6 flex justify-end">
                <button type="button" class="primary-button" @click="saveNotificationPreference">알림 설정 저장</button>
                </div>
              </div>
            </article>

            <article class="shell-card overflow-hidden p-0">
              <div class="bg-brand-50 px-6 py-6">
                <div class="border-b border-brand-100 pb-5">
                  <p class="text-sm font-semibold text-brand-600">비밀번호 변경</p>
                  <h2 class="mt-2 text-xl font-bold tracking-[-0.02em] text-neutral-900">보안 정보를 안전하게 수정하세요</h2>
                  <p class="mt-2 text-sm leading-6 text-neutral-500">현재 비밀번호를 입력한 뒤 8자 이상 20자 이하의 새 비밀번호로 변경할 수 있습니다.</p>
                </div>

                <div class="mt-6 grid gap-4">
                  <label class="grid gap-2">
                    <span class="form-label">현재 비밀번호</span>
                    <input v-model="passwordForm.currentPassword" type="password" class="form-input bg-white" placeholder="현재 비밀번호" :disabled="!isLoggedIn" />
                    <p v-if="passwordErrors.currentPassword" class="text-xs font-medium text-rose-500">{{ passwordErrors.currentPassword }}</p>
                  </label>
                  <label class="grid gap-2">
                    <span class="form-label">새 비밀번호</span>
                    <input v-model="passwordForm.newPassword" type="password" class="form-input bg-white" placeholder="8자 이상 20자 이하" :disabled="!isLoggedIn" />
                    <p v-if="passwordErrors.newPassword" class="text-xs font-medium text-rose-500">{{ passwordErrors.newPassword }}</p>
                  </label>
                  <label class="grid gap-2">
                    <span class="form-label">비밀번호 재확인</span>
                    <input v-model="passwordForm.newPasswordConfirm" type="password" class="form-input bg-white" placeholder="새 비밀번호를 다시 입력해주세요." :disabled="!isLoggedIn" />
                    <p v-if="passwordErrors.newPasswordConfirm" class="text-xs font-medium text-rose-500">{{ passwordErrors.newPasswordConfirm }}</p>
                  </label>
                  <p v-if="passwordErrors.general" class="text-xs font-medium text-rose-500">{{ passwordErrors.general }}</p>
                </div>

                <div class="mt-6 flex justify-end">
                  <button type="button" class="primary-button" :disabled="!isLoggedIn || isPasswordSubmitting" @click="handlePasswordSubmit">
                    {{ isPasswordSubmitting ? '변경 중...' : '비밀번호 변경' }}
                  </button>
                </div>
              </div>
            </article>
          </section>
        </div>
      </section>
    </div>
    <AppConfirmDialog
      :open="showWithdrawDialog"
      title="회원탈퇴"
      description="정말로 회원탈퇴를 진행하시겠습니까? 탈퇴 후에는 계정을 복구할 수 없습니다."
      confirm-text="회원탈퇴"
      cancel-text="취소"
      tone="danger"
      :loading="isWithdrawing"
      @cancel="closeWithdrawDialog"
      @confirm="handleWithdraw"
    />
  </AppShell>
</template>
