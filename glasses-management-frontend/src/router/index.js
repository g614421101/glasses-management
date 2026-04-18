import { createRouter, createWebHashHistory } from 'vue-router';
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
  history: createWebHashHistory(),
  routes
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const token = localStorage.getItem('token');
  
  if (to.name !== 'Login' && to.name !== 'Register') {
    if (!token) {
      next({ name: 'Login' });
    } else {
      // 使用内存标记 verified（而非 localStorage 中的 username）
      // 确保每次应用重新启动（后端重启/Token 过期）都会重新向后端验证一次
      if (!authStore.verified) {
        try {
          await authStore.verifyToken();
          next();
        } catch (error) {
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
