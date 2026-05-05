import http from './http'

export const getMyInfo = (userId) => http.get(`/api/v1/users/${userId}`)
export const patchProfile = (userId, payload) => http.patch(`/api/v1/users/${userId}/profile`, payload)
export const patchPassword = (userId, payload) => http.patch(`/api/v1/users/${userId}/password`, payload)
export const deleteUser = (userId) => http.delete(`/api/v1/users/${userId}`)
export const getProfileImage = (userId) => http.get(`/api/v1/users/${userId}/profile-image`)
export const deleteProfileImage = (userId) => http.delete(`/api/v1/users/${userId}/profile-image`)
export const patchProfileImage = (userId, payload) => http.patch(`/api/v1/users/${userId}/profile-image`, payload)

export const patchProfileImageFile = (userId, file) => {
  const formData = new FormData()
  formData.append('file', file)

  return http.patch(`/api/v1/users/${userId}/profile-image/file`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
