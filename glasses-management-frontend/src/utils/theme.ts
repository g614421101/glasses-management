import { computed, ref } from 'vue';

export type ThemeMode = 'light' | 'dark';

const STORAGE_KEY = 'theme-mode';

const getPreferredTheme = (): ThemeMode => {
  if (typeof window === 'undefined') {
    return 'light';
  }

  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === 'light' || stored === 'dark') {
    return stored;
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

const themeMode = ref<ThemeMode>(getPreferredTheme());

export const applyTheme = (mode: ThemeMode = themeMode.value) => {
  document.documentElement.dataset.theme = mode;
  document.documentElement.style.colorScheme = mode;
};

export const initTheme = () => {
  themeMode.value = getPreferredTheme();
  applyTheme(themeMode.value);
};

export const useTheme = () => {
  const isDark = computed(() => themeMode.value === 'dark');

  const toggleTheme = () => {
    themeMode.value = isDark.value ? 'light' : 'dark';
    localStorage.setItem(STORAGE_KEY, themeMode.value);
    applyTheme(themeMode.value);
  };

  return {
    themeMode,
    isDark,
    toggleTheme
  };
};
