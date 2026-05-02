package me.miki.shindo.ui.components.v1.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import me.miki.shindo.ui.components.v1.style.CompControlVariant
import me.miki.shindo.ui.components.v1.templates.CompControlTemplate
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils

class CompSceneButton(
    private val iconSupplier: () -> String,
    private val titleSupplier: () -> String,
    private val descriptionSupplier: () -> String
) : CompControlTemplate() {

    private var active: Boolean = false
    private val hoverAnimation = SimpleAnimation()
    private val pressAnimation = SimpleAnimation()

    init {
        setVariant(CompControlVariant.GHOST)
        Comp.setHeight(DEFAULT_HEIGHT)
    }

    fun setActive(active: Boolean): CompSceneButton {
        this.active = active
        return this
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val x = Comp.getX()
        val y = Comp.getY()
        val width = Comp.getWidth()
        val height = Comp.getHeight()

        val iconX = x + ICON_MARGIN
        val iconY = y + (height - ICON_SIZE) / 2f

        Comp.nvg.drawShadow(x, y, width, height, CARD_RADIUS, 7)
        Comp.nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            CARD_RADIUS,
            ColorUtils.applyAlpha(Comp.palette.getBackgroundColor(ColorType.DARK), 220)
        )
        Comp.nvg.drawOutlineRoundedRect(
            x,
            y,
            width,
            height,
            CARD_RADIUS,
            1f,
            ColorUtils.applyAlpha(Comp.palette.getBackgroundColor(ColorType.MID), 210)
        )
        //nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY + 19.5F, this.getWidth() - 30, 1F, 0, new Color(255, 200, 10));
        Comp.nvg.drawCenteredText(
            iconSupplier.invoke(),
            iconX + ICON_SIZE / 2f - 1f,
            iconY + ICON_SIZE / 2f - 8f,
            Comp.palette.getFontColor(ColorType.DARK),
            ICON_FONT_SIZE,
            Fonts.LEGACYICON
        )

        val textStartX = iconX + ICON_SIZE + TEXT_MARGIN_START
        val textWidth = width - (textStartX - x) - TEXT_MARGIN_END
        val title = Comp.nvg.getLimitText(titleSupplier.invoke(), TITLE_FONT_SIZE, Fonts.MEDIUM, textWidth)
        val rawDescription = descriptionSupplier.invoke()
        val description = if (!"null".equals(rawDescription, ignoreCase = true)) {
            Comp.nvg.getLimitText(rawDescription, DESCRIPTION_FONT_SIZE, Fonts.REGULAR, textWidth)
        } else {
            ""
        }

        Comp.nvg.drawText(title, textStartX, y + TITLE_OFFSET , Comp.palette.getFontColor(ColorType.DARK), TITLE_FONT_SIZE, Fonts.MEDIUM
        )
        if (description.isNotEmpty()) {
            Comp.nvg.drawText(description, textStartX, y + DESCRIPTION_OFFSET , Comp.palette.getFontColor(ColorType.NORMAL), DESCRIPTION_FONT_SIZE, Fonts.REGULAR
            )
        }

        val arrowX = x + width - ARROW_MARGIN
        val arrowY = y + (height / 2f) - (Comp.nvg.getTextHeight(LegacyIcon.CHEVRON_RIGHT, ARROW_FONT_SIZE, Fonts.LEGACYICON) / 2f)
        Comp.nvg.drawCenteredText(LegacyIcon.CHEVRON_RIGHT, arrowX, arrowY, Comp.palette.getFontColor(ColorType.NORMAL), ARROW_FONT_SIZE, Fonts.LEGACYICON)

    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && isEnabled()) {
            pressAnimation.value = 1.0f
        }
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
