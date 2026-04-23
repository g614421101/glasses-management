<template>
  <el-container class="layout-container">
    <el-header class="glass-header">
      <div class="logo-box" @click="goHome">
        <div class="brand-orb">
          <el-icon :size="22" color="#ffffff"><View /></el-icon>
        </div>
        <div class="brand-copy">
          <span class="logo-text">视光档案管理系统</span>
          <span class="logo-subtext">Blue clinical workspace</span>
        </div>
      </div>
      <div class="user-info">
        <div class="user-chip">
          <span class="user-label">当前账号</span>
          <strong class="welcome-text">{{ authStore.username }}</strong>
        </div>
        <el-button type="primary" plain size="small" class="logout-btn" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>
    
    <el-container class="main-body">
      <el-aside width="200px" class="glass-aside">
        <div class="aside-title">导航菜单</div>
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

<script setup lang="ts">
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
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.glass-header {
  min-height: 74px;
  background: rgba(255, 255, 255, 0.86);
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  padding: 14px 28px;
  backdrop-filter: blur(16px);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.06);
  position: sticky;
  top: 0;
  z-index: 20;
}

.logo-box {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  min-width: 0;
  transition: transform 0.3s ease;
}

.logo-box:hover {
  transform: translateY(-1px);
}

.brand-orb {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-main);
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.24);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.logo-subtext {
  font-size: 12px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.18em;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(239, 246, 255, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.user-label {
  font-size: 12px;
  color: var(--text-muted);
}

.welcome-text {
  font-size: 14px;
  color: var(--text-primary);
}

.logout-btn {
  min-width: 104px;
  color: var(--primary-color) !important;
  font-weight: 800 !important;
}

.main-body {
  flex: 1;
  overflow: visible;
  background: transparent;
  min-height: 0;
}

.glass-aside {
  background: rgba(255, 255, 255, 0.76);
  border-right: 1px solid rgba(148, 163, 184, 0.14);
  z-index: 10;
  position: sticky;
  top: 86px;
  height: fit-content;
  margin: 24px 0 24px 24px;
  padding: 20px 12px 14px;
  border-radius: 26px;
  backdrop-filter: blur(18px);
  box-shadow: 0 18px 35px rgba(15, 23, 42, 0.07);
}

.aside-title {
  padding: 0 14px 12px;
  font-size: 12px;
  font-weight: 800;
  color: var(--text-muted);
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.side-menu {
  background: transparent;
  border-right: none;
}

.side-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 6px 12px;
  border-radius: 16px;
  font-weight: 700;
  color: var(--text-secondary);
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(219, 234, 254, 0.6);
  color: var(--primary-color);
}

.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(219, 234, 254, 0.95) 0%, rgba(239, 246, 255, 0.98) 100%);
  color: var(--primary-color);
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.12), 0 12px 24px rgba(37, 99, 235, 0.12);
}

.main-content {
  padding: 24px 24px 28px;
  overflow-y: auto;
  min-height: 0;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.45s cubic-bezier(0.22, 1, 0.36, 1);
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(18px) scale(0.98);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.99);
}

@media (max-width: 1024px) {
  .glass-header {
    padding: 14px 18px;
  }

  .glass-aside {
    margin-left: 18px;
    margin-right: 18px;
  }

  .main-content {
    padding: 18px;
  }
}

@media (max-width: 900px) {
  .main-body {
    flex-direction: column;
  }

  .glass-aside {
    width: auto !important;
    position: static;
    margin: 18px;
    padding: 16px 10px 10px;
  }

  .side-menu {
    display: flex;
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .side-menu :deep(.el-menu-item) {
    flex: 0 0 auto;
    margin: 6px 8px;
  }

  .main-content {
    padding-top: 0;
  }
}

@media (max-width: 640px) {
  .glass-header {
    flex-direction: column;
    align-items: stretch;
  }

  .logo-box,
  .user-info {
    justify-content: space-between;
  }

  .brand-copy {
    gap: 2px;
  }

  .logo-subtext {
    letter-spacing: 0.12em;
  }

  .user-chip,
  .logout-btn {
    width: 100%;
    justify-content: center;
  }

  .glass-aside,
  .main-content {
    margin-left: 12px;
    margin-right: 12px;
  }
}
</style>
