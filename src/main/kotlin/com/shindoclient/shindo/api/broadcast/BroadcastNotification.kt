package com.shindoclient.shindo.api.broadcast

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.color.AccentColor
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.notification.NotificationType
import com.shindoclient.shindo.ui.animation.v2.Animation
import com.shindoclient.shindo.ui.animation.v2.Direction
import com.shindoclient.shindo.ui.animation.v2.easing.EaseBackIn
import com.shindoclient.shindo.ui.animation.v2.screen.ScreenAlpha
import com.shindoclient.shindo.utils.ColorUtils
import com.shindoclient.shindo.utils.TimerUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class BroadcastNotification(
    private val title: String,
    private val message: String,
    private val type: NotificationType,
) {
    private val timer = TimerUtils()
    private val screenAlpha = ScreenAlpha()
    private lateinit var animation: Animation

    fun show() {
        animation = EaseBackIn(320, 1.0, 0F)
        animation.setDirection(Direction.FORWARDS)
        animation.reset()
        timer.reset()
    }

    fun isShown(): Boolean = !animation.isDone(Direction.BACKWARDS)

    fun draw() {
        val nvg = Shindo.getInstance().nanoVGManager ?: return
        screenAlpha.wrap(Runnable { drawNanoVG(nvg) }, animation.getValueFloat())
    }

    private fun drawNanoVG(nvg: NanoVGManager) {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val currentColor: AccentColor = Shindo.getInstance().getColorManager().getCurrentColor()

        val titleWidth = nvg.getTextWidth(title, 10.4f, Fonts.MEDIUM)
        val messageWidth = nvg.getTextWidth(message, 8.2f, Fonts.REGULAR)
        val maxWidth = maxOf(titleWidth, messageWidth) + 36f
        val height = 34f
        val margin = 12f
        val x = (sr.scaledWidth / 2f) - (maxWidth / 2f)

        if (timer.delay(5000)) {
            animation.setDirection(Direction.BACKWARDS)
        }

        nvg.save()
        val slide = animation.getValueFloat()
        val slideOffset = 22f
        nvg.translate(0f, -slideOffset + slide * slideOffset)
        nvg.drawShadow(x, margin, maxWidth, height, 7f)
        nvg.drawGradientRoundedRect(
            x,
            margin,
            maxWidth,
            height,
            7f,
            ColorUtils.applyAlpha(currentColor.getColor1(), 230),
            ColorUtils.applyAlpha(currentColor.getColor2(), 230),
        )
        nvg.drawText(type.icon, x + 7f, margin + 7f, Color.WHITE, 18f, Fonts.LUCIDE)
        nvg.drawText(title, x + 28f, margin + 7f, Color.WHITE, 10.4f, Fonts.MEDIUM)
        nvg.drawText(message, x + 28f, margin + 19.5f, Color.WHITE, 8.2f, Fonts.REGULAR)
        nvg.restore()
    }
}
