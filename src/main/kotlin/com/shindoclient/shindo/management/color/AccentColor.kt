package com.shindoclient.shindo.management.color

import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import com.shindoclient.shindo.utils.ColorUtils
import java.awt.Color

class AccentColor(
    private val name: String,
    private val color1: Color,
    private val color2: Color,
) {
    private val animation = SimpleAnimation()

    fun getName(): String = name

    fun getColor1(): Color = color1

    fun getColor2(): Color = color2

    fun getInterpolateColor(): Color = ColorUtils.interpolateColors(15, 0, color1, color2)

    fun getInterpolateColor(index: Int): Color = ColorUtils.interpolateColors(15, index, color1, color2)

    fun getAnimation(): SimpleAnimation = animation
}
