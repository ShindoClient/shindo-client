package me.miki.shindo.ui.animation.value

import java.awt.Color

/**
 * Animates RGBA channels via four [SimpleAnimation] instances.
 * Reuses a cached [Color] instance to avoid per-frame allocations.
 */
open class ColorAnimation {

    private val animation = Array(4) { SimpleAnimation() }
    private var cachedColor: Color = Color(0, 0, 0, 0)

    /**
     * Returns the animated color toward [color] using shared speed for RGB and alphaSpeed for alpha.
     */
    fun getColor(color: Color, speed: Int, alphaSpeed: Int): Color {
        animation[0].setAnimation(color.red.toFloat(), speed.toDouble())
        animation[1].setAnimation(color.green.toFloat(), speed.toDouble())
        animation[2].setAnimation(color.blue.toFloat(), speed.toDouble())
        animation[3].setAnimation(color.alpha.toFloat(), alphaSpeed.toDouble())
        val r = animation[0].value.toInt()
        val g = animation[1].value.toInt()
        val b = animation[2].value.toInt()
        val a = animation[3].value.toInt()
        if (cachedColor.red != r || cachedColor.green != g || cachedColor.blue != b || cachedColor.alpha != a) {
            cachedColor = Color(r, g, b, a)
        }
        return cachedColor
    }

    /**
     * Returns the animated color using the same [speed] for RGB and alpha channels.
     */
    fun getColor(color: Color, speed: Int): Color = getColor(color, speed, speed)

    /** Convenience overload using a default speed of 12 for all channels. */
    fun getColor(color: Color): Color = getColor(color, 12, 12)

    /** Instantly sets the cached color and underlying animations to [color]. */
    fun setColor(color: Color) {
        animation[0].value = color.red.toFloat()
        animation[1].value = color.green.toFloat()
        animation[2].value = color.blue.toFloat()
        animation[3].value = color.alpha.toFloat()
        cachedColor = color
    }
}
