package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Mouse
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Notifications layout scene.
 *
 * This scene keeps the original interactive concept: the user drags
 * a notification preview and it snaps to one of the four corner anchors.
 */
class LayoutNotificationsScene(parent: SettingsCategory) : LayoutAreaScene(
    parent,
    UILayoutArea.NOTIFICATIONS,
    TranslateText.SETTINGS_LAYOUT_SECTION_NOTIFICATION,
    TranslateText.SETTINGS_LAYOUT_NOTIFICATION_DESCRIPTION,
    LegacyIcon.BELL
) {

    private var dragActive = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var highlightedAnchor: UILayoutType? = null

    private var cardX = Float.NaN
    private var cardY = Float.NaN
    private var previewState: NotificationPreviewState? = null

    override fun showTypeSelector(): Boolean {
        return false
    }

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
        LayoutSceneRenderer.drawPreviewSurface(nvg, palette, x, y, width, height, LayoutSceneStyle.PREVIEW_RADIUS)

        val pad = 12f
        val areaX = x + pad
        val areaY = y + pad
        val areaWidth = max(22f, width - pad * 2f)
        val areaHeight = max(22f, height - pad * 2f)

        val cardWidth = max(72f, min(132f, areaWidth * 0.43f))
        val cardHeight = max(24f, min(38f, areaHeight * 0.22f))

        val anchors = buildAnchorMap(areaX, areaY, areaWidth, areaHeight, cardWidth, cardHeight)
        previewState = NotificationPreviewState(areaX, areaY, areaWidth, areaHeight, cardWidth, cardHeight, anchors)

        if (dragActive && !Mouse.isButtonDown(0)) {
            finalizeDragSnap()
        }

        val selected = getSelectedType() ?: UILayoutType.NOTIFICATION_BOTTOM_RIGHT
        val target = if (dragActive) {
            val rawX = (mouseX - dragOffsetX).coerceIn(areaX, areaX + areaWidth - cardWidth)
            val rawY = (mouseY - dragOffsetY).coerceIn(areaY, areaY + areaHeight - cardHeight)
            val nearest = findNearestAnchorType(rawX + cardWidth / 2f, rawY + cardHeight / 2f, anchors, cardWidth, cardHeight)
            highlightedAnchor = nearest
            val nearestPoint = anchors[nearest] ?: NotificationPoint(rawX, rawY)
            val dx = nearestPoint.x - rawX
            val dy = nearestPoint.y - rawY
            val distance = sqrt(dx * dx + dy * dy)
            val attraction = ((MAGNET_RADIUS - distance) / MAGNET_RADIUS).coerceIn(0f, 1f)
            val easedAttraction = attraction * attraction * 0.7f
            NotificationPoint(
                rawX + dx * easedAttraction,
                rawY + dy * easedAttraction
            )
        } else {
            highlightedAnchor = selected
            anchors[selected] ?: anchors[UILayoutType.NOTIFICATION_BOTTOM_RIGHT]!!
        }

        val smooth = if (dragActive) 0.35f else 0.22f
        cardX = if (cardX.isNaN()) target.x else cardX + (target.x - cardX) * smooth
        cardY = if (cardY.isNaN()) target.y else cardY + (target.y - cardY) * smooth

        drawAnchorBoard(
            nvg,
            palette,
            accent,
            areaX,
            areaY,
            areaWidth,
            areaHeight,
            cardWidth,
            cardHeight,
            anchors,
            selected,
            highlightedAnchor
        )

        drawNotificationCard(nvg, palette, accent, cardX, cardY, cardWidth, cardHeight, dragActive)
    }

    /**
     * Draws the board and all candidate anchors.
     */
    private fun drawAnchorBoard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        areaX: Float,
        areaY: Float,
        areaWidth: Float,
        areaHeight: Float,
        cardWidth: Float,
        cardHeight: Float,
        anchors: Map<UILayoutType, NotificationPoint>,
        selectedType: UILayoutType,
        highlightedType: UILayoutType?
    ) {
        nvg.drawRoundedRect(
            areaX,
            areaY,
            areaWidth,
            areaHeight,
            9f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 128)
        )

        nvg.drawLine(
            areaX + areaWidth / 2f,
            areaY + 6f,
            areaX + areaWidth / 2f,
            areaY + areaHeight - 6f,
            1f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 78)
        )
        nvg.drawLine(
            areaX + 6f,
            areaY + areaHeight / 2f,
            areaX + areaWidth - 6f,
            areaY + areaHeight / 2f,
            1f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 78)
        )

        val pulse = ((sin(System.currentTimeMillis().toDouble() * 0.01) + 1.0) * 0.5).toFloat()
        val iterator = anchors.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val type = entry.key
            val anchor = entry.value
            val selected = type == selectedType
            val highlighted = type == highlightedType
            val fillAlpha = when {
                selected -> 156
                highlighted -> 142
                else -> 112
            }
            val borderAlpha = when {
                highlighted -> (130f + pulse * 55f).toInt()
                selected -> 168
                else -> 102
            }
            val borderWidth = if (highlighted) 1.35f else 1f

            if (highlighted) {
                nvg.drawRoundedRect(
                    anchor.x - 1f,
                    anchor.y - 1f,
                    cardWidth + 2f,
                    cardHeight + 2f,
                    7f,
                    ColorUtils.applyAlpha(accent.getColor1(), (38f + pulse * 22f).toInt())
                )
            }

            nvg.drawRoundedRect(
                anchor.x,
                anchor.y,
                cardWidth,
                cardHeight,
                6f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), fillAlpha)
            )
            nvg.drawOutlineRoundedRect(
                anchor.x,
                anchor.y,
                cardWidth,
                cardHeight,
                6f,
                borderWidth,
                ColorUtils.applyAlpha(if (selected || highlighted) accent.getColor1() else accent.getColor2(), borderAlpha)
            )
            nvg.drawRoundedRect(
                anchor.x + 3f,
                anchor.y + 3f,
                4f,
                4f,
                2f,
                ColorUtils.applyAlpha(accent.getColor1(), if (selected || highlighted) 184 else 120)
            )
        }
    }

    /**
     * Draws the live draggable notification card.
     */
    private fun drawNotificationCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        dragging: Boolean
    ) {
        nvg.drawShadow(x, y, width, height, 7f, if (dragging) 8 else 5)
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            7f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220)
        )
        nvg.drawGradientRoundedRect(
            x,
            y,
            width,
            height,
            7f,
            ColorUtils.applyAlpha(accent.getColor1(), 198),
            ColorUtils.applyAlpha(accent.getColor2(), 198)
        )
        nvg.drawOutlineRoundedRect(
            x,
            y,
            width,
            height,
            7f,
            if (dragging) 1.4f else 1f,
            ColorUtils.applyAlpha(if (dragging) accent.getColor1() else accent.getColor2(), if (dragging) 196 else 148)
        )

        nvg.drawCenteredText(
            LegacyIcon.BELL,
            x + 10.5f,
            y + height / 2f - 5.5f,
            palette.getFontColor(ColorType.DARK),
            10.6f,
            Fonts.LEGACYICON
        )
        val textStartX = x + 21f
        val titleY = y + max(5.5f, height * 0.26f)
        val subtitleY = titleY + max(6f, height * 0.3f)
        nvg.drawRoundedRect(
            textStartX,
            titleY,
            max(14f, width - 28f),
            5.2f,
            2f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 220)
        )
        nvg.drawRoundedRect(
            textStartX,
            subtitleY,
            max(10f, width - 40f),
            4.4f,
            2f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 188)
        )
    }

    override fun mouseClickedExtra(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            return
        }
        val state = previewState ?: return
        val anchorIterator = state.anchors.entries.iterator()
        while (anchorIterator.hasNext()) {
            val entry = anchorIterator.next()
            if (MouseUtils.isInside(mouseX, mouseY, entry.value.x, entry.value.y, state.cardWidth, state.cardHeight)) {
                selectType(entry.key)
                highlightedAnchor = entry.key
                return
            }
        }
        if (!MouseUtils.isInside(mouseX, mouseY, cardX, cardY, state.cardWidth, state.cardHeight)) {
            return
        }

        dragActive = true
        dragOffsetX = mouseX - cardX
        dragOffsetY = mouseY - cardY
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && dragActive) {
            finalizeDragSnap()
        }
    }

    /**
     * Snaps dragged card to nearest anchor and commits selected type.
     */
    private fun finalizeDragSnap() {
        val state = previewState ?: return
        val centerX = cardX + state.cardWidth / 2f
        val centerY = cardY + state.cardHeight / 2f

        val nearest = findNearestAnchorType(centerX, centerY, state.anchors, state.cardWidth, state.cardHeight)

        dragActive = false
        highlightedAnchor = nearest
        if (nearest != getSelectedType()) {
            selectType(nearest)
        }
    }

    /**
     * Returns nearest anchor type based on card center distance.
     */
    private fun findNearestAnchorType(
        centerX: Float,
        centerY: Float,
        anchors: Map<UILayoutType, NotificationPoint>,
        cardWidth: Float,
        cardHeight: Float
    ): UILayoutType {
        var nearestType = UILayoutType.NOTIFICATION_BOTTOM_RIGHT
        var nearestDistance = Float.MAX_VALUE
        val iterator = anchors.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val dx = entry.value.x + cardWidth / 2f - centerX
            val dy = entry.value.y + cardHeight / 2f - centerY
            val distance = dx * dx + dy * dy
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestType = entry.key
            }
        }
        return nearestType
    }

    /**
     * Builds fixed corner anchor points for current preview canvas.
     */
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

    private data class NotificationPoint(val x: Float, val y: Float)

    private data class NotificationPreviewState(
        val areaX: Float,
        val areaY: Float,
        val areaWidth: Float,
        val areaHeight: Float,
        val cardWidth: Float,
        val cardHeight: Float,
        val anchors: Map<UILayoutType, NotificationPoint>
    )

    companion object {
        private const val MAGNET_RADIUS = 30f
    }
}
