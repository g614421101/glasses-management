<template>
  <el-container class="layout-container">
    <el-header class="glass-header">
      <div class="logo-box" @click="goHome">
        <el-icon :size="24" color="var(--primary-color)"><View /></el-icon>
        <span class="logo-text">视光档案管理系统</span>
      </div>
      <div class="user-info">
        <span class="welcome-text">欢迎, {{ authStore.username }}</span>
        <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>
    
    <el-container class="main-body">
      <el-aside width="200px" class="glass-aside">
        <el-menu :default-active="route.path" router class="side-menu" :border="false">
          <el-menu-item index="/">
            <el-icon><Monitor /></el-icon>
            <span>工作平台</span>
          </el-menu-item>
          <el-menu-item index="/customer">
            <el-icon><User /></el-icon>
            <span>顾客管理</span>
          </el-menu-item>
          <el-menu-item index="/stats">
            <el-icon><TrendCharts /></el-icon>
            <span>营收统计</span>
          </el-menu-item>
          <el-menu-item index="/sys-user" v-if="authStore.role === 'admin'">
            <el-icon><Setting /></el-icon>
            <span>账号管理(超管)</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useAuthStore } from '../store/auth';
import { useRouter, useRoute } from 'vue-router';
import { Monitor, User, Setting, View, TrendCharts } from '@element-plus/icons-vue';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const goHome = () => {
  router.push('/');
}

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.glass-header {
  height: 60px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.logo-box {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 1px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.main-body {
  flex: 1;
  overflow: hidden;
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
}

.glass-aside {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-right: 1px solid var(--border-color);
  z-index: 100;
  position: relative;
}

.side-menu {
  background: transparent;
  border-right: none;
}

.el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(79, 70, 229, 0.1) 0%, transparent 100%);
  border-left: 4px solid var(--primary-color);
  font-weight: 600;
}

.main-content {
  padding: 24px;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(15px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}
</style>
