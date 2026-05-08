package me.miki.shindo.gui.modmenu.v2.style

import net.minecraft.client.gui.ScaledResolution
import kotlin.math.max
import kotlin.math.min

object ModMenuResponsiveLayout {

    data class Bounds(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    fun resolve(sr: ScaledResolution): Bounds {
        val safeWidth = max(1, sr.scaledWidth - (SAFE_MARGIN * 2))
        val safeHeight = max(1, sr.scaledHeight - (SAFE_MARGIN * 2))

        val width = min(ModMenuStyle.DEFAULT_WIDTH, safeWidth).coerceAtLeast(min(MIN_WIDTH, safeWidth))
        val height = min(ModMenuStyle.DEFAULT_HEIGHT, safeHeight).coerceAtLeast(min(MIN_HEIGHT, safeHeight))

        val x = (sr.scaledWidth - width) / 2
        val y = (sr.scaledHeight - height) / 2

        return Bounds(x = x, y = y, width = width, height = height)
    }

    private const val SAFE_MARGIN = 8
    private const val MIN_WIDTH = 420
    private const val MIN_HEIGHT = 250
}

