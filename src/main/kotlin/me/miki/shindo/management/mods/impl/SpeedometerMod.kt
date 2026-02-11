package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ColorUtils.resetColor
import me.miki.shindo.utils.ColorUtils.setColor
import me.miki.shindo.utils.GlUtils.startTranslate
import me.miki.shindo.utils.GlUtils.stopTranslate
import me.miki.shindo.utils.PlayerUtils.getSpeed
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.text.DecimalFormat

class SpeedometerMod :
    SimpleHUDMod(TranslateText.SPEEDOMETER, TranslateText.SPEEDOMETER_DESCRIPTION, LegacyIcon.MOD_SPEEDOMETER) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val showIcon = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.GRAPH)
    private val showGraph = true

    private val speedCount = 200
    private val speeds = DoubleArray(speedCount)
    private val speedFormat = DecimalFormat("0.00")
    private var lastUpdate: Long = 0

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager

        if (showGraph) {
            nvg!!.setupAndDraw(Runnable { this.drawNanoVG() })

            startTranslate((this.getX() - 3).toFloat(), this.getY().toFloat())

            GL11.glLineWidth(1.5f)
            if (!mc.isGamePaused && (lastUpdate == -1L || (System.currentTimeMillis() - lastUpdate) > 30)) {
                addSpeed((getSpeed() / 5).toDouble())
                lastUpdate = System.currentTimeMillis()
            }

            GlStateManager.enableBlend()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glShadeModel(GL11.GL_SMOOTH)

            GL11.glBegin(GL11.GL_LINE_STRIP)

            setColor(this.getFontColor().rgb)

            for (i in 0 until speedCount) {
                GL11.glVertex2d(
                    (this.getWidth() + 1) * i / speedCount.toDouble() + 3,
                    this.getHeight() - (speeds[i] * (16)) - 10
                )
            }

            GL11.glEnd()
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            resetColor()

            stopTranslate()

            this.setWidth(155)
            this.setHeight(100)
        } else {
            this.draw()
        }
    }

    private fun drawNanoVG() {
        this.drawBackground(155f, 100f)
        this.drawRect(0f, 17.5f, 155f, 1f)
        this.drawText("Speed: " + speedFormat.format(getSpeed().toDouble()) + " m/s", 5.5f, 6f, 10.5f, getHudFont(2))
    }

    override fun getText(): String {
        return "Speed: " + speedFormat.format(getSpeed().toDouble()) + " m/s"
    }

    override fun getIcon(): String? {
        return if (showIcon) LegacyIcon.ACTIVITY else null
    }

    private fun addSpeed(speed: Double) {
        var speed = speed
        if (speed > 3.8) {
            speed = 3.8
        }

        System.arraycopy(speeds, 1, speeds, 0, speedCount - 1)
        speeds[speedCount - 1] = speed
    }
}



