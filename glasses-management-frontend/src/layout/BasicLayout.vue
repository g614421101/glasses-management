<template>
  <el-container class="layout-container">
    <el-header class="glass-header">
      <div class="logo-box">
        <el-button link class="mobile-menu-toggle" v-if="isMobile" @click="drawerVisible = true" style="margin-right: 8px;">
          <el-icon :size="20"><MenuIcon /></el-icon>
        </el-button>
        <div class="brand-orb" @click="goHome">
          <el-icon :size="isMobile ? 18 : 22" color="#ffffff"><View /></el-icon>
        </div>
        <div class="brand-copy" v-if="!isMobile" @click="goHome">
          <span class="logo-text">视光档案管理系统</span>
          <span class="logo-subtext">Optical Record Management System</span>
        </div>
        <span class="logo-text" v-else @click="goHome">视光档案</span>
      </div>

      <!-- Desktop User Info -->
      <div class="user-info" v-if="!isMobile">
        <div class="user-chip user-chip--link" @click="router.push('/profile')">
          <span class="user-label">当前账号</span>
          <strong class="welcome-text">{{ authStore.username }}</strong>
        </div>
        <el-button
          class="theme-toggle-btn"
          plain
          size="small"
          :aria-label="isDark ? '切换到浅色模式' : '切换到暗色模式'"
          @click="toggleTheme"
        >
          <el-icon>
            <Sunny v-if="isDark" />
            <Moon v-else />
          </el-icon>
          <span>{{ isDark ? '浅色' : '暗色' }}</span>
        </el-button>
        <el-button type="primary" plain size="small" class="logout-btn" @click="handleLogout">退出登录</el-button>
      </div>

      <!-- Mobile User Info Dropdown -->
      <div class="user-info user-info--mobile" v-else>
        <el-button
          class="theme-toggle-btn theme-toggle-btn--mobile"
          circle
          size="default"
          @click="toggleTheme"
        >
          <el-icon>
            <Sunny v-if="isDark" />
            <Moon v-else />
          </el-icon>
        </el-button>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button circle class="mobile-avatar-btn">
            <el-icon :size="16"><User /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu class="mobile-dropdown-menu">
              <el-dropdown-item command="profile" disabled>
                <div class="user-profile-meta">
                  <span class="user-meta-label">当前账号:</span>
                  <strong class="user-meta-val">{{ authStore.username }}</strong>
                </div>
              </el-dropdown-item>
              <el-dropdown-item command="profile" divided>
                <el-icon><User /></el-icon>个人主页
              </el-dropdown-item>
              <el-dropdown-item command="logout" style="color: var(--el-color-danger)">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container class="main-body">
      <!-- Desktop Sidebar Menu -->
      <el-aside :width="isCollapsed ? '76px' : '220px'" class="glass-aside" :class="{ 'glass-aside--collapsed': isCollapsed }" v-if="!isMobile">
        <div class="aside-title" v-show="!isCollapsed">导航菜单</div>
        <el-menu :default-active="route.path" router class="side-menu" :border="false" :collapse="isCollapsed">
          <el-menu-item index="/">
            <el-icon><Monitor /></el-icon>
            <span>工作平台</span>
          </el-menu-item>
          <el-menu-item index="/customer" v-if="FEATURES.CUSTOMER">
            <el-icon><User /></el-icon>
            <span>顾客管理</span>
          </el-menu-item>
          <el-menu-item index="/stats" v-if="FEATURES.STATISTICS">
            <el-icon><TrendCharts /></el-icon>
            <span>营收统计</span>
          </el-menu-item>
          <el-menu-item index="/data-manage" v-if="FEATURES.DATA_MANAGE">
            <el-icon><FolderOpened /></el-icon>
            <span>数据管理</span>
          </el-menu-item>
          <el-menu-item index="/profile" v-if="FEATURES.PROFILE">
            <el-icon><User /></el-icon>
            <span>个人主页</span>
          </el-menu-item>
          <el-menu-item index="/recycle-bin" v-if="FEATURES.RECYCLE_BIN && authStore.role === 'admin'">
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </el-menu-item>
          <el-menu-item index="/sys-user" v-if="FEATURES.SYS_USER && authStore.role === 'admin'">
            <el-icon><Setting /></el-icon>
            <span>账号管理(超管)</span>
          </el-menu-item>
        </el-menu>
        <div class="collapse-toggle-wrapper">
          <el-button link class="collapse-toggle-btn" @click="isCollapsed = !isCollapsed">
            <el-icon :size="18">
              <Expand v-if="isCollapsed" />
              <Fold v-else />
            </el-icon>
            <span v-show="!isCollapsed">收起菜单</span>
          </el-button>
        </div>
      </el-aside>

      <!-- Mobile/Tablet Drawer Menu -->
      <el-drawer
        v-if="isMobile"
        v-model="drawerVisible"
        direction="ltr"
        size="250px"
        :with-header="false"
        class="mobile-menu-drawer"
      >
        <div class="drawer-logo-box">
          <div class="brand-orb">
            <el-icon :size="20" color="#ffffff"><View /></el-icon>
          </div>
          <span class="logo-text">视光档案</span>
        </div>
        <div class="drawer-title">导航菜单</div>
        <el-menu :default-active="route.path" router class="drawer-side-menu" @select="drawerVisible = false">
          <el-menu-item index="/">
            <el-icon><Monitor /></el-icon>
            <span>工作平台</span>
          </el-menu-item>
          <el-menu-item index="/customer" v-if="FEATURES.CUSTOMER">
            <el-icon><User /></el-icon>
            <span>顾客管理</span>
          </el-menu-item>
          <el-menu-item index="/stats" v-if="FEATURES.STATISTICS">
            <el-icon><TrendCharts /></el-icon>
            <span>营收统计</span>
          </el-menu-item>
          <el-menu-item index="/data-manage" v-if="FEATURES.DATA_MANAGE">
            <el-icon><FolderOpened /></el-icon>
            <span>数据管理</span>
          </el-menu-item>
          <el-menu-item index="/profile" v-if="FEATURES.PROFILE">
            <el-icon><User /></el-icon>
            <span>个人主页</span>
          </el-menu-item>
          <el-menu-item index="/recycle-bin" v-if="FEATURES.RECYCLE_BIN && authStore.role === 'admin'">
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </el-menu-item>
          <el-menu-item index="/sys-user" v-if="FEATURES.SYS_USER && authStore.role === 'admin'">
            <el-icon><Setting /></el-icon>
            <span>账号管理(超管)</span>
          </el-menu-item>
        </el-menu>
      </el-drawer>
      
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
import { ref, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '../store/auth';
import { useRouter, useRoute } from 'vue-router';
import { Monitor, User, Setting, View, TrendCharts, Moon, Sunny, Delete, FolderOpened, SwitchButton, Menu as MenuIcon, Expand, Fold } from '@element-plus/icons-vue';
import { useTheme } from '../utils/theme';
import { FEATURES } from '../config/features';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const { isDark, toggleTheme } = useTheme();

const drawerVisible = ref(false);
const isCollapsed = ref(false);

const isMobile = ref(window.matchMedia('(max-width: 900px)').matches);
let mediaQuery: MediaQueryList | null = null;
let mediaHandler: ((e: MediaQueryListEvent) => void) | null = null;

onMounted(() => {
  mediaQuery = window.matchMedia('(max-width: 900px)');
  mediaHandler = (e: MediaQueryListEvent) => { isMobile.value = e.matches; };
  mediaQuery.addEventListener('change', mediaHandler);
});

onUnmounted(() => {
  if (mediaQuery && mediaHandler) {
    mediaQuery.removeEventListener('change', mediaHandler);
  }
});

const goHome = () => {
  router.push('/');
}

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
}

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile');
  } else if (command === 'logout') {
    handleLogout();
  }
};
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.glass-header {
  height: auto !important;
  min-height: 74px;
  background: var(--surface-overlay);
  border-bottom: 1px solid var(--border-color);
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
  min-width: 0;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 999px;
  background: var(--surface-muted);
  border: 1px solid var(--border-color);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18);
  min-width: 0;
}

.user-chip--link {
  cursor: pointer;
  transition: transform 0.24s ease, border-color 0.24s ease;
}

.user-chip--link:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
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

.theme-toggle-btn {
  min-width: 92px;
  height: 36px !important;
  color: var(--text-secondary) !important;
  border-color: var(--border-color) !important;
  background: var(--surface-muted) !important;
  font-weight: 800 !important;
}

.theme-toggle-btn:hover {
  color: var(--primary-color) !important;
  border-color: var(--border-strong) !important;
  transform: translateY(-1px);
}

.main-body {
  flex: 1;
  overflow: visible;
  background: transparent;
  min-height: 0;
}

.glass-aside {
  background: var(--surface-color);
  border-right: 1px solid var(--border-color);
  z-index: 10;
  position: sticky;
  top: 86px;
  height: fit-content;
  margin: 24px 0 24px 24px;
  padding: 20px 12px 14px;
  border-radius: 26px;
  backdrop-filter: blur(18px);
  box-shadow: 0 18px 35px rgba(15, 23, 42, 0.07);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1), padding 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-aside--collapsed {
  padding: 20px 6px 14px;
}

.aside-title {
  padding: 0 14px 12px;
  font-size: 12px;
  font-weight: 800;
  color: var(--text-muted);
  letter-spacing: 0.14em;
  text-transform: uppercase;
  white-space: nowrap;
  overflow: hidden;
}

.side-menu {
  background: transparent;
  border-right: none;
}

.side-menu.el-menu--collapse {
  width: 100% !important;
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

.glass-aside--collapsed .side-menu :deep(.el-menu-item) {
  margin: 6px 4px !important;
  padding: 0 !important;
  display: flex;
  justify-content: center;
}

.side-menu :deep(.el-menu-item:hover) {
  background: var(--primary-soft);
  color: var(--primary-color);
}

.side-menu :deep(.el-menu-item.is-active) {
  background: var(--gradient-soft);
  color: var(--primary-color);
  box-shadow: inset 0 0 0 1px var(--border-strong), 0 12px 24px rgba(37, 99, 235, 0.12);
}

.collapse-toggle-wrapper {
  margin-top: 20px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
}

.collapse-toggle-btn {
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 0 16px !important;
  color: var(--text-secondary) !important;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.glass-aside--collapsed .collapse-toggle-btn {
  justify-content: center;
  padding: 0 !important;
}

.collapse-toggle-btn:hover {
  background: var(--primary-soft);
  color: var(--primary-color) !important;
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
    flex-direction: column !important;
  }

  .main-content {
    padding-top: 16px !important;
    padding-left: 12px !important;
    padding-right: 12px !important;
  }
}

@media (max-width: 640px) {
  .glass-header {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    padding: 10px 14px;
    min-height: 56px;
  }

  .brand-orb {
    width: 34px;
    height: 34px;
    border-radius: 12px;
  }

  .logo-text {
    font-size: 16px;
  }
}

/* Mobile Menu Drawer Styling */
.mobile-menu-drawer :deep(.el-drawer) {
  background: var(--surface-overlay) !important;
  backdrop-filter: blur(20px);
  border-right: 1px solid var(--border-color);
}

.mobile-menu-drawer :deep(.el-drawer__body) {
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.drawer-logo-box {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-left: 8px;
}

.drawer-title {
  padding: 0 8px 12px;
  font-size: 12px;
  font-weight: 800;
  color: var(--text-muted);
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.drawer-side-menu {
  background: transparent;
  border-right: none;
  flex: 1;
}

.drawer-side-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 6px 0;
  border-radius: 16px;
  font-weight: 700;
  color: var(--text-secondary);
}

.drawer-side-menu :deep(.el-menu-item:hover) {
  background: var(--primary-soft);
  color: var(--primary-color);
}

.drawer-side-menu :deep(.el-menu-item.is-active) {
  background: var(--gradient-soft);
  color: var(--primary-color);
  box-shadow: inset 0 0 0 1px var(--border-strong), 0 12px 24px rgba(37, 99, 235, 0.12);
}

.user-info--mobile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-toggle-btn--mobile {
  width: 36px;
  height: 36px !important;
  min-width: 0 !important;
  padding: 0 !important;
  background: var(--surface-muted) !important;
  border-color: var(--border-color) !important;
  color: var(--text-secondary) !important;
}

.theme-toggle-btn--mobile:hover {
  border-color: var(--border-strong) !important;
  color: var(--primary-color) !important;
}

.mobile-avatar-btn {
  width: 36px;
  height: 36px !important;
  min-width: 0 !important;
  padding: 0 !important;
  background: var(--primary-soft) !important;
  border-color: var(--border-strong) !important;
  color: var(--primary-color) !important;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.12) !important;
}

.mobile-avatar-btn:hover {
  background: var(--primary-color) !important;
  color: #ffffff !important;
}

.user-profile-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
  padding: 4px 0;
  text-align: left;
}

.user-meta-label {
  font-size: 11px;
  color: var(--text-muted);
}

.user-meta-val {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 700;
  margin-top: 2px;
}

.mobile-dropdown-menu :deep(.el-dropdown-menu__item.is-disabled) {
  cursor: default;
  background-color: transparent !important;
  opacity: 1;
}
</style>