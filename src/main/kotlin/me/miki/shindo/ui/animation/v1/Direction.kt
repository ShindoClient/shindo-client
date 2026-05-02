package me.miki.shindo.ui.animation.v1

enum class Direction {
    FORWARDS,
    BACKWARDS;

    fun opposite(): Direction = if (this == FORWARDS) BACKWARDS else FORWARDS
}
