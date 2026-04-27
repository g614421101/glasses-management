<template>
  <router-view />
  <button
    v-if="showFloatingTheme"
    class="theme-float"
    type="button"
    :aria-label="isDark ? '切换到浅色模式' : '切换到暗色模式'"
    @click="toggleTheme"
  >
    <span class="theme-float__icon">{{ isDark ? '☀' : '☾' }}</span>
    <span>{{ isDark ? '浅色' : '暗色' }}</span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useTheme } from './utils/theme';

const route = useRoute();
const { isDark, toggleTheme } = useTheme();

const showFloatingTheme = computed(() => route.name === 'Login' || route.name === 'Register');
</script>

<style>
#app {
  width: 100%;
  min-height: 100vh;
}

.theme-float {
  position: fixed;
  top: 22px;
  right: 22px;
  z-index: 100;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  padding: 0 16px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: var(--surface-overlay);
  color: var(--text-primary);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(18px);
  cursor: pointer;
  font-weight: 800;
  letter-spacing: 0.04em;
  transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
}

.theme-float:hover {
  transform: translateY(-2px);
  border-color: var(--border-strong);
  box-shadow: var(--shadow-card);
}

.theme-float__icon {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary-color);
  font-size: 15px;
}

@media (max-width: 640px) {
  .theme-float {
    top: 12px;
    right: 12px;
    min-height: 38px;
    padding: 0 12px;
    font-size: 13px;
  }
}
</style>
