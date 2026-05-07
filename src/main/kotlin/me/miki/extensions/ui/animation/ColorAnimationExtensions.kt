@file:JvmName("ColorAnimationExtensions")

package me.miki.extensions.ui.animation

import me.miki.shindo.ui.animation.v1.value.ColorAnimation
import java.awt.Color

fun ColorAnimation.reset(color: Color = Color(0, 0, 0, 0)) {
    setColor(color)
}
