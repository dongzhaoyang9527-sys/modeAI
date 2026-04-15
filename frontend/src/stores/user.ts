import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const roles = ref<string[]>(JSON.parse(localStorage.getItem('roles') || '[]'))
  const nickname = ref(localStorage.getItem('nickname') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => roles.value.includes('ROLE_ADMIN'))

  async function login(loginForm: { username: string; password: string }) {
    const res: any = await loginApi(loginForm)
    token.value = res.data.token
    username.value = res.data.username
    roles.value = res.data.roles || []
    nickname.value = res.data.nickname || res.data.username
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('roles', JSON.stringify(res.data.roles || []))
    localStorage.setItem('nickname', res.data.nickname || res.data.username)
    return res
  }

  function logout() {
    token.value = ''
    username.value = ''
    roles.value = []
    nickname.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('roles')
    localStorage.removeItem('nickname')
  }

  return { token, username, roles, nickname, isLoggedIn, isAdmin, login, logout }
})
