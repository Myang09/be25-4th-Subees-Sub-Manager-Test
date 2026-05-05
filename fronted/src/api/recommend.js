import http from './http'

export const postRecommendSubmit = (payload) => http.post('/api/v1/recommend/submit', payload)
export const postRecommendGenerate = (payload) => http.post('/api/v1/recommend/generate', payload)
export const postRecommendSave = (payload) => http.post('/api/v1/recommend/save', payload)
export const getRecommendList = () => http.get('/api/v1/recommend/list')
export const getRecommendDetail = (reportId) => http.get(`/api/v1/recommend/${reportId}`)
export const deleteRecommend = (reportId) => http.delete(`/api/v1/recommend/${reportId}`)
