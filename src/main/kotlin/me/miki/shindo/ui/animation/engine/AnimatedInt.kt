package me.miki.shindo.ui.animation.engine

import kotlin.math.roundToInt

class AnimatedInt(
    initial: Int = 0,
    controller: AnimationController? = AnimationController.global
) : AnimatedValue<Int>(initial, controller) {

    override fun lerp(from: Int, to: Int, t: Float): Int {
        val value = from + (to - from) * t
        return value.roundToInt()
    }
}
