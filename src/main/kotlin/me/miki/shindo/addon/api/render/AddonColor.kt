package me.miki.shindo.addon.api.render

/**
 * Cor no formato RGBA para uso em render.
 */
data class AddonColor(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int = 255
) {
    fun toRgba(): Int = (a shl 24) or (r shl 16) or (g shl 8) or b

    fun withAlpha(alpha: Int): AddonColor = copy(a = alpha.coerceIn(0, 255))

    companion object {
        val WHITE = AddonColor(255, 255, 255)
        val BLACK = AddonColor(0, 0, 0)
        val TRANSPARENT = AddonColor(0, 0, 0, 0)
        val RED = AddonColor(255, 0, 0)
        val GREEN = AddonColor(0, 255, 0)
        val BLUE = AddonColor(0, 0, 255)

        fun fromRgba(rgba: Int): AddonColor = AddonColor(
            r = (rgba shr 16) and 0xFF,
            g = (rgba shr 8) and 0xFF,
            b = rgba and 0xFF,
            a = (rgba shr 24) and 0xFF
        )
    }
}
