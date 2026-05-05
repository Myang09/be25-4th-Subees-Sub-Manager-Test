import http from './http'

export const getDashboardSummary = () => http.get('/api/dashboard/summary')
export const getUpcomingSubscriptions = () => http.get('/api/dashboard/upcoming-subscriptions')
export const getCategorySpendSummary = () => http.get('/api/dashboard/category-spend-summary')
export const getMonthlySpendTrend = () => http.get('/api/dashboard/monthly-spend-trend')
