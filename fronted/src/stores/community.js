import { computed, reactive, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import { useAuthStore } from './auth'
//api 함수를 import해서 사용
import { getCommunityPosts, getCommunityPostDetail, createCommunityPost, updateCommunityPost, deleteCommunityPost, toggleCommunityScrap, cancelCommunityScrap, getScrappedCommunityPosts } from '@/api/community'


const getScrapStorageKey = (userId) => `subees-community-scraps-${userId ?? 'guest'}`

const loadJson = (key, fallback) => {
  try {
    const raw = window.localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch (error) {
    return fallback
  }
}

const saveJson = (key, value) => {
  window.localStorage.setItem(key, JSON.stringify(value))
}

const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  return `${month}월 ${day}일 ${hour}:${minute}`
}

export const useCommunityStore = defineStore('community', () => {
  const authStore = useAuthStore()
  const posts = ref([])
  const currentPost = ref(null)
  const isLoading = ref(false)
  const pagination = reactive({
    page: 1,
    totalPages: 0, // 0 > 0 = false → 스킵 → API 호출해서 페이지 불러옴. 1로 설정시 페이지 예외 뜸
    totalCount: 0,
  })
  const scrappedPostIds = ref(loadJson(getScrapStorageKey(authStore.userId), []))
  const filters = reactive({
    query: '',
    sortBy: 'latest',
  })
  const selectedPostId = ref(null)
  const successMessage = ref('')
  const errorMessage = ref('')

  const PAGE_SIZE = 10
  const SCRAP_DISPLAY_SIZE = 5   // 화면에 5개씩 표시
  const SCRAP_BACKEND_SIZE = 10  // 백에서 10개씩 반환 (고정)

  // 스크랩 목록 페이징 상태
  const scrapPosts = ref([])
  const scrapPagination = reactive({
    page: 1,
    totalPages: 1,
    totalCount: 0,
  }) // 한 페이지당 10개의 글

  const fetchPosts = async (page = 1) => {
    // 페이지 범위 사전 검증 (pagination.totalPages가 이미 설정된 경우)
    if (pagination.totalPages > 0 && (page < 1 || page > pagination.totalPages)) {
      // setError(`유효하지 않은 페이지입니다. (1 ~ ${pagination.totalPages}페이지)`)
      setError(`유효하지 않은 페이지입니다.`)

      return
    }

    isLoading.value = true
    errorMessage.value = ''
    try {
      const response = await getCommunityPosts({ page, size: PAGE_SIZE })
      const data = response.data.data
      posts.value = data.posts.map((post) => ({
        postId: post.postId,
        title: post.title,
        authorNickname: post.nickname,
        authorId: null,
        createdAt: post.createdAt,
        updatedAt: post.createdAt,
        viewCount: post.viewCount,
        scrapCount: 0,
        commentCount: 0,
        content: '',
        tags: [],
      }))
      // 백 응답에서 size/page 필드 가져옴
      // data.size 실제 현재 페이지 번호, data.page  실제 페이지 크기
      pagination.page = data.size
      pagination.totalPages = data.totalPages
      pagination.totalCount = data.totalCount
      selectedPostId.value = posts.value[0]?.postId ?? null
    } catch (error) {
      // 백엔드 에러 응답 형식 { code, status, message }
      const serverMessage = error.response?.data?.message
      const statusCode = error.response?.status
      if (serverMessage) {
        // 400 INVALID_PAGE / 500 INTERNAL_SERVER_ERROR 등 서버 메시지 그대로 표시
        setError(serverMessage)
      } else if (!error.response) {
        setError('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.')
      } else if (statusCode === 500) {
        setError('서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')
      } else {
        setError('게시글 목록을 불러오지 못했습니다.')
      }
    } finally {
      isLoading.value = false
    }
  }

  const fetchScraps = async (displayPage = 1) => {
    // 화면 5개, 백 10개 → 화면 페이지를 백 페이지로 변환
    // 화면 1,2페이지 → 백 1페이지 / 화면 3,4페이지 → 백 2페이지
    const backendPage = Math.ceil((displayPage * SCRAP_DISPLAY_SIZE) / SCRAP_BACKEND_SIZE)

    isLoading.value = true
    errorMessage.value = ''
    try {
      const response = await getScrappedCommunityPosts({ page: backendPage })
      const data = response.data.data

      // 백 결과(최대 10개)에서 화면 페이지에 맞는 5개 슬라이스
      const sliceStart = ((displayPage - 1) * SCRAP_DISPLAY_SIZE) % SCRAP_BACKEND_SIZE
      scrapPosts.value = (data.scraps || [])
        .slice(sliceStart, sliceStart + SCRAP_DISPLAY_SIZE)
        .map((scrap) => ({
          postId: scrap.postId,
          title: scrap.title,
          authorNickname: scrap.nickname,
          createdAt: scrap.createdAt,
          createdAtLabel: formatDateTime(scrap.createdAt),
          isScrapped: true,
        }))

      scrapPagination.page = displayPage
      scrapPagination.totalCount = data.totalCount
      // 화면 기준 전체 페이지 수 = 전체 스크랩 수 / 화면 1페이지 표시 개수
      scrapPagination.totalPages = Math.ceil(data.totalCount / SCRAP_DISPLAY_SIZE)
    } catch (error) {
      const serverMessage = error.response?.data?.message
      if (serverMessage) {
        setError(serverMessage)
      } else if (!error.response) {
        setError('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.')
      } else {
        setError('스크랩 목록을 불러오지 못했습니다.')
      }
    } finally {
      isLoading.value = false
    }
  }

  // 스크랩 목록 페이지에서 해제 버튼 클릭 시 호출
  // toggleScrap과 달리, 항상 DELETE를 호출하고 목록을 새로고침
  const cancelScrapFromList = async (postId) => {
    clearMessages()
    const targetId = Number(postId)
    try {
      await cancelCommunityScrap(targetId)
      // 로컬 스크랩 ID에서도 제거
      scrappedPostIds.value = scrappedPostIds.value.filter((id) => id !== targetId)
      setSuccess('스크랩을 해제했습니다.')
      // 현재 페이지 목록 새로고침 (해제된 항목 제거 반영)
      await fetchScraps(scrapPagination.page)
    } catch (error) {
      const serverMessage = error.response?.data?.message
      if (serverMessage) {
        setError(serverMessage)
      } else if (!error.response) {
        setError('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.')
      } else {
        setError('스크랩 해제 중 오류가 발생했습니다.')
      }
    }
  }

  const fetchPostDetail = async (postId) => {
    isLoading.value = true // 로딩 시작
    try {
      const response = await getCommunityPostDetail(postId) // api 호출
      const data = response.data.data // 백 응답 
      currentPost.value = { // 상태에 저장
        postId: data.postId,
        title: data.title,
        content: data.content,
        authorNickname: data.nickname,
        authorId: null,
        viewCount: data.viewCount,
        scrapCount: data.scrapCount,
        createdAt: data.createdAt,
        updatedAt: data.createdAt,
        // tags: [],
        isScrapped: scrappedPostIds.value.includes(data.postId),
        isMine: Boolean(authStore.nickname && authStore.nickname === data.nickname), //nickname이 localStorage에 저장됨(auth.js)
        createdAtLabel: formatDateTime(data.createdAt),
        updatedAtLabel: formatDateTime(data.createdAt),
      }
    } catch (error) {
      setError('게시글을 불러오지 못했습니다.')
      currentPost.value = null
    } finally {
      isLoading.value = false
    }
  }

  // 유저가 바뀌면(로그인/로그아웃/계정 전환) 해당 유저의 스크랩 목록으로 교체
  watch(
    () => authStore.userId,
    async (newUserId) => {
      scrappedPostIds.value = loadJson(getScrapStorageKey(newUserId), [])
      clearMessages()

      if (currentPost.value?.postId) {
        await fetchPostDetail(currentPost.value.postId)
      }
    },
  )



  watch(
    scrappedPostIds,
    (value) => {
      saveJson(getScrapStorageKey(authStore.userId), value)
    },
    { deep: true },
  )

  const postMap = computed(() => {
    return new Map(posts.value.map((post) => [post.postId, post]))
  })

  const enrichedPosts = computed(() => {
    return posts.value.map((post) => ({
      ...post,
      isScrapped: scrappedPostIds.value.includes(post.postId),
      isMine: Boolean(authStore.nickname && authStore.nickname === post.authorNickname), //내 게시글 표시
      createdAtLabel: formatDateTime(post.createdAt),
      updatedAtLabel: formatDateTime(post.updatedAt),
      preview: post.content.split('\n').filter(Boolean).slice(0, 2).join(' '),
    }))
  })

  const sortedPosts = computed(() => {
    const nextItems = [...enrichedPosts.value]

    if (filters.sortBy === 'views') {
      return nextItems.sort((a, b) => b.viewCount - a.viewCount)
    }

    if (filters.sortBy === 'scraps') {
      return nextItems.sort((a, b) => b.scrapCount - a.scrapCount)
    }

    return nextItems.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  })

  const filteredPosts = computed(() => {
    const keyword = filters.query.trim().toLowerCase()
    if (!keyword) return sortedPosts.value

    return sortedPosts.value.filter((post) => {
      const haystack = `${post.title} ${post.content} ${post.authorNickname} ${post.tags.join(' ')}`.toLowerCase()
      return haystack.includes(keyword)
    })
  })

  const selectedPost = computed(() => {
    if (!selectedPostId.value) return null
    const post = postMap.value.get(Number(selectedPostId.value))
    if (!post) return null

    return {
      ...post,
      isScrapped: scrappedPostIds.value.includes(post.postId),
      isMine: Boolean(authStore.userId && authStore.userId === post.authorId),
      createdAtLabel: formatDateTime(post.createdAt),
      updatedAtLabel: formatDateTime(post.updatedAt),
    }
  })

  const scrappedPosts = computed(() => enrichedPosts.value.filter((post) => post.isScrapped))
  const myPosts = computed(() => enrichedPosts.value.filter((post) => post.isMine))
  const totalPosts = computed(() => posts.value.length)
  const totalScraps = computed(() => scrappedPostIds.value.length)
  const popularPost = computed(() => [...enrichedPosts.value].sort((a, b) => b.viewCount - a.viewCount)[0] ?? null)
  const recentPost = computed(() => [...enrichedPosts.value].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0] ?? null)

    const clearMessages = () => {
      successMessage.value = ''
      errorMessage.value = ''
    }

    const setSuccess = (message) => {
      successMessage.value = message
      errorMessage.value = ''
    }

    const setError = (message) => {
      errorMessage.value = message
      successMessage.value = ''
    }



  const setQuery = (value) => {
    filters.query = value
  }

  const setSort = (value) => {
    filters.sortBy = value
  }

  const selectPost = (postId) => {
    selectedPostId.value = Number(postId)
  }

  const openPost = (postId) => {
    const targetId = Number(postId)
    const index = posts.value.findIndex((post) => post.postId === targetId)
    if (index < 0) return null

    posts.value[index] = {
      ...posts.value[index],
      viewCount: posts.value[index].viewCount + 1,
    }
    selectedPostId.value = targetId
    return posts.value[index]
  }

  const createPost = async (payload) => {
    clearMessages()

    if (!authStore.isLoggedIn) {
      setError('게시글 작성은 로그인 후 이용할 수 있습니다.')
      return null
    }

    isLoading.value = true
    try {
      const response = await createCommunityPost({
        title: payload.title.trim(),
        content: payload.content.trim(),
      })
      const data = response.data.data
      setSuccess('게시글이 등록되었습니다.')
      await fetchPosts(1)
      return { postId: data.postId }
    } catch (error) {
      setError('게시글 등록에 실패했습니다.')
      return null
    } finally {
      isLoading.value = false
    }
  }

  const updatePost = async (postId, payload) => {
    clearMessages()
    isLoading.value = true
    try {
      await updateCommunityPost(postId, {
        title: payload.title.trim(),
        content: payload.content.trim(),
      })
      setSuccess('게시글이 수정되었습니다.')
      await fetchPostDetail(postId)
      return { postId: Number(postId) }
    } catch (error) {
      setError('게시글 수정에 실패했습니다.')
      return null
    } finally {
      isLoading.value = false
    }
  }

  const deletePost = async (postId) => {
    clearMessages()
    isLoading.value = true
    try {
      await deleteCommunityPost(postId)
      setSuccess('게시글이 삭제되었습니다.')
      currentPost.value = null
      await fetchPosts(1)
      return true
    } catch (error) {
      setError('게시글 삭제에 실패했습니다.')
      return false
    } finally {
      isLoading.value = false
    }
  }

  const toggleScrap = async (postId) => {
    clearMessages()

    // 로그인 여부 + 토큰 존재 여부 체크
    // 비로그인 상태이거나 토큰이 없으면 API 호출 시 인증 실패로 500 발생
    if (!authStore.isLoggedIn || !authStore.accessToken) {
      setError('로그인 후 스크랩하실 수 있습니다.')
      return
    }

    const targetId = Number(postId)
    const hasScrapped = scrappedPostIds.value.includes(targetId)

    try {
      if (hasScrapped) {
        // 스크랩 취소시  DELETE API 호출
        await cancelCommunityScrap(targetId)
        scrappedPostIds.value = scrappedPostIds.value.filter((id) => id !== targetId)
        // 상세 페이지 상태 반영
        if (currentPost.value?.postId === targetId) {
          currentPost.value = {
            ...currentPost.value,
            isScrapped: false,
            scrapCount: Math.max(0, currentPost.value.scrapCount - 1),
          }
        }
        setSuccess('스크랩을 해제했습니다.')
      } else {
        // 스크랩 등록 시 POST API 호출
        await toggleCommunityScrap(targetId)
        scrappedPostIds.value = [...scrappedPostIds.value, targetId]
        // 상세 페이지 상태 반영
        if (currentPost.value?.postId === targetId) {
          currentPost.value = {
            ...currentPost.value,
            isScrapped: true,
            scrapCount: currentPost.value.scrapCount + 1,
          }
        }
        setSuccess('게시글을 스크랩했습니다.')
      }
    } catch (error) {
      // 백 에러 형식: { code, status, message }
      // 409 ALREADY_SCRAPPED: 이미 스크랩한 게시글
      // 400 INVALID_SCRAP_REQUEST: 자신의 글 스크랩 불가
      const serverMessage = error.response?.data?.message
      if (serverMessage) {
        setError(serverMessage)
      } else if (!error.response) {
        setError('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.')
      } else {
        setError('스크랩 처리 중 오류가 발생했습니다.')
      }
    }
  }

  const getPostById = (postId) => {
    const target = postMap.value.get(Number(postId))
    if (!target) return null
    return {
      ...target,
      isScrapped: scrappedPostIds.value.includes(target.postId),
      isMine: Boolean(authStore.userId && authStore.userId === target.authorId),
      createdAtLabel: formatDateTime(target.createdAt),
      updatedAtLabel: formatDateTime(target.updatedAt),
    }
  }

  return {
    filters,
    isLoading,
    pagination,
    currentPost,
    fetchPostDetail,
    setSuccess,
    setError,
    successMessage,
    errorMessage,
    selectedPostId,
    filteredPosts,
    selectedPost,
    scrappedPosts,
    myPosts,
    totalPosts,
    totalScraps,
    popularPost,
    recentPost,
    clearMessages,
    setQuery,
    setSort,
    selectPost,
    openPost,
    fetchPosts,
    createPost,
    updatePost,
    deletePost,
    toggleScrap,
    getPostById,
    scrapPosts,
    scrapPagination,
    fetchScraps,
    cancelScrapFromList,
  }
})
