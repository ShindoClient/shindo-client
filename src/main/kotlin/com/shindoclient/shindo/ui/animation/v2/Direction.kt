package com.shindoclient.shindo.ui.animation.v2

enum class Direction {
    FORWARDS,
    BACKWARDS,
    ;

    fun opposite(): Direction = if (this == FORWARDS) BACKWARDS else FORWARDS
}
