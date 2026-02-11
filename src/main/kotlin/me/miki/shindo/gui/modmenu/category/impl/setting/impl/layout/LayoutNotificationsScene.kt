package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Mouse
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class LayoutNotificationsScene(parent: SettingsCategory) :
        LayoutAreaScene(
                parent,
                UILayoutArea.NOTIFICATIONS,
                TranslateText.SETTINGS_LAYOUT_SECTION_NOTIFICATION,
                TranslateText.SETTINGS_LAYOUT_NOTIFICATION_DESCRIPTION,
                LegacyIcon.BELL
        ) {

    private var previewDragActive = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var previewCardX = Float.NaN
    private var previewCardY = Float.NaN
    private var previewState: NotificationPreviewState? = null

    override val previewMaxHeight: Float = 188f
    override val previewHeightRatio: Float = 0.62f

    override fun renderLegacyPreview(): Boolean = true
    override fun showTypeSelector(): Boolean = false

    override fun drawPreview(
            nvg: NanoVGManager,
            palette: ColorPalette,
            accent: AccentColor,
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            mouseX: Int,
            mouseY: Int,
            partialTicks: Float
    ) {
        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170)
        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base)

        val pad = 12f
        val areaX = x + pad
        val areaY = y + pad
        val areaWidth = max(20f, width - pad * 2f)
        val areaHeight = max(20f, height - pad * 2f)
        val maxCardWidthByGap = max(72f, areaWidth - 40f)
        val maxCardHeightByGap = max(26f, areaHeight - 30f)
        val cardWidth = max(72f, min(min(128f, maxCardWidthByGap), areaWidth * 0.42f))
        val cardHeight = max(26f, min(min(36f, maxCardHeightByGap), areaHeight * 0.2f))
        val state = NotificationPreviewState(
                areaX,
                areaY,
                areaWidth,
                areaHeight,
                cardWidth,
                cardHeight,
                buildAnchorMap(areaX, areaY, areaWidth, areaHeight, cardWidth, cardHeight)
        )
        previewState = state

        drawAnchorGuides(nvg, palette, accent, state)

        if (previewDragActive && !Mouse.isButtonDown(0)) {
            finalizeDragSnap()
        }

        val selectedType = getSelectedType() ?: UILayoutType.NOTIFICATION_BOTTOM_RIGHT
        val target = if (previewDragActive) {
            val targetX = (mouseX - dragOffsetX).coerceIn(state.areaX, state.areaX + state.areaWidth - state.cardWidth)
            val targetY = (mouseY - dragOffsetY).coerceIn(state.areaY, state.areaY + state.areaHeight - state.cardHeight)
            NotificationPoint(targetX, targetY)
        } else {
            state.anchors[selectedType] ?: state.anchors[UILayoutType.NOTIFICATION_BOTTOM_RIGHT]!!
        }

        val smooth = if (previewDragActive) 0.45f else 0.2f
        previewCardX = if (previewCardX.isNaN()) target.x else previewCardX + (target.x - previewCardX) * smooth
        previewCardY = if (previewCardY.isNaN()) target.y else previewCardY + (target.y - previewCardY) * smooth

        drawNotificationPreviewCard(
                nvg,
                palette,
                accent,
                previewCardX,
                previewCardY,
                state.cardWidth,
                state.cardHeight
        )
    }

    private fun drawAnchorGuides(
            nvg: NanoVGManager,
            palette: ColorPalette,
            accent: AccentColor,
            state: NotificationPreviewState
    ) {
        nvg.drawRoundedRect(
                state.areaX,
                state.areaY,
                state.areaWidth,
                state.areaHeight,
                10f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 130)
        )

        for (anchor in state.anchors.values) {
            nvg.drawRoundedRect(
                    anchor.x,
                    anchor.y,
                    state.cardWidth,
                    state.cardHeight,
                    7f,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 128)
            )
            nvg.drawOutlineRoundedRect(
                    anchor.x,
                    anchor.y,
                    state.cardWidth,
                    state.cardHeight,
                    7f,
                    1f,
                    ColorUtils.applyAlpha(accent.getColor2(), 95)
            )
        }
    }

    private fun drawNotificationPreviewCard(
            nvg: NanoVGManager,
            palette: ColorPalette,
            accent: AccentColor,
            cardX: Float,
            cardY: Float,
            cardWidth: Float,
            cardHeight: Float
    ) {
        val accentStart = ColorUtils.applyAlpha(accent.getColor1(), 200)
        val accentEnd = ColorUtils.applyAlpha(accent.getColor2(), 200)
        val textColor = palette.getFontColor(ColorType.NORMAL)
        nvg.drawRoundedRect(
                cardX,
                cardY,
                cardWidth,
                cardHeight,
                7f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210)
        )
        nvg.drawGradientRoundedRect(cardX, cardY, cardWidth, cardHeight, 7f, accentStart, accentEnd)
        nvg.drawText(LegacyIcon.BELL, cardX + 7f, cardY + 6f, textColor, 11f, Fonts.LEGACYICON)
        nvg.drawRoundedRect(cardX + 22f, cardY + 7f, cardWidth - 28f, 6f, 2f, ColorUtils.applyAlpha(textColor, 200))
        nvg.drawRoundedRect(cardX + 22f, cardY + 16f, cardWidth - 38f, 5f, 2f, ColorUtils.applyAlpha(textColor, 160))
    }

    override fun drawTypeCardPreview(
            nvg: NanoVGManager,
            palette: ColorPalette,
            accent: AccentColor,
            type: UILayoutType,
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            selected: Boolean,
            hovered: Boolean
    ) {
        nvg.drawRoundedRect(
                x,
                y,
                width,
                height,
                4f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (selected) 190 else 162)
        )

        val pad = 4.5f
        val areaW = max(18f, width - pad * 2f)
        val areaH = max(12f, height - pad * 2f)
        val cardW = max(12f, areaW * 0.56f)
        val cardH = max(5f, areaH * 0.34f)
        val targetX = when (type) {
            UILayoutType.NOTIFICATION_TOP_LEFT, UILayoutType.NOTIFICATION_BOTTOM_LEFT -> x + pad
            UILayoutType.NOTIFICATION_TOP_RIGHT, UILayoutType.NOTIFICATION_BOTTOM_RIGHT -> x + pad + areaW - cardW
            else -> x + pad
        }
        val targetY = when (type) {
            UILayoutType.NOTIFICATION_TOP_LEFT, UILayoutType.NOTIFICATION_TOP_RIGHT -> y + pad
            UILayoutType.NOTIFICATION_BOTTOM_LEFT, UILayoutType.NOTIFICATION_BOTTOM_RIGHT -> y + pad + areaH - cardH
            else -> y + pad + areaH - cardH
        }

        nvg.drawRoundedRect(x + pad, y + pad, areaW, areaH, 3f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 155))
        nvg.drawRoundedRect(targetX, targetY, cardW, cardH, 2.5f, ColorUtils.applyAlpha(accent.getColor1(), if (selected) 205 else 170))
        nvg.drawRoundedRect(
                targetX + 2f,
                targetY + cardH / 2f - 1.2f,
                max(6f, cardW - 4f),
                2.4f,
                1.2f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), if (hovered || selected) 220 else 185)
        )
    }

    override fun mouseClickedExtra(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return
        val state = previewState ?: return
        if (!MouseUtils.isInside(mouseX, mouseY, previewCardX, previewCardY, state.cardWidth, state.cardHeight)) return
        previewDragActive = true
        dragOffsetX = mouseX - previewCardX
        dragOffsetY = mouseY - previewCardY
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && previewDragActive) {
            finalizeDragSnap()
        }
    }

    private fun finalizeDragSnap() {
        val state = previewState ?: return
        val centerX = previewCardX + state.cardWidth / 2f
        val centerY = previewCardY + state.cardHeight / 2f
        val nearestType = state.anchors.minBy { (_, point) ->
            (point.x + state.cardWidth / 2f - centerX).pow(2) + (point.y + state.cardHeight / 2f - centerY).pow(2)
        }?.key ?: UILayoutType.NOTIFICATION_BOTTOM_RIGHT

        previewDragActive = false
        if (nearestType != getSelectedType()) {
            selectType(nearestType)
        }
    }

    private fun buildAnchorMap(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            cardWidth: Float,
            cardHeight: Float
    ): Map<UILayoutType, NotificationPoint> {
        val left = x
        val right = x + width - cardWidth
        val top = y
        val bottom = y + height - cardHeight
        return mapOf(
                UILayoutType.NOTIFICATION_TOP_LEFT to NotificationPoint(left, top),
                UILayoutType.NOTIFICATION_TOP_RIGHT to NotificationPoint(right, top),
                UILayoutType.NOTIFICATION_BOTTOM_LEFT to NotificationPoint(left, bottom),
                UILayoutType.NOTIFICATION_BOTTOM_RIGHT to NotificationPoint(right, bottom)
        )
    }

    private data class NotificationPoint(
            val x: Float,
            val y: Float
    )

    private data class NotificationPreviewState(
            val areaX: Float,
            val areaY: Float,
            val areaWidth: Float,
            val areaHeight: Float,
            val cardWidth: Float,
            val cardHeight: Float,
            val anchors: Map<UILayoutType, NotificationPoint>
    )
}
