import React, { useState, useCallback } from 'react';
import { useTheme } from '@/hooks/useTheme';
import { SunOutlined, MoonOutlined } from '@ant-design/icons';

interface ThemeToggleProps {
  className?: string;
}

const ThemeToggle: React.FC<ThemeToggleProps> = ({ className = '' }) => {
  const { isDark, toggleTheme } = useTheme();
  const [spinning, setSpinning] = useState(false);

  const handleClick = useCallback(() => {
    setSpinning(true);
    toggleTheme();
  }, [toggleTheme]);

  const handleAnimEnd = useCallback(() => {
    setSpinning(false);
  }, []);

  return (
    <button
      type="button"
      onClick={handleClick}
      aria-label={isDark ? '切换到浅色模式' : '切换到暗色模式'}
      className={`theme-toggle-btn ${className}`}
    >
      <span
        className={`theme-toggle-icon ${spinning ? 'theme-toggle-icon--spin' : ''}`}
        onAnimationEnd={handleAnimEnd}
      >
        {isDark ? <SunOutlined /> : <MoonOutlined />}
      </span>
    </button>
  );
};

export default ThemeToggle;
