package me.miki.shindo.ui.animation.engine

import java.util.concurrent.CopyOnWriteArraySet

class AnimationController {

    private val animations = CopyOnWriteArraySet<Animatable>()
    private var lastUpdateMs = System.currentTimeMillis()

    fun add(animation: Animatable) {
        animations.add(animation)
    }

    fun remove(animation: Animatable) {
        animations.remove(animation)
    }

    fun clear() {
        animations.clear()
    }

    fun tick() {
        val now = System.currentTimeMillis()
        val delta = now - lastUpdateMs
        lastUpdateMs = now
        tick(delta)
    }

    fun tick(deltaMs: Long) {
        val safeDelta = if (deltaMs < 0) 0 else deltaMs
        for (animation in animations) {
            animation.update(safeDelta)
            if (!animation.isRunning) {
                animations.remove(animation)
            }
        }
    }

    companion object {
        @JvmField
        val global = AnimationController()
    }
}
