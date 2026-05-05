import http from './http'

// 캘린더 전체 조회
export const getPaymentCalendar = (params = {}) =>
    http.get('/api/v1/consumption/calendar-summary', { params })

// 선택 날짜 상세 조회
export const getPaymentDateDetails = (params = {}) =>
    http.get('/api/v1/consumption/date-details', { params })

// 카테고리 전체 조회
export const getCategorySummary = (params = {}) =>
    http.get('/api/v1/consumption/category-summary', { params })

// 소비 분석 조회(카테고리)
export const getPaymentAnalytics = (params = {}) =>
    http.get('/api/v1/consumption/analysis/categories', { params })

// 월 결제 정보 조회
export const getMonthlyPaymentList = (params = {}) =>
    http.get('/api/v1/consumption/analysis/month', { params })