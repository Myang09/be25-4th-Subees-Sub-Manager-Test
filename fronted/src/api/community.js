// http.js 에 설정되어 있는 import한 axios를 불러온다
import http from './http'

//api 함수 모음

//params : url에 붙는 값 (페이징 ,필터 등 조회에 조건을 걸 때)
//payload : 서버에 데이터 전달할 때 (body)

export const getCommunityPosts = (params) => http.get('/api/v1/community/posts', { params }) //게시글 조회
// 실 url http://localhost:8080/api/v1/community/posts?page=1 

export const getCommunityPostDetail = (postId) => http.get(`/api/v1/community/posts/${postId}`) // 상세 조회
export const createCommunityPost = (payload) => http.post('/api/v1/community/posts', payload) // 글 작성
export const updateCommunityPost = (postId, payload) => http.put(`/api/v1/community/posts/${postId}`, payload) // 글 수정
export const deleteCommunityPost = (postId) => http.delete(`/api/v1/community/posts/${postId}`) // 글 삭제
export const toggleCommunityScrap = (postId) => http.post(`/api/v1/community/posts/${postId}/scraps`) // 스크랩 등록
export const cancelCommunityScrap = (postId) => http.delete(`/api/v1/community/posts/${postId}/scraps`) // 스크랩 취소
export const getScrappedCommunityPosts = (params) => http.get('/api/v1/community/scraps', { params }) // 스크랩 목록 페이징 조회
