import http from './http'

// 위에서부터 등록 ,수정, 삭제, 조회, 상세조회,카테고리 조회, 
export const createSubscription = (payload) => http.post('/api/v1/subscriptions', payload)
export const updateSubscription = (subscriptionId, payload) => http.put(`/api/v1/subscriptions/${subscriptionId}`, payload)
export const deleteSubscription = (subscriptionId) => http.delete(`/api/v1/subscriptions/${subscriptionId}`)
export const getSubscription = (params) => http.get('/api/v1/subscriptions', { params })
export const getSubscriptionDetail = (subscriptionId) => http.get(`/api/v1/subscriptions/${subscriptionId}`)
export const getBillingCycle = () => http.get('/api/v1/subscriptions/billing-cycles')
export const getSubscriptionCategory = () => http.get('/api/v1/subscriptions/categories')
export const getSubscriptionItemByCategory = (categoryId) => http.get(`/api/v1/subscriptions/category-items/${categoryId}`)
export const getPaymentCard = () => http.get('/api/v1/cards')
