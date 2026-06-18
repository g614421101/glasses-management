import { useState, useEffect, useRef } from 'react';

/**
 * Animates a numeric value from its previous count to the new target
 * using a cubic ease-out curve over the given duration (ms).
 * Port of the Vue `useCountUp` composable.
 */
export function useCountUp(target: number, duration = 400): number {
  const [display, setDisplay] = useState(0);
  const rafRef = useRef<number | null>(null);
  const prevRef = useRef(0);

  useEffect(() => {
    const from = prevRef.current;
    const to = target;

    if (from === to) {
      setDisplay(to);
      return;
    }

    if (rafRef.current !== null) {
      cancelAnimationFrame(rafRef.current);
    }

    const start = performance.now();

    const tick = (now: number) => {
      const elapsed = now - start;
      const t = Math.min(elapsed / duration, 1);
      const ease = 1 - Math.pow(1 - t, 3);
      const value = from + (to - from) * ease;
      setDisplay(value);

      if (t < 1) {
        rafRef.current = requestAnimationFrame(tick);
      } else {
        setDisplay(to);
        rafRef.current = null;
      }
    };

    rafRef.current = requestAnimationFrame(tick);
    prevRef.current = to;

    return () => {
      if (rafRef.current !== null) {
        cancelAnimationFrame(rafRef.current);
      }
    };
  }, [target, duration]);

  return display;
}
