package me.miki.shindo.management.notification

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.SmoothStepAnimation
import me.miki.shindo.ui.animation.v2.screen.ScreenAlpha
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class Notification {
    private val title: String
    private val message: String
    private val type: NotificationType
    val timer: TimerUtils = TimerUtils()
    private val screenAlpha = ScreenAlpha()
    private lateinit var animation: Animation

    constructor(title: String, message: String, type: NotificationType) {
        this.title = title
        this.message = message
        this.type = type
    }

    constructor(title: TranslateText, message: TranslateText, type: NotificationType) : this(
        title.getText(),
        message.getText(),
        type,
    )

    constructor(title: TranslateText, message: String, type: NotificationType) : this(title.getText(), message, type)

    constructor(title: String, message: TranslateText, type: NotificationType) : this(title, message.getText(), type)

    fun draw() {
        if (!::animation.isInitialized) {
            show()
        }
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return
        screenAlpha.wrap(Runnable { drawNanoVG(nvg) }, animation.getValueFloat())
    }

    private fun drawNanoVG(nvg: NanoVGManager) {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val instance = Shindo.getInstance()
        val palette = instance.getColorManager().getPalette()
        val currentColor: AccentColor = instance.getColorManager().getCurrentColor()
        val severity = resolveSeverityColors(type, currentColor)

        val titleWidth = nvg.getTextWidth(title, TITLE_SIZE, Fonts.MEDIUM)
        val messageWidth = nvg.getTextWidth(message, MESSAGE_SIZE, Fonts.REGULAR)
        val maxWidth = max(MIN_WIDTH, maxOf(titleWidth, messageWidth) + CONTENT_PADDING_LEFT + CONTENT_PADDING_RIGHT)
        val margin = NOTIFICATION_MARGIN
        val height = NOTIFICATION_HEIGHT

        val corner = InternalSettingsMod.instance.notificationCorner
        val x =
            when (corner) {
                InternalSettingsMod.NotificationCorner.TOP_LEFT,
                InternalSettingsMod.NotificationCorner.BOTTOM_LEFT,
                -> margin

                InternalSettingsMod.NotificationCorner.TOP_RIGHT,
                InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT,
                -> sr.scaledWidth - maxWidth - margin
            }

        val y =
            when (corner) {
                InternalSettingsMod.NotificationCorner.TOP_LEFT,
                InternalSettingsMod.NotificationCorner.TOP_RIGHT,
                -> margin

                InternalSettingsMod.NotificationCorner.BOTTOM_LEFT,
                InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT,
                -> sr.scaledHeight - height - margin
            }

        if (timer.delay(SHOW_MS)) {
            animation.setDirection(Direction.BACKWARDS)
        }

        nvg.save()
        val slide = animation.getValueFloat()
        val slideOffset = SLIDE_OFFSET
        val slideX =
            when (corner) {
                InternalSettingsMod.NotificationCorner.TOP_LEFT,
                InternalSettingsMod.NotificationCorner.BOTTOM_LEFT,
                -> -slideOffset + (slide * slideOffset)

                InternalSettingsMod.NotificationCorner.TOP_RIGHT,
                InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT,
                -> slideOffset - (slide * slideOffset)
            }
        nvg.translate(slideX, 0f)

        nvg.drawShadow(x, y, maxWidth, height, 8f)
        nvg.drawRoundedRect(
            x,
            y,
            maxWidth,
            height,
            CORNER_RADIUS,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 224),
        )
        nvg.drawGradientOutlineRoundedRect(
            x,
            y,
            maxWidth,
            height,
            CORNER_RADIUS,
            1.1f,
            ColorUtils.applyAlpha(severity.start, 140),
            ColorUtils.applyAlpha(severity.end, 120),
        )

        val iconBoxX = x + ICON_BOX_PADDING_X
        val iconBoxY = y + ICON_BOX_PADDING_Y
        nvg.drawRoundedRect(
            iconBoxX,
            iconBoxY,
            ICON_BOX_SIZE,
            ICON_BOX_SIZE,
            6f,
            ColorUtils.applyAlpha(severity.start, 180),
        )
        nvg.drawCenteredText(
            type.icon,
            iconBoxX + ICON_BOX_SIZE / 2f,
            iconBoxY + ICON_BOX_SIZE / 2f - 8f,
            Color.WHITE,
            16f,
            Fonts.LUCIDE,
        )

        val textX = x + CONTENT_PADDING_LEFT
        val textWidth = maxWidth - CONTENT_PADDING_LEFT - CONTENT_PADDING_RIGHT
        nvg.drawText(
            nvg.getLimitText(title, TITLE_SIZE, Fonts.MEDIUM, textWidth),
            textX,
            y + TITLE_Y,
            Color.WHITE,
            TITLE_SIZE,
            Fonts.MEDIUM,
        )
        nvg.drawText(
            nvg.getLimitText(message, MESSAGE_SIZE, Fonts.REGULAR, textWidth),
            textX,
            y + MESSAGE_Y,
            ColorUtils.applyAlpha(Color.WHITE, 230),
            MESSAGE_SIZE,
            Fonts.REGULAR,
        )

        val progressBaseX = x + CONTENT_PADDING_LEFT
        val progressBaseY = y + height - PROGRESS_BOTTOM_INSET
        val progressWidth = maxWidth - CONTENT_PADDING_LEFT - CONTENT_PADDING_RIGHT
        nvg.drawRoundedRect(
            progressBaseX,
            progressBaseY,
            progressWidth,
            PROGRESS_HEIGHT,
            PROGRESS_HEIGHT / 2f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 170),
        )
        val remainingProgress = 1f - min(1f, timer.elapsedTime / SHOW_MS.toFloat())
        nvg.drawRoundedRect(
            progressBaseX,
            progressBaseY,
            progressWidth * max(0f, remainingProgress),
            PROGRESS_HEIGHT,
            PROGRESS_HEIGHT / 2f,
            ColorUtils.applyAlpha(severity.start, 220),
        )

        nvg.restore()
    }

    fun show() {
        animation = SmoothStepAnimation(SLIDE_MS, 1.0)
        animation.setDirection(Direction.FORWARDS)
        animation.reset()
        timer.reset()
    }

    fun isShown(): Boolean = !::animation.isInitialized || !animation.isDone(Direction.BACKWARDS)

    fun getAnimation(): Animation = animation

    private data class SeverityColors(
        val start: Color,
        val end: Color,
    )

    private fun resolveSeverityColors(
        type: NotificationType,
        accent: AccentColor,
    ): SeverityColors =
        when (type) {
            NotificationType.INFO -> SeverityColors(Color(88, 178, 255), Color(74, 125, 255))
            NotificationType.WARNING -> SeverityColors(Color(255, 194, 82), Color(255, 143, 64))
            NotificationType.ERROR -> SeverityColors(Color(255, 115, 123), Color(220, 74, 90))
            NotificationType.SUCCESS -> SeverityColors(Color(104, 222, 132), Color(69, 194, 116))
            NotificationType.MUSIC -> SeverityColors(accent.getColor1(), accent.getColor2())
            NotificationType.WEBSOCKET -> SeverityColors(Color(88, 178, 255), Color(74, 125, 255))
        }

    private companion object {
        private const val SHOW_MS = 3000L
        private const val SLIDE_MS = 260
        private const val SLIDE_OFFSET = 170f

        private const val NOTIFICATION_MARGIN = 8f
        private const val NOTIFICATION_HEIGHT = 36f
        private const val CORNER_RADIUS = 7.5f
        private const val MIN_WIDTH = 168f

        private const val ICON_BOX_SIZE = 22f
        private const val ICON_BOX_PADDING_X = 7f
        private const val ICON_BOX_PADDING_Y = 7f
        private const val CONTENT_PADDING_LEFT = 36f
        private const val CONTENT_PADDING_RIGHT = 10f
        private const val TITLE_Y = 8f
        private const val MESSAGE_Y = 21f
        private const val TITLE_SIZE = 9.7f
        private const val MESSAGE_SIZE = 7.6f

        private const val PROGRESS_BOTTOM_INSET = 4.5f
        private const val PROGRESS_HEIGHT = 2f
    }
}
