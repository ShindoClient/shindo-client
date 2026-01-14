package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.buffer.ScreenStencil
import net.minecraft.util.MathHelper
import org.lwjgl.nanovg.NanoVG
import java.awt.Color

class CompassMod : SimpleHUDMod(TranslateText.COMPASS, TranslateText.COMPASS_DESCRIPTION, LegacyIcon.MOD_COMPASS) {
    private val stencil = ScreenStencil()

    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private val design = Design.SIMPLE

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @Property(type = PropertyType.NUMBER, translate = TranslateText.WIDTH, min = 5.00, max = 45.00, step = 1.0, current = 180.0)
    private val widthSetting = 180

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager

        if (design == Design.SIMPLE) {
            this.draw()
        } else {
            nvg!!.setupAndDraw(Runnable {
                this.drawBackground(widthSetting.toFloat(), 29f)
            })
            stencil.wrap(
                Runnable { drawNanoVG() },
                this.getX().toFloat(),
                this.getY().toFloat(),
                this.getWidth().toFloat(),
                this.getHeight().toFloat(),
                6 * this.getScale()
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
            this.getFontColor()
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

    private fun renderMarker(x: Float, y: Float, color: Color?) {
        val nvg = getInstance().nanoVGManager
        val vg = nvg!!.getContext()
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

    public override fun getText(): String? {
        val s = "Direction: "
        var rotation = ((mc.thePlayer.rotationYawHead - 90) % 360).toDouble()

        if (rotation < 0) {
            rotation += 360.0
        }

        if (0 <= rotation && rotation < 22.5) {
            return s + "W"
        } else if (22.5 <= rotation && rotation < 67.5) {
            return s + "NW"
        } else if (67.5 <= rotation && rotation < 112.5) {
            return s + "N"
        } else if (112.5 <= rotation && rotation < 157.5) {
            return s + "NE"
        } else if (157.5 <= rotation && rotation < 202.5) {
            return s + "E"
        } else if (202.5 <= rotation && rotation < 247.5) {
            return s + "SE"
        } else if (247.5 <= rotation && rotation < 292.5) {
            return s + "S"
        } else if (292.5 <= rotation && rotation < 337.5) {
            return s + "SW"
        } else if (337.5 <= rotation && rotation < 360.0) {
            return s + "W"
        }

        return s + "Error"
    }

    public override fun getIcon(): String? {
        return if (iconSetting) LegacyIcon.COMPASS else null
    }

    private enum class Design(private val translate: TranslateText) : PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        FANCY(TranslateText.FANCY);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }
}


