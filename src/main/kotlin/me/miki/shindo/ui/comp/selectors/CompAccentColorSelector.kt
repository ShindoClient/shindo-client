package me.miki.shindo.ui.comp.selectors

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.templates.CompPanel
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import java.awt.Color
import kotlin.math.max

/**
 * Componente para seleção de accent colors com scroll horizontal.
 * Exibe cards com preview de cada cor e permite seleção.
 */
class CompAccentColorSelector(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 120f,
    accentColors: List<AccentColor>
) : CompPanel(x, y, width, height) {

    private val accentColorsList: List<AccentColor> = accentColors
    private val scroll = Scroll()
    private var selectedColor: AccentColor? = null
    private var onColorSelected: ((AccentColor) -> Unit)? = null

    private val itemWidth: Float = 96f
    private val itemSpacing: Float = 16f
    private val innerPadding: Float = 18f
    private val itemHeight: Float = 76f

    init {
        setWidth(width)
        setHeight(height)
        setRadius(10f)
        setBackgroundColor(null) // Será definido no draw
    }

    fun getSelectedColor(): AccentColor? = selectedColor
    fun setSelectedColor(color: AccentColor) {
        this.selectedColor = color
    }

    fun setOnColorSelected(callback: ((AccentColor) -> Unit)?): CompAccentColorSelector {
        this.onColorSelected = callback
        return this
    }

    override fun getBackgroundColor(paletteColors: me.miki.shindo.management.color.palette.ColorPalette, accentColors: me.miki.shindo.management.color.AccentColor): Color? {
        return ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.MID), 165)
    }

    override fun drawPanelContent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val paletteColors = palette
        val currentAccent = accent

        // Desenha overlay de gradiente
        nvgInstance.drawGradientRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            getRadius(),
            ColorUtils.applyAlpha(currentAccent.color1, 28),
            ColorUtils.applyAlpha(currentAccent.color2, 28)
        )

        val innerX = getX() + innerPadding
        val innerY = getY() + innerPadding
        val visibleWidth = getWidth() - innerPadding * 2f

        val totalWidth = accentColorsList.size * itemWidth + (accentColorsList.size - 1) * itemSpacing
        scroll.maxScroll = max(0f, totalWidth - visibleWidth)

        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            scroll.onScroll()
        }

        scroll.onAnimation()

        val scrollValue = scroll.getValue()

        nvgInstance.save()
        nvgInstance.intersectScissor(getX(), getY(), getWidth(), getHeight())

        var cardX = innerX + scrollValue
        for (accent in accentColorsList) {
            val screenX = cardX
            val hovered = MouseUtils.isInside(mouseX, mouseY, screenX, innerY, itemWidth, itemHeight)
            val selected = accent == selectedColor

            accent.animation.setAnimation(if (selected) 1.0f else 0.0f, 18.0)

            nvgInstance.drawRoundedRect(
                screenX,
                innerY,
                itemWidth,
                itemHeight,
                10f,
                ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.MID), if (hovered || selected) 220 else 190)
            )
            nvgInstance.drawGradientRoundedRect(
                screenX,
                innerY,
                itemWidth,
                itemHeight,
                10f,
                ColorUtils.applyAlpha(accent.color1, if (selected) 220 else 185),
                ColorUtils.applyAlpha(accent.color2, if (selected) 220 else 185)
            )

            if (selected) {
                nvgInstance.drawText(
                    LegacyIcon.CHECK,
                    screenX + itemWidth - 18f,
                    innerY + 10f,
                    Color(255, 255, 255, (accent.animation.value * 255).toInt()),
                    12f,
                    Fonts.LEGACYICON
                )
            } else if (hovered) {
                nvgInstance.drawOutlineRoundedRect(
                    screenX,
                    innerY,
                    itemWidth,
                    itemHeight,
                    10f,
                    2f,
                    ColorUtils.applyAlpha(accent.color2, 160)
                )
            }

            val label = nvgInstance.getLimitText(accent.name, 8.5f, Fonts.MEDIUM, itemWidth - 16f)
            nvgInstance.drawCenteredText(
                label,
                screenX + itemWidth / 2f,
                innerY + itemHeight - 18f,
                Color.WHITE,
                8.5f,
                Fonts.MEDIUM
            )

            cardX += itemWidth + itemSpacing
        }

        nvgInstance.restore()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val innerX = getX() + innerPadding
        val innerY = getY() + innerPadding
        val scrollValue = scroll.getValue()

        var cardX = innerX + scrollValue
        for (accent in accentColorsList) {
            val screenX = cardX
            if (MouseUtils.isInside(mouseX, mouseY, screenX, innerY, itemWidth, itemHeight)) {
                selectedColor = accent
                onColorSelected?.invoke(accent)
                return
            }
            cardX += itemWidth + itemSpacing
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        scroll.onKey(keyCode)
        super.keyTyped(typedChar, keyCode)
    }
}
