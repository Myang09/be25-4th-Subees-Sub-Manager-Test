export const easeInOutCubic = (value) => {
  if (value < 0.5) return 4 * value * value * value
  return 1 - ((-2 * value + 2) ** 3) / 2
}

export const animateScrollToTop = ({ duration = 720 } = {}) => {
  const startY = Math.max(window.scrollY, document.documentElement.scrollTop, document.body.scrollTop, 0)

  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    window.scrollTo(0, 0)
    return null
  }

  if (startY <= 0) return null

  let frameId = null
  const startAt = performance.now()

  const step = (timestamp) => {
    const progress = Math.min((timestamp - startAt) / duration, 1)
    const eased = easeInOutCubic(progress)
    const nextY = Math.round(startY * (1 - eased))

    window.scrollTo(0, nextY)

    if (progress < 1) {
      frameId = window.requestAnimationFrame(step)
      return
    }

    window.scrollTo(0, 0)
  }

  frameId = window.requestAnimationFrame(step)
  return frameId
}
