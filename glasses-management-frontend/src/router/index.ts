import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';
import BasicLayout from '../layout/BasicLayout.vue';
import { useAuthStore } from '../store/auth';
import { FEATURES } from '../config/features';

const routes: Array<RouteRecordRaw> = [
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
      ...(FEATURES.CUSTOMER ? [{ path: 'customer', name: 'Customer', component: () => import('../views/Customer.vue') }] : []),
      {
        path: 'archive/:id',
        name: 'Archive',
        component: () => import('../views/Archive.vue')
      },
      ...(FEATURES.SYS_USER ? [{ path: 'sys-user', name: 'SysUser', component: () => import('../views/SysUser.vue') }] : []),
      ...(FEATURES.PROFILE ? [{ path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') }] : []),
      ...(FEATURES.RECYCLE_BIN ? [{ path: 'recycle-bin', name: 'RecycleBin', component: () => import('../views/RecycleBin.vue') }] : []),
      ...(FEATURES.STATISTICS ? [{ path: 'stats', name: 'Statistics', component: () => import('../views/Statistics.vue') }] : []),
      ...(FEATURES.DATA_MANAGE ? [{ path: 'data-manage', name: 'DataManage', component: () => import('../views/DataManage.vue') }] : [])
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

router.beforeEach(async (to, _from, next) => {
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
          if ((to.name === 'SysUser' || to.name === 'RecycleBin') && authStore.role !== 'admin') {
            next({ name: 'Home' });
            return;
          }
          next();
        } catch {
          next({ name: 'Login' });
        }
      } else {
        if ((to.name === 'SysUser' || to.name === 'RecycleBin') && authStore.role !== 'admin') {
          next({ name: 'Home' });
          return;
        }
        next();
      }
    }
  } else {
    next();
  }
});

export default router;