<script setup>
const props = defineProps({
  size: { type: Number, default: 58 },
  eyebrow: { type: String, default: 'Subscription Utility' },
  title: { type: String, default: 'Subees' },
  subtitle: { type: String, default: '구독·결제·캘린더를 한 곳에 정리해요' },
  dark: { type: Boolean, default: false },
  framed: { type: Boolean, default: true },
})
</script>

<template>
  <div class="flex min-w-0 items-center gap-4">
    <div
      class="grid shrink-0 place-items-center overflow-hidden"
      :class="[
        framed
          ? dark
            ? 'rounded-[22px] border border-white/15 bg-white/10 shadow-soft backdrop-blur'
            : 'rounded-[22px] border border-[rgba(46,34,10,0.08)] bg-[linear-gradient(180deg,rgba(255,253,249,0.98),rgba(247,241,227,0.95))] shadow-soft'
          : '',
      ]"
      :style="{ width: `${size}px`, height: `${size}px`, padding: framed ? '8px' : '0px' }"
    >
      <img src="/image/subees-logo.png" alt="Subees" class="h-full w-full object-contain" />
    </div>

    <div class="min-w-0">
      <p :class="dark ? 'text-[11px] font-extrabold uppercase tracking-[0.12em] text-white/55' : 'text-[11px] font-extrabold uppercase tracking-[0.12em] text-neutral-300'">
        {{ eyebrow }}
      </p>
      <p :class="dark ? 'mt-1 text-[20px] font-black tracking-[-0.04em] text-white' : 'mt-1 text-[20px] font-black tracking-[-0.04em] text-neutral-900'">
        {{ title }}
      </p>
      <p v-if="subtitle" :class="dark ? 'mt-1 text-sm text-white/68' : 'mt-1 text-sm text-neutral-500'">
        {{ subtitle }}
      </p>
    </div>
  </div>
</template>
