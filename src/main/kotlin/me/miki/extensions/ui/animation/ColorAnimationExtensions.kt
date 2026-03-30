@file:JvmName("ColorAnimationExtensions")

package me.miki.extensions.ui.animation

import me.miki.shindo.ui.animation.value.ColorAnimation
import java.awt.Color

/**
 * Extension helpers for [ColorAnimation].
 */
/** Animates to the target color using the same speed for RGB and alpha. */
fun ColorAnimation.animateTo(color: Color, speed: Int): Color = getColor(color, speed, speed)

/** Animates to the target color with independent alpha speed. */
fun ColorAnimation.animateToWithAlpha(color: Color, speed: Int, alphaSpeed: Int): Color =
    getColor(color, speed, alphaSpeed)

/** Resets the underlying color animation to [color] (defaults to transparent). */
fun ColorAnimation.reset(color: Color = Color(0, 0, 0, 0)) {
    setColor(color)
}
