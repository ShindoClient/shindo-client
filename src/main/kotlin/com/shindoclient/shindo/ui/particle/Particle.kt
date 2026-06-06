package com.shindoclient.shindo.ui.particle

import com.shindoclient.shindo.utils.TimerUtils
import com.shindoclient.shindo.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import kotlin.random.Random

class Particle(
    x: Int,
    y: Int,
) {
    private val mc: Minecraft = Minecraft.getMinecraft()

    val size: Float = genRandom() + 0.4f
    val ySpeed: Float = Random.nextInt(5).toFloat()
    val xSpeed: Float = Random.nextInt(5).toFloat()
    val timer: TimerUtils = TimerUtils()

    var x: Float = x.toFloat()
    var y: Float = y.toFloat()
    var height: Int = 0
    var width: Int = 0

    private fun lint1(f: Float): Float = 1.02f * (1.0f - f) + f

    private fun lint2(f: Float): Float = 1.02f + f * (1.0f - 1.02f)

    fun connect(
        x: Float,
        y: Float,
    ) {
        RenderUtils.connectPoints(this.x, this.y, x, y)
    }

    fun interpolation() {
        for (n in 0..64) {
            val f = n / 64.0f
            val p1 = lint1(f)
            val p2 = lint2(f)

            if (p1 != p2) {
                y -= f
                x -= f
            }
        }
    }

    fun fall() {
        val sr = ScaledResolution(mc)

        y += ySpeed
        x += xSpeed

        if (y > mc.displayHeight) y = 1f
        if (x > mc.displayWidth) x = 1f
        if (x < 1f) x = sr.scaledWidth.toFloat()
        if (y < 1f) y = sr.scaledHeight.toFloat()
    }

    private fun genRandom(): Float = (0.3f + Math.random() * (0.6f - 0.3f + 1.0)).toFloat()
}
