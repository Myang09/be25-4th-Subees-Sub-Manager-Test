import { defineStore } from 'pinia'

const FAQ_ITEMS = [
  {
    id: 1,
    category: '계정',
    question: '회원가입은 어디서 하나요?',
    answer: '홈 화면 상단 회원가입 버튼 또는 /signup 화면에서 이메일, 닉네임, 비밀번호를 입력해 가입할 수 있습니다.',
    tags: ['가입', '이메일'],
    isPopular: true,
  },
  {
    id: 2,
    category: '구독',
    question: '구독 서비스는 어떻게 등록하나요?',
    answer: '사이드바의 구독 추가 메뉴에서 카테고리, 서비스, 결제정보를 입력한 뒤 마지막 확인 단계에서 저장하면 됩니다.',
    tags: ['구독등록', '결제정보'],
    isPopular: true,
  },
  {
    id: 3,
    category: '캘린더',
    question: '결제 캘린더에서는 무엇을 볼 수 있나요?',
    answer: '월별 결제일, 날짜별 결제 항목, 총 결제 금액, 카테고리별 소비 현황을 함께 확인할 수 있습니다.',
    tags: ['캘린더', '결제일'],
    isPopular: true,
  },
  {
    id: 4,
    category: '알림',
    question: '결제 예정 알림은 어디서 켜나요?',
    answer: '마이페이지의 환경설정에서 이메일 알림, 서비스 메시지 알림, 결제 리마인더를 각각 켜고 끌 수 있습니다.',
    tags: ['알림', '마이페이지'],
    isPopular: false,
  },
  {
    id: 5,
    category: '프로필',
    question: '닉네임이나 비밀번호는 어디서 바꾸나요?',
    answer: '마이페이지에서 닉네임을 수정하고 보안 설정 카드에서 비밀번호를 변경할 수 있습니다.',
    tags: ['닉네임', '비밀번호'],
    isPopular: false,
  },
  {
    id: 6,
    category: '커뮤니티',
    question: '게시글 스크랩은 어디에서 다시 보나요?',
    answer: '커뮤니티 화면의 스크랩 목록 또는 상세 화면의 스크랩 버튼을 통해 저장한 게시글을 따로 모아볼 수 있습니다.',
    tags: ['스크랩', '게시판'],
    isPopular: false,
  },
  {
    id: 7,
    category: '계정',
    question: '로그아웃은 어디서 하나요?',
    answer: '공개 홈 상단과 앱 내부 상단 헤더에서 로그아웃 버튼을 확인할 수 있습니다.',
    tags: ['로그아웃'],
    isPopular: false,
  },
]

export const useSupportStore = defineStore('support', {
  state: () => ({
    faqItems: FAQ_ITEMS,
    selectedFaqCategory: '전체',
    faqSearchKeyword: '',
  }),
  getters: {
    faqCategories(state) {
      return ['전체', ...new Set(state.faqItems.map((item) => item.category))]
    },
    filteredFaqItems(state) {
      const keyword = state.faqSearchKeyword.trim().toLowerCase()
      return state.faqItems.filter((item) => {
        const categoryMatched = state.selectedFaqCategory === '전체' || item.category === state.selectedFaqCategory
        const keywordMatched = !keyword
          || item.question.toLowerCase().includes(keyword)
          || item.answer.toLowerCase().includes(keyword)
          || item.tags.some((tag) => tag.toLowerCase().includes(keyword))

        return categoryMatched && keywordMatched
      })
    },
    popularFaqItems(state) {
      return state.faqItems.filter((item) => item.isPopular)
    },
  },
  actions: {
    setFaqCategory(category) {
      this.selectedFaqCategory = category
    },
    setFaqSearchKeyword(keyword) {
      this.faqSearchKeyword = keyword
    },
  },
})
