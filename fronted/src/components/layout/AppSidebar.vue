<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppIcon from '@/components/ui/AppIcon.vue'

const route = useRoute()

const sections = computed(() => [
  {
    title: 'Subscription Manager Core',
    items: [
      { label: '대시보드', to: '/dashboard', desc: '이번 달 결제와 핵심 요약 확인', icon: 'home' },
      { label: '구독목록', to: '/subscriptions', desc: '등록 항목과 상세 정보 확인', icon: 'list' },
      { label: '구독추가', to: '/subscriptions/new', desc: '카테고리부터 결제 정보까지 등록', icon: 'plus' },
      { label: '결제 캘린더', to: '/calendar', desc: '날짜별 일정과 월별 소비 흐름 확인', icon: 'calendar' },
      { label: '커뮤니티', to: '/community', desc: '사용 경험과 팁 공유', icon: 'community' },
      { label: 'AI 추천', to: '/ai-recommendations', desc: 'AI 구독 조합 추천과 저장 기록 관리', icon: 'bot' },
    ],
  },
  {
    title: 'More',
    items: [
      { label: '알림 센터', to: '/notifications', desc: '결제와 인사이트 알림 확인', icon: 'bell' },
      { label: '결제 카드', to: '/payment-cards', desc: '카드 등록·수정·삭제 관리', icon: 'creditcard' },
      { label: 'FAQ', to: '/faq', desc: '자주 묻는 질문 빠르게 확인', icon: 'faq' },
      { label: '마이페이지', to: '/mypage', desc: '프로필과 환경설정 관리', icon: 'user' },
    ],
  },
])

const isActive = (to) => {
  const currentPath = route.path

  if (to === '/subscriptions') {
    return currentPath === '/subscriptions' || /^\/subscriptions\/(?!new(?:\/|$))/.test(currentPath)
  }

  return currentPath === to || currentPath.startsWith(`${to}/`)
}
</script>

<template>
  <aside class="side hidden lg:flex">
    <nav class="side__nav utility-panel">
      <RouterLink to="/" class="side__intro" aria-label="홈으로 이동">
        <div class="side__intro-head">
          <div class="side__intro-logo-wrap">
            <img src="/image/subees-logo.png" alt="Subees 홈" class="side__intro-logo" />
          </div>
          <div class="side__intro-copy">
            <p class="side__intro-eyebrow">Subscription Utility</p>
            <strong class="side__intro-title">Subees</strong>
          </div>
        </div>
      </RouterLink>

      <template v-for="section in sections" :key="section.title">
        <div class="side__group-label">{{ section.title }}</div>
        <RouterLink
          v-for="item in section.items"
          :key="item.to"
          :to="item.to"
          class="side__item"
          :class="{ 'side__item--active': isActive(item.to) }"
        >
          <span class="side__icon" :class="{ 'side__icon--active': isActive(item.to) }">
            <AppIcon :name="item.icon" :size="18" />
          </span>
          <span class="side__item-copy">
            <span class="side__item-label">{{ item.label }}</span>
            <span class="side__item-description">{{ item.desc }}</span>
          </span>
        </RouterLink>
      </template>
    </nav>
  </aside>
</template>

<style scoped>
.side {
  position: fixed;
  left: clamp(16px, 1.45vw, 26px);
  top: 16px;
  bottom: 16px;
  width: var(--shell-side-width);
  display: flex;
  flex-direction: column;
  z-index: 40;
}

.side__nav {
  flex: 1;
  min-height: 0;
  padding: 16px 12px 16px;
  display: grid;
  align-content: start;
  gap: 5px;
  overflow: hidden;
}

.side__intro {
  display: grid;
  gap: 0;
  padding: 10px 10px 14px;
  text-decoration: none;
  border-bottom: 1px solid rgba(46, 34, 10, 0.08);
  margin-bottom: 6px;
  transition: transform .18s ease, opacity .18s ease;
}

.side__intro:hover {
  transform: translateY(-1px);
}

.side__intro-head {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 10px;
}

.side__intro-logo-wrap {
  display: grid;
  place-items: center;
}

.side__intro-logo {
  width: clamp(50px, 3.35vw, 60px);
  height: clamp(50px, 3.35vw, 60px);
  object-fit: contain;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(255,253,249,0.98), rgba(247,241,227,0.95));
  border: 1px solid rgba(46,34,10,0.08);
  padding: 6px;
  box-shadow: 0 10px 18px rgba(33, 24, 8, 0.05);
}

.side__intro-copy {
  min-width: 0;
  display: grid;
  gap: 1px;
}

.side__intro-eyebrow {
  font-size: 10px;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 0.13em;
  color: #c7b895;
}

.side__intro-title {
  display: block;
  font-size: clamp(20px, 1.5vw, 24px);
  line-height: 1.02;
  font-weight: 950;
  letter-spacing: -0.05em;
  color: #1e180d;
}


.side__group-label {
  padding: 7px 8px 6px;
  font-size: 10px;
  font-weight: 900;
  color: #c7b895;
  text-transform: uppercase;
  letter-spacing: .11em;
}

.side__item {
  min-height: 52px;
  border: 1px solid transparent;
  border-radius: 20px;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  color: #61563d;
  font-weight: 700;
  text-decoration: none;
  transition: transform .18s ease, background-color .18s ease, border-color .18s ease, color .18s ease;
}

.side__item:hover {
  background: rgba(247, 241, 227, 0.84);
  border-color: rgba(46, 34, 10, 0.08);
}

.side__item--active {
  background: rgba(242, 210, 33, 0.16);
  border-color: rgba(242, 210, 33, 0.28);
  color: #8a6a00;
}

.side__icon {
  width: 40px;
  height: 40px;
  border-radius: 15px;
  display: grid;
  place-items: center;
  background: rgba(247, 241, 227, 0.96);
  border: 1px solid rgba(46, 34, 10, 0.08);
  color: #61563d;
  flex-shrink: 0;
}

.side__icon--active {
  color: #8a6a00;
  background: rgba(255, 249, 226, 0.96);
  border-color: rgba(242, 210, 33, 0.3);
}

.side__item-copy {
  min-width: 0;
  display: grid;
  gap: 1px;
}

.side__item-label {
  font-size: 15px;
  font-weight: 900;
  line-height: 1.15;
  color: #302516;
}

.side__item-description {
  font-size: 11.5px;
  color: #61563d;
  line-height: 1.28;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@media (min-width: 1680px) {
  .side__item {
    min-height: 56px;
  }

  .side__item-label {
    font-size: 15px;
  }

  .side__item-description {
    font-size: 11.5px;
  }
}
</style>
