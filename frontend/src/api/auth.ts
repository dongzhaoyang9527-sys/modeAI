import request from './request'

export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data)
}

export function register(data: { username: string; password: string; email?: string; nickname?: string }) {
  return request.post('/auth/register', data)
}

export function refreshToken(refreshToken: string) {
  return request.post('/auth/refresh', null, { headers: { 'X-Refresh-Token': refreshToken } })
}

export function logout() {
  return request.post('/auth/logout')
}
