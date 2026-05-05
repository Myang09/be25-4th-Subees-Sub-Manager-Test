import http from './http'

export const postLogin = (payload) => http.post('/api/v1/login', payload)

export const postLogout = () => http.post('/api/v1/logout')

export const postSignup = (payload) => http.post('/api/v1/users', payload)

export const getCheckEmail = (email) => http.get('/api/v1/users/check-email', {
  params: { email },
})

export const getCheckNickname = (nickname) => http.get('/api/v1/users/check-nickname', {
  params: { nickname },
})
