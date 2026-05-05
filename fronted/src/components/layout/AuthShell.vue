<script setup>
import BrandLockup from '@/components/common/BrandLockup.vue'
import PublicFooter from '@/components/layout/PublicFooter.vue'

defineProps({
  contentWidth: {
    type: String,
    default: 'max-w-[1280px]',
  },
  contentClass: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <div class="flex min-h-screen flex-col bg-[rgba(255,253,247,0.96)] text-neutral-900">
    <header class="shrink-0 border-b border-[rgba(46,34,10,0.08)] bg-brand-100/95 backdrop-blur">
      <div class="mx-auto flex w-full max-w-[1440px] items-center justify-between gap-6 px-[clamp(24px,3vw,48px)] py-3">
        <RouterLink to="/" class="inline-flex items-center no-underline">
          <BrandLockup
            eyebrow="subscription utility"
            title="Subees"
            subtitle="구독·결제·캘린더를 한 곳에 정리해요"
            :size="44"
            :framed="false"
          />
        </RouterLink>

        <nav class="flex shrink-0 items-center gap-3">
          <RouterLink to="/login" class="secondary-button !min-h-10 !rounded-full !px-5 !text-sm">로그인</RouterLink>
          <RouterLink to="/signup" class="primary-button !min-h-10 !rounded-full !px-5 !text-sm !shadow-none">회원가입</RouterLink>
        </nav>
      </div>
    </header>

    <main class="flex min-h-0 flex-1 items-stretch overflow-hidden px-[clamp(24px,3vw,48px)] py-[clamp(14px,2vw,24px)]">
      <div class="mx-auto h-full w-full" :class="[contentWidth, contentClass]">
        <slot />
      </div>
    </main>

    <PublicFooter compact class="shrink-0" />
  </div>
</template>
