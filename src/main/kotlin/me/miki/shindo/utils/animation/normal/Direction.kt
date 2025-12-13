package me.miki.shindo.utils.animation.normal

enum class Direction {
    FORWARDS,
    BACKWARDS;

    fun opposite(): Direction {
        return if (this == FORWARDS) BACKWARDS else FORWARDS
    }
}