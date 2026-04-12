import { createRouter, createWebHistory } from 'vue-router';
import BasicLayout from '../layout/BasicLayout.vue';

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

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  if (to.name !== 'Login' && to.name !== 'Register' && !token) {
    next({ name: 'Login' });
  } else {
    next();
  }
});

export default router;
