package me.miki.shindo.ui.animation

object GlobalAnimationSettings {

    @JvmField
    var enabled: Boolean = true

    @JvmField
    var animationScale: Float = 1.0f

    fun scaleDuration(durationMs: Long): Long {
        if (!enabled) return 0L
        val scaled = durationMs * animationScale
        return if (scaled <= 0f) 0L else scaled.toLong()
    }
}
