package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.ui.animation.v2.screen.ScreenStencil
import net.minecraft.util.MathHelper
import org.lwjgl.nanovg.NanoVG
import java.awt.Color

class CompassMod : SimpleHUDMod(TranslateText.COMPASS, TranslateText.COMPASS_DESCRIPTION, Shinconic.MOD_COMPASS) {
    private val stencil = ScreenStencil()

    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private val design = Design.SIMPLE

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.WIDTH,
        min = 50.0,
        max = 450.0,
        step = 1.0,
        current = 180.0,
    )
    private val widthSetting = 180

    @EventTarget
    fun onRenderNVG(event: EventNVG?) {
        if (design == Design.SIMPLE) {
            draw()
        } else {
            this.drawBackground(widthSetting.toFloat(), 29f)
        }
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        if (design == Design.FANCY) {
            stencil.wrap(
                Runnable { drawNanoVG() },
                getX().toFloat(),
                getY().toFloat(),
                getWidth().toFloat(),
                getHeight().toFloat(),
                6 * getScale(),
            )
        }
    }

    private fun drawNanoVG() {
        var angle = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw).toInt() * -1 - 360
        var angle2 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw).toInt() * -1 - 360
        var angle3 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw).toInt() * -1 - 360
        var angle4 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw).toInt() * -1 - 360
        var angle5 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw).toInt() * -1 - 360
        var angle6 = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw).toInt() * -1 - 360
        val width = widthSetting

        this.renderMarker(
            this.getX() + ((width / 2) * this.getScale()),
            this.getY() + (2.5f * this.getScale()),
            this.getFontColor(),
        )

        for (i in 0..2) {
            run {
                var d = 0.0
                while (d <= 1.5) {
                    var s = "W"

                    if (d == 0.0) {
                        s = "S"
                    }

                    if (d == 1.0) {
                        s = "N"
                    }

                    if (d == 1.5) {
                        s = "E"
                    }

                    this.drawRect((width / 2) + angle - 2f, 8f, 0.8f, 9f)

                    this.drawRect((width / 2) + angle + 12f, 8f, 0.8f, 6f)
                    this.drawRect((width / 2) + angle + 26f, 8f, 0.8f, 6f)

                    this.drawRect((width / 2) + angle - 16f, 8f, 0.8f, 6f)
                    this.drawRect((width / 2) + angle - 30f, 8f, 0.8f, 6f)

                    this.drawCenteredText(s, (width / 2) + angle - 1.5f, 19f, 8.5f, getHudFont(2))

                    angle += 90
                    d += 0.5
                }
            }

            run {
                var d = 0.0
                while (d <= 1.5) {
                    var s = "NW"

                    if (d == 0.0) {
                        s = "SW"
                    }

                    if (d == 1.0) {
                        s = "NE"
                    }

                    if (d == 1.5) {
                        s = "SE"
                    }

                    this.drawCenteredText(s, (width / 2) + angle2 + 43f, 8.5f, 6.8f, getHudFont(1))

                    angle2 += 90
                    d += 0.5
                }
            }

            run {
                var d = 0.0
                while (d <= 1.5) {
                    var s = "105"

                    if (d == 0.0) {
                        s = "15"
                    }

                    if (d == 1.0) {
                        s = "195"
                    }

                    if (d == 1.5) {
                        s = "285"
                    }

                    this.drawCenteredText(s, (width / 2) + angle3 + 13f, 17f, 5.4f, getHudFont(1))

                    angle3 += 90
                    d += 0.5
                }
            }

            run {
                var d = 0.0
                while (d <= 1.5) {
                    var s = "120"

                    if (d == 0.0) {
                        s = "30"
                    }

                    if (d == 1.0) {
                        s = "210"
                    }

                    if (d == 1.5) {
                        s = "300"
                    }

                    this.drawCenteredText(s, (width / 2) + angle4 + 27f, 17f, 5.4f, getHudFont(1))

                    angle4 += 90
                    d += 0.5
                }
            }

            run {
                var d = 0.0
                while (d <= 1.5) {
                    var s = "150"

                    if (d == 0.0) {
                        s = "60"
                    }

                    if (d == 1.0) {
                        s = "240"
                    }

                    if (d == 1.5) {
                        s = "300"
                    }

                    this.drawCenteredText(s, (width / 2) + angle5 + 60.5f, 17f, 5.4f, getHudFont(1))

                    angle5 += 90
                    d += 0.5
                }
            }

            var d = 0.0
            while (d <= 1.5) {
                var s = "165"

                if (d == 0.0) {
                    s = "70"
                }

                if (d == 1.0) {
                    s = "255"
                }

                if (d == 1.5) {
                    s = "345"
                }

                this.drawCenteredText(s, (width / 2) + angle6 + 74.5f, 17f, 5.4f, getHudFont(1))

                angle6 += 90
                d += 0.5
            }
        }

        this.setWidth(width)
        this.setHeight(29)
    }

    private fun renderMarker(
        x: Float,
        y: Float,
        color: Color,
    ) {
        val nvg = getInstance().nanoVGManager
        val vg = nvg.getContext()
        val nvgColor = nvg.getColor(color)
        val scale = this.getScale()

        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgMoveTo(vg, x, y + (4 * scale))
        NanoVG.nvgLineTo(vg, x + (4 * scale), y)
        NanoVG.nvgLineTo(vg, x - (4 * scale), y)
        NanoVG.nvgClosePath(vg)

        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgFill(vg)
    }

    override fun getText(): String {
        val s = "Direction: "
        var rotation = ((mc.thePlayer.rotationYawHead - 90) % 360).toDouble()

        if (rotation < 0) {
            rotation += 360.0
        }

        when (rotation) {
            in 0.0..<22.5 -> {
                return s + "W"
            }

            in 22.5..<67.5 -> {
                return s + "NW"
            }

            in 67.5..<112.5 -> {
                return s + "N"
            }

            in 112.5..<157.5 -> {
                return s + "NE"
            }

            in 157.5..<202.5 -> {
                return s + "E"
            }

            in 202.5..<247.5 -> {
                return s + "SE"
            }

            in 247.5..<292.5 -> {
                return s + "S"
            }

            in 292.5..<337.5 -> {
                return s + "SW"
            }

            in 337.5..<360.0 -> {
                return s + "W"
            }

            else -> {
                return s + "Error"
            }
        }
    }

    override fun getIcon(): String? = if (iconSetting) Lucide.COMPASS else null

    private enum class Design(
        private val translate: TranslateText,
    ) : PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        FANCY(TranslateText.FANCY),
        ;

        override fun getTranslate(): TranslateText = translate
    }
}
