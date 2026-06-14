import { ref, watch, type Ref } from 'vue'

export function useCountUp(source: Ref<number>, duration = 400) {
  const display = ref(0)
  let rafId: number | null = null

  const animate = (from: number, to: number) => {
    if (rafId) cancelAnimationFrame(rafId)
    if (from === to) { display.value = to; return }

    const start = performance.now()
    const tick = (now: number) => {
      const elapsed = now - start
      const t = Math.min(elapsed / duration, 1)
      const ease = 1 - Math.pow(1 - t, 3)
      display.value = from + (to - from) * ease
      if (t < 1) {
        rafId = requestAnimationFrame(tick)
      } else {
        display.value = to
        rafId = null
      }
    }
    rafId = requestAnimationFrame(tick)
  }

  watch(source, (newVal, oldVal) => {
    animate(oldVal ?? 0, newVal ?? 0)
  }, { immediate: true })

  return display
}
