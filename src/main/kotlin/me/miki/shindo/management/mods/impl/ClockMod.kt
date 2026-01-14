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
import org.lwjgl.nanovg.NanoVG
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class ClockMod : SimpleHUDMod(TranslateText.CLOCK, TranslateText.CLOCK_DESCRIPTION, LegacyIcon.MOD_CLOCK) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private val design = Design.SIMPLE

    private val df: DateFormat = SimpleDateFormat("HH:mm a", Locale.US)

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        if (design == Design.SIMPLE) {
            this.draw()
        } else {
            getInstance().nanoVGManager!!.setupAndDraw(Runnable { this.drawNanoVG() })
        }
    }

    public override fun getText(): String? {
        return df.format(Calendar.getInstance().getTime())
    }

    public override fun getIcon(): String? {
        return if (iconSetting) LegacyIcon.CLOCK else null
    }

    private enum class Design(private val translate: TranslateText) : PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        FANCY(TranslateText.FANCY);


        override fun getTranslate(): TranslateText {
            return translate
        }
    }

    private fun drawNanoVG() {
        val instance = getInstance()
        val nvg = instance.nanoVGManager

        val scale = this.getScale()
        val size = 128
        val center = (size / 2).toFloat()
        val scaledSize = 128 * scale
        val radius = center * this.getScale()
        val numbers = arrayOf<String?>("3", "6", "9", "12")
        var lineLength = 4 * scale
        var index = 0
        val c = Calendar.getInstance()

        this.drawBackground(size.toFloat(), size.toFloat(), radius)

        nvg!!.drawCircle(this.getX() + (scaledSize / 2), this.getY() + (scaledSize / 2), 2 * scale, this.getFontColor())

        for (i in 0..3) {
            val angle = Math.toRadians(360.0 / 4.0 * i).toFloat()

            val textX = center + ((radius / scale) - 6f) * cos(angle.toDouble()).toFloat()
            val textY = center + ((radius / scale) - 6f) * sin(angle.toDouble()).toFloat()

            this.drawCenteredText(numbers[i]!!, textX + 0.5f, textY - 3, 8f, getHudFont(2))
        }

        val nvgColor = nvg.getColor(this.getFontColor())
        val nvgColor2 = nvg.getColor(this.getFontColor(180))

        for (i in 0..11) {
            if (i == index * 3) {
                index++
                continue
            }

            val angle = Math.toRadians(360.0 / 12.0 * i).toFloat()

            val startX = (center * scale) + (radius - lineLength - 4) * cos(angle.toDouble()).toFloat()
            val startY = (center * scale) + (radius - lineLength - 4) * sin(angle.toDouble()).toFloat()
            val endX = (center * scale) + (radius - 4) * cos(angle.toDouble()).toFloat()
            val endY = (center * scale) + (radius - 4) * sin(angle.toDouble()).toFloat()

            NanoVG.nvgBeginPath(nvg.getContext())
            NanoVG.nvgMoveTo(nvg.getContext(), this.getX() + startX, this.getY() + startY)
            NanoVG.nvgLineTo(nvg.getContext(), this.getX() + endX, this.getY() + endY)
            NanoVG.nvgStrokeColor(nvg.getContext(), nvgColor)
            NanoVG.nvgStroke(nvg.getContext())
        }

        index = 0
        lineLength = 2 * scale

        for (i in 0..59) {
            if (i == index * 5) {
                index++
                continue
            }

            val angle = Math.toRadians(360.0 / 60.0 * i).toFloat()

            val startX = (center * scale) + (radius - lineLength - 6) * cos(angle.toDouble()).toFloat()
            val startY = (center * scale) + (radius - lineLength - 6) * sin(angle.toDouble()).toFloat()
            val endX = (center * scale) + (radius - 6) * cos(angle.toDouble()).toFloat()
            val endY = (center * scale) + (radius - 6) * sin(angle.toDouble()).toFloat()

            NanoVG.nvgBeginPath(nvg.getContext())
            NanoVG.nvgMoveTo(nvg.getContext(), this.getX() + startX, this.getY() + startY)
            NanoVG.nvgLineTo(nvg.getContext(), this.getX() + endX, this.getY() + endY)
            NanoVG.nvgStrokeColor(nvg.getContext(), nvgColor2)
            NanoVG.nvgStrokeWidth(nvg.getContext(), 0.5f)
            NanoVG.nvgStroke(nvg.getContext())
        }

        val secondAngle =
            Math.toRadians(360.0 / 60.0 * c.get(Calendar.SECOND)).toFloat() - Math.toRadians(90.0).toFloat()

        val secondX = (center * scale) + (radius - (14f * scale)) * cos(secondAngle.toDouble()).toFloat()
        val secondY = (center * scale) + (radius - (14f * scale)) * sin(secondAngle.toDouble()).toFloat()

        val minuteAngle = Math.toRadians(360.0 / 60.0 * c.get(Calendar.MINUTE)).toFloat()
        val hourAngle = Math.toRadians(360.0 / 12.0 * (c.get(Calendar.HOUR) + c.get(Calendar.MINUTE) / 60.0)).toFloat()

        nvg.drawCircle(this.getX() + secondX, this.getY() + secondY, 1.3f * scale, this.getFontColor())

        nvg.save()
        nvg.rotate(
            this.getX().toFloat(),
            this.getY().toFloat(),
            scaledSize,
            scaledSize,
            minuteAngle - Math.toRadians(90.0).toFloat()
        )
        nvg.drawRoundedRect(
            this.getX() + (scaledSize / 2) - (6 * scale),
            this.getY() + (scaledSize / 2) - scale,
            48 * scale,
            2 * scale,
            scale,
            this.getFontColor()
        )
        nvg.restore()

        nvg.save()
        nvg.rotate(
            this.getX().toFloat(),
            this.getY().toFloat(),
            scaledSize,
            scaledSize,
            hourAngle - Math.toRadians(90.0).toFloat()
        )
        nvg.drawRoundedRect(
            this.getX() + (scaledSize / 2) - (6 * scale),
            this.getY() + (scaledSize / 2) - scale,
            38 * scale,
            2 * scale,
            scale,
            this.getFontColor()
        )
        nvg.restore()

        this.setWidth(size)
        this.setHeight(size)
    }
}


