@file:JvmName("ColorAnimationExtensions")

package me.miki.extensions.animation

import me.miki.shindo.ui.animation.value.ColorAnimation
import java.awt.Color

/**
 * Extension helpers for [ColorAnimation].
 */
fun ColorAnimation.animateTo(color: Color, speed: Int): Color = getColor(color, speed, speed)

fun ColorAnimation.animateToWithAlpha(color: Color, speed: Int, alphaSpeed: Int): Color =
    getColor(color, speed, alphaSpeed)

fun ColorAnimation.reset(color: Color = Color(0, 0, 0, 0)) {
    setColor(color)
}
