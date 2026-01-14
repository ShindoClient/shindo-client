package me.miki.shindo.ui.animation.engine

interface Animatable {
    fun update(deltaMs: Long)
    val isRunning: Boolean
}
