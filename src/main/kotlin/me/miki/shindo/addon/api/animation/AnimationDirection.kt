package me.miki.shindo.addon.api.animation

enum class AnimationDirection {
    FORWARDS,
    BACKWARDS;

    fun opposite(): AnimationDirection = if (this == FORWARDS) BACKWARDS else FORWARDS
}
