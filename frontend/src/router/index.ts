import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/views/LayoutView.vue'),
      redirect: '/chat',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'chat',
          name: 'Chat',
          component: () => import('@/views/ChatView.vue'),
          meta: { title: '智能问答' }
        },
        {
          path: 'documents',
          name: 'Documents',
          component: () => import('@/views/DocumentView.vue'),
          meta: { title: '文档管理' }
        },
        {
          path: 'history',
          name: 'History',
          component: () => import('@/views/HistoryView.vue'),
          meta: { title: '问答历史' }
        },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '仪表盘' }
        },
        {
          path: 'admin/users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/UserManageView.vue'),
          meta: { title: '用户管理', requiresAdmin: true }
        },
        {
          path: 'admin/roles',
          name: 'AdminRoles',
          component: () => import('@/views/admin/RoleManageView.vue'),
          meta: { title: '角色管理', requiresAdmin: true }
        },
        {
          path: 'admin/departments',
          name: 'AdminDepartments',
          component: () => import('@/views/admin/DepartmentManageView.vue'),
          meta: { title: '部门管理', requiresAdmin: true }
        },
        {
          path: 'admin/monitor',
          name: 'AdminMonitor',
          component: () => import('@/views/admin/MonitorView.vue'),
          meta: { title: '系统监控', requiresAdmin: true }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth !== false && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/')
  } else {
    next()
  }
})

export default router
