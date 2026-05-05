import http from './http'

// 카드 조회
export const getCards = () => {
    return http.get('/api/v1/cards')}

// 카드 등록
export const createCard = (data) => {
    return http.post('/api/v1/cards', data)}

// 카드 수정
export const updateCard = (paymentId, data) => {
    return http.patch(`/api/v1/cards/${paymentId}`, data)}

// 카드 삭제
export const deleteCard = (paymentId) => {
    return http.delete(`/api/v1/cards/${paymentId}`)}