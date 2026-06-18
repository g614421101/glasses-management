import { useState, useEffect, useCallback } from 'react';

type ThemeMode = 'light' | 'dark';

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

export const useTheme = () => {
  const [theme, setTheme] = useState<ThemeMode>(getPreferredTheme);

  const applyTheme = useCallback((mode: ThemeMode) => {
    document.documentElement.dataset.theme = mode;
    document.documentElement.style.colorScheme = mode;
  }, []);

  useEffect(() => {
    applyTheme(theme);
  }, [theme, applyTheme]);

  const toggleTheme = useCallback(() => {
    // Temporarily disable transitions to prevent intermediate states
    const root = document.documentElement;
    root.classList.add('theme-switching');

    setTheme((prev) => {
      const newTheme = prev === 'light' ? 'dark' : 'light';
      localStorage.setItem(STORAGE_KEY, newTheme);
      return newTheme;
    });

    // Re-enable transitions after the browser has painted the new theme
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        root.classList.remove('theme-switching');
      });
    });
  }, []);

  const isDark = theme === 'dark';

  return { theme, isDark, toggleTheme };
};
