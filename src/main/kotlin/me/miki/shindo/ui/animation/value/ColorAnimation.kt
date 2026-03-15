package me.miki.shindo.ui.animation.value

import java.awt.Color

/**
 * Animates RGB channels individually via three [SimpleAnimation] instances.
 * Reuses a cached [Color] instance to avoid per-frame allocations.
 */
open class ColorAnimation {

    private val animation = Array(3) { SimpleAnimation() }
    private var cachedColor: Color = Color(0, 0, 0, 0)
    private var cachedAlpha: Int = 0

    /**
     * Returns the animated color toward [color] at the given [speed] without allocating on every call.
     */
    fun getColor(color: Color, speed: Int): Color {
        animation[0].setAnimation(color.red.toFloat(), speed.toDouble())
        animation[1].setAnimation(color.green.toFloat(), speed.toDouble())
        animation[2].setAnimation(color.blue.toFloat(), speed.toDouble())
        val r = animation[0].value.toInt()
        val g = animation[1].value.toInt()
        val b = animation[2].value.toInt()
        val a = color.alpha
        if (cachedColor.red != r || cachedColor.green != g || cachedColor.blue != b || cachedAlpha != a) {
            cachedColor = Color(r, g, b, a)
            cachedAlpha = a
        }
        return cachedColor
    }

    /** Convenience overload using a default speed of 12. */
    fun getColor(color: Color): Color = getColor(color, 12)

    /** Instantly sets the cached color and underlying animations to [color]. */
    fun setColor(color: Color) {
        animation[0].value = color.red.toFloat()
        animation[1].value = color.green.toFloat()
        animation[2].value = color.blue.toFloat()
    }
}
