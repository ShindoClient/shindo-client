package me.miki.shindo.ui.animation.engine

object AnimationClock {

    private var lastMs = System.currentTimeMillis()

    fun reset() {
        lastMs = System.currentTimeMillis()
    }

    fun deltaMs(): Long {
        val now = System.currentTimeMillis()
        val delta = now - lastMs
        lastMs = now
        return if (delta < 0) 0 else delta
    }
}
