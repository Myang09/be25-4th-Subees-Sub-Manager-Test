<script setup>
defineProps({
  compact: {
    type: Boolean,
    default: false,
  },
})

const footerLinks = [
  { label: '회원가입', to: '/signup' },
  { label: '로그인', to: '/login' },
  { label: '대시보드', to: { path: '/login', query: { redirect: '/dashboard' } } },
  { label: '결제 캘린더', to: { path: '/login', query: { redirect: '/calendar' } } },
]

const helpLinks = [
  { label: '서비스 안내', to: '/' },
  { label: '정책안내', to: '/' },
  { label: '고객지원', to: '/' },
]
</script>

<template>
  <footer class="border-t border-[rgba(46,34,10,0.08)] bg-brand-100">
    <div
      class="footer-shell"
      :class="compact ? 'footer-shell--compact' : ''"
    >
      <div class="footer-brand">
        <img
          src="/image/subees-logo.png"
          alt="Subees"
          class="footer-brand__logo"
          :class="compact ? 'footer-brand__logo--compact' : ''"
        />
        <div class="min-w-0">
          <p class="footer-brand__eyebrow">subscription utility</p>
          <div class="footer-brand__row">
            <p class="footer-brand__title">Subees</p>
            <p class="footer-brand__copy">구독·결제·캘린더를 한 곳에</p>
          </div>
        </div>
      </div>

      <div class="footer-links" :class="compact ? 'footer-links--compact' : ''">
        <div class="footer-links__row">
          <template v-for="(item, index) in footerLinks" :key="item.label">
            <RouterLink :to="item.to" class="transition hover:text-neutral-900">{{ item.label }}</RouterLink>
            <span v-if="index < footerLinks.length - 1">/</span>
          </template>
        </div>
        <div class="footer-links__row">
          <template v-for="(item, index) in helpLinks" :key="item.label">
            <RouterLink :to="item.to" class="transition hover:text-neutral-900">{{ item.label }}</RouterLink>
            <span v-if="index < helpLinks.length - 1">/</span>
          </template>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer-shell {
  width: 100%;
  padding: 18px clamp(24px, 3vw, 54px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.footer-shell--compact {
  padding-top: 12px;
  padding-bottom: 12px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.footer-brand__logo {
  width: 56px;
  height: 56px;
  object-fit: contain;
  border-radius: 999px;
  background: white;
  padding: 6px;
  box-shadow: 0 10px 24px rgba(33, 24, 8, 0.06);
}

.footer-brand__logo--compact {
  width: 44px;
  height: 44px;
}

.footer-brand__eyebrow {
  font-size: 11px;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #c7b895;
}

.footer-brand__row {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.footer-brand__title {
  font-size: 22px;
  font-weight: 900;
  color: #1e180d;
  letter-spacing: -0.04em;
}

.footer-brand__copy {
  font-size: 15px;
  color: #61563d;
}

.footer-links {
  text-align: right;
  font-size: 14px;
  line-height: 1.8;
  font-weight: 700;
  color: #61563d;
}

.footer-links--compact {
  font-size: 13px;
}

.footer-links__row {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
}

.footer-links__row + .footer-links__row {
  margin-top: 4px;
}

@media (max-width: 1023px) {
  .footer-shell {
    flex-direction: column;
    align-items: flex-start;
  }

  .footer-links,
  .footer-links__row {
    text-align: left;
    justify-content: flex-start;
  }
}
</style>
