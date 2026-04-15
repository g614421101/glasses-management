import { createRouter, createWebHistory } from 'vue-router';
import BasicLayout from '../layout/BasicLayout.vue';
import { useAuthStore } from '../store/auth';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: BasicLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('../views/Customer.vue')
      },
      {
        path: 'archive/:id',
        name: 'Archive',
        component: () => import('../views/Archive.vue')
      },
      {
        path: 'sys-user',
        name: 'SysUser',
        component: () => import('../views/SysUser.vue')
      },
      {
        path: 'stats',
        name: 'Statistics',
        component: () => import('../views/Statistics.vue')
      }
    ]
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const token = localStorage.getItem('token');
  
  if (to.name !== 'Login' && to.name !== 'Register') {
    if (!token) {
      next({ name: 'Login' });
    } else {
      // 核心修复逻辑：如果有 Token 但 Pinia 里没有用户信息，说明是首屏加载
      // 必须主动向后端验证一次 Token 的有效性
      if (!authStore.username) {
        try {
          await authStore.verifyToken();
          next();
        } catch (error) {
          // verifyToken 内部如果 401 会被拦截器处理，
          // 但这里为了路由逻辑严密，捕获后跳回登录
          next({ name: 'Login' });
        }
      } else {
        next();
      }
    }
  } else {
    next();
  }
});

export default router;
