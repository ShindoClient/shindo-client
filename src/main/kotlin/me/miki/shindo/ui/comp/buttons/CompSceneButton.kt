package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.templates.CompControlTemplate
import me.miki.shindo.utils.ColorUtils

class CompSceneButton(
        private val iconSupplier: () -> String,
        private val titleSupplier: () -> String,
        private val descriptionSupplier: () -> String
) : CompControlTemplate() {

    private var active: Boolean = false

    init {
        setVariant(CompControlVariant.GHOST)
        setHeight(DEFAULT_HEIGHT)
    }

    fun setActive(active: Boolean): CompSceneButton {
        this.active = active
        return this
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()
        val paletteColors = palette
        val accentColors = accent
        val isHighlighted = hovered || active

        val baseColor = ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.MID), if (active) 210 else 188)
        val overlayStart = ColorUtils.applyAlpha(accentColors.getColor1(), if (isHighlighted) 62 else 38)
        val overlayEnd = ColorUtils.applyAlpha(accentColors.getColor2(), if (isHighlighted) 62 else 38)

        nvg.drawShadow(x, y, width, height, CARD_RADIUS, SHADOW_STRENGTH)
        nvg.drawRoundedRect(x, y, width, height, CARD_RADIUS, baseColor)
        nvg.drawGradientRoundedRect(x, y, width, height, CARD_RADIUS, overlayStart, overlayEnd)

        val iconX = x + ICON_MARGIN
        val iconY = y + (height - ICON_SIZE) / 2f
        nvg.drawGradientRoundedRect(
                iconX,
                iconY,
                ICON_SIZE,
                ICON_SIZE,
                ICON_RADIUS,
                ColorUtils.applyAlpha(accentColors.getColor1(), 160),
                ColorUtils.applyAlpha(accentColors.getColor2(), 160)
        )
        nvg.drawCenteredText(
                iconSupplier.invoke(),
                iconX + ICON_SIZE / 2f - 1f,
                iconY + ICON_SIZE / 2f - 8f,
                java.awt.Color.WHITE,
                ICON_FONT_SIZE,
                Fonts.LEGACYICON
        )

        val textStartX = iconX + ICON_SIZE + TEXT_MARGIN_START
        val textWidth = width - (textStartX - x) - TEXT_MARGIN_END
        val title = nvg.getLimitText(titleSupplier.invoke(), TITLE_FONT_SIZE, Fonts.MEDIUM, textWidth)
        val rawDescription = descriptionSupplier.invoke()
        val description = if (!"null".equals(rawDescription, ignoreCase = true)) {
            nvg.getLimitText(rawDescription, DESCRIPTION_FONT_SIZE, Fonts.REGULAR, textWidth)
        } else {
            ""
        }

        nvg.drawText(
                title,
                textStartX,
                y + TITLE_OFFSET,
                paletteColors.getFontColor(ColorType.DARK),
                TITLE_FONT_SIZE,
                Fonts.MEDIUM
        )
        if (description.isNotEmpty()) {
            nvg.drawText(
                    description,
                    textStartX,
                    y + DESCRIPTION_OFFSET,
                    paletteColors.getFontColor(ColorType.NORMAL),
                    DESCRIPTION_FONT_SIZE,
                    Fonts.REGULAR
            )
        }

        val arrowX = x + width - ARROW_MARGIN
        val arrowY = y + (height / 2f) - (nvg.getTextHeight(LegacyIcon.CHEVRON_RIGHT, ARROW_FONT_SIZE, Fonts.LEGACYICON) / 2f)
        nvg.drawCenteredText(
                LegacyIcon.CHEVRON_RIGHT,
                arrowX,
                arrowY,
                paletteColors.getFontColor(ColorType.NORMAL),
                ARROW_FONT_SIZE,
                Fonts.LEGACYICON
        )
    }

    companion object {
        private const val DEFAULT_HEIGHT = 52f
        private const val CARD_RADIUS = 10f
        private const val SHADOW_STRENGTH = 5
        private const val ICON_MARGIN = 18f
        private const val ICON_SIZE = 28f
        private const val ICON_RADIUS = 8f
        private const val TEXT_MARGIN_START = 14f
        private const val TEXT_MARGIN_END = 34f
        private const val TITLE_FONT_SIZE = 11.5f
        private const val DESCRIPTION_FONT_SIZE = 8.5f
        private const val TITLE_OFFSET = 16f
        private const val DESCRIPTION_OFFSET = 30f
        private const val ICON_FONT_SIZE = 18f
        private const val ARROW_MARGIN = 22f
        private const val ARROW_FONT_SIZE = 12f
    }
}
