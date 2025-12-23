package me.miki.shindo.management.notification

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.easing.EaseBackIn
import me.miki.shindo.utils.buffer.ScreenAlpha
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class Notification {
    private val title: String
    private val message: String
    private val type: NotificationType
    val timer: TimerUtils = TimerUtils()
    private val screenAlpha = ScreenAlpha()
    private lateinit var animation: Animation

    constructor(title: TranslateText, message: TranslateText, type: NotificationType) : this(
        title.text,
        message.text,
        type
    )

    constructor(title: String, message: String, type: NotificationType) {
        this.title = title
        this.message = message
        this.type = type
    }

    constructor(title: TranslateText, message: String, type: NotificationType) : this(title.text, message, type)

    fun draw() {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return
        screenAlpha.wrap(Runnable { drawNanoVG(nvg) }, animation.getValueFloat())
    }

    private fun drawNanoVG(nvg: NanoVGManager) {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val instance = Shindo.getInstance()
        val currentColor: AccentColor = instance.colorManager.currentColor

        val titleWidth = nvg.getTextWidth(title, 9.6f, Fonts.MEDIUM)
        val messageWidth = nvg.getTextWidth(message, 7.6f, Fonts.REGULAR)
        val maxWidth = maxOf(titleWidth, messageWidth) + 31f
        val margin = 8f
        val height = 29f

        val corner = InternalSettingsMod.getInstance().notificationCorner
        val x = when (corner) {
            InternalSettingsMod.NotificationCorner.TOP_LEFT,
            InternalSettingsMod.NotificationCorner.BOTTOM_LEFT -> margin
            InternalSettingsMod.NotificationCorner.TOP_RIGHT,
            InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT -> sr.scaledWidth - maxWidth - margin
        }

        val y = when (corner) {
            InternalSettingsMod.NotificationCorner.TOP_LEFT,
            InternalSettingsMod.NotificationCorner.TOP_RIGHT -> margin
            InternalSettingsMod.NotificationCorner.BOTTOM_LEFT,
            InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT -> sr.scaledHeight - height - margin
        }

        if (timer.delay(3000)) {
            animation.setDirection(Direction.BACKWARDS)
        }

        nvg.save()
        val slide = animation.getValueFloat()
        val slideOffset = 160f
        val slideX = when (corner) {
            InternalSettingsMod.NotificationCorner.TOP_LEFT,
            InternalSettingsMod.NotificationCorner.BOTTOM_LEFT -> -slideOffset + (slide * slideOffset)
            InternalSettingsMod.NotificationCorner.TOP_RIGHT,
            InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT -> slideOffset - (slide * slideOffset)
        }
        nvg.translate(slideX, 0f)

        nvg.drawShadow(x, y, maxWidth, height, 6f)
        nvg.drawGradientRoundedRect(
            x,
            y,
            maxWidth,
            height,
            6f,
            ColorUtils.applyAlpha(currentColor.color1, 220),
            ColorUtils.applyAlpha(currentColor.color2, 220)
        )
        nvg.drawText(type.icon, x + 5f, y + 6f, Color.WHITE, 17f, Fonts.LEGACYICON)
        nvg.drawText(title, x + 26f, y + 6f, Color.white, 9.6f, Fonts.MEDIUM)
        nvg.drawText(message, x + 26f, y + 17.5f, Color.WHITE, 7.5f, Fonts.REGULAR)

        nvg.restore()
    }

    fun show() {
        animation = EaseBackIn(300, 1.0, 0F)
        animation.setDirection(Direction.FORWARDS)
        animation.reset()
        timer.reset()
    }

    fun isShown(): Boolean = !animation.isDone(Direction.BACKWARDS)

    fun getAnimation(): Animation = animation
}
