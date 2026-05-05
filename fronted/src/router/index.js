import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/pages/HomePage.vue'),
    meta: { layout: 'public' },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/pages/DashboardPage.vue'),
    meta: { requiresAuth: true, pageTitle: '대시보드' },
  },
  {
    path: '/subscriptions',
    name: 'subscriptions',
    component: () => import('@/pages/SubscriptionListPage.vue'),
    meta: { requiresAuth: true, pageTitle: '구독 목록' },
  },
  {
    path: '/subscriptions/new',
    name: 'subscription-create',
    component: () => import('@/pages/SubscriptionCreatePage.vue'),
    meta: { requiresAuth: true, pageTitle: '구독 추가' },
  },
  {
    path: '/calendar',
    name: 'calendar',
    component: () => import('@/pages/PaymentCalendarPage.vue'),
    meta: { requiresAuth: true, pageTitle: '결제 캘린더' },
  },
  {
    path: '/community',
    name: 'community',
    component: () => import('@/pages/CommunityBoardPage.vue'),
    meta: { requiresAuth: true, pageTitle: '커뮤니티' },
  },
  {
    path: '/community/new',
    name: 'community-new',
    component: () => import('@/pages/CommunityPostFormPage.vue'),
    meta: { requiresAuth: true, pageTitle: '게시글 작성' },
  },
  {
    path: '/community/scraps',
    name: 'community-scraps',
    component: () => import('@/pages/CommunityScrapPage.vue'),
    meta: { requiresAuth: true, pageTitle: '스크랩 목록' },
  },
  {
    path: '/community/:postId/edit',
    name: 'community-edit',
    component: () => import('@/pages/CommunityPostFormPage.vue'),
    meta: { requiresAuth: true, pageTitle: '게시글 수정' },
  },
  {
    path: '/community/:postId',
    name: 'community-detail',
    component: () => import('@/pages/CommunityPostDetailPage.vue'),
    meta: { requiresAuth: true, pageTitle: '게시글 상세' },
  },
  {
    path: '/faq',
    name: 'faq',
    component: () => import('@/pages/FaqPage.vue'),
    meta: { requiresAuth: true, pageTitle: 'FAQ' },
  },
  {
    path: '/ai-recommendations',
    name: 'ai-recommendations',
    component: () => import('@/pages/AiRecommendationsPage.vue'),
    meta: { requiresAuth: true, pageTitle: 'AI 추천' },
  },
  {
    path: '/ai-recommendations/results',
    name: 'ai-recommendation-results',
    component: () => import('@/pages/AiRecommendationResultsPage.vue'),
    meta: { requiresAuth: true, pageTitle: 'AI 추천 결과' },
  },
  {
    path: '/ai-recommendations/history',
    name: 'ai-recommendation-history',
    component: () => import('@/pages/AiRecommendationHistoryPage.vue'),
    meta: { requiresAuth: true, pageTitle: 'AI 추천 기록' },
  },
  {
    path: '/ai-recommendations/:recommendationId',
    name: 'ai-recommendation-detail',
    component: () => import('@/pages/AiRecommendationDetailPage.vue'),
    meta: { requiresAuth: true, pageTitle: 'AI 추천 상세' },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/LoginPage.vue'),
    meta: { guestOnly: true },
  },
  {
    path: '/signup',
    name: 'signup',
    component: () => import('@/pages/SignupPage.vue'),
    meta: { guestOnly: true },
  },
  {
    path: '/password/reset/request',
    redirect: '/login',
  },
  {
    path: '/password/reset/change',
    redirect: '/login',
  },
  {
    path: '/notifications',
    name: 'notifications',
    component: () => import('@/pages/NotificationsPage.vue'),
    meta: { requiresAuth: true, pageTitle: '알림 센터' },
  },
  {
    path: '/payment-cards',
    name: 'payment-cards',
    component: () => import('@/pages/PaymentCardsPage.vue'),
    meta: { requiresAuth: true, pageTitle: '결제 카드' },
  },
  {
    path: '/mypage',
    name: 'mypage',
    component: () => import('@/pages/MyPage.vue'),
    meta: { requiresAuth: true, pageTitle: '마이페이지' },
  },
  {
    path: '/help',
    redirect: '/faq',
  },
  {
    path: '/chatbot',
    redirect: '/faq',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.guestOnly && authStore.isLoggedIn) {
    return '/dashboard'
  }

  return true
})

export default router
