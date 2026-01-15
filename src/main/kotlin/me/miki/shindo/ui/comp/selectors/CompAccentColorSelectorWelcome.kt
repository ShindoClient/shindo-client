package me.miki.shindo.ui.comp.selectors

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.templates.CompPanel
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import java.awt.Color
import kotlin.math.max

/**
 * Seletor de accent color para a tela de welcome com grid.
 * Mantém o estilo original com grid de cores.
 */
class CompAccentColorSelectorWelcome(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f,
    accentColors: List<AccentColor>
) : CompPanel(x, y, width, height) {

    private val accentColorsList: List<AccentColor> = accentColors
    private val scroll = Scroll()
    private var selectedColor: AccentColor? = null
    private var onColorSelected: ((AccentColor) -> Unit)? = null

    private val itemSize: Float = 32f
    private val itemSpacing: Float = 8f
    private val columns: Int = 4
    private val padding: Float = 10f

    init {
        setWidth(width)
        setHeight(height)
        setRadius(0f)
        setBackgroundColor(null)
        setShadowEnabled(false)
    }

    fun getSelectedColor(): AccentColor? = selectedColor
    fun setSelectedColor(color: AccentColor) {
        this.selectedColor = color
    }

    fun setOnColorSelected(callback: ((AccentColor) -> Unit)?): CompAccentColorSelectorWelcome {
        this.onColorSelected = callback
        return this
    }

    override fun drawPanelContent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg

        val innerX = getX() + padding
        val innerY = getY() + padding
        val visibleHeight = getHeight() - padding * 2f

        val rows = kotlin.math.ceil(accentColorsList.size / columns.toFloat()).toInt()
        val totalHeight = rows * itemSize + max(0f, rows - 1f) * itemSpacing
        scroll.maxScroll = max(0f, totalHeight - visibleHeight)

        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            scroll.onScroll()
        }

        scroll.onAnimation()

        val scrollValue = scroll.getValue()

        nvgInstance.save()
        nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())
        nvgInstance.translate(0f, scrollValue)

        var offsetX = 0
        var offsetY = 0
        var index = 1

        for (accent in accentColorsList) {
            val screenX = innerX + offsetX
            val screenY = innerY + offsetY

            nvgInstance.drawGradientRoundedRect(screenX, screenY, itemSize, itemSize, 6f, accent.color1, accent.color2)

            accent.animation.setAnimation(if (accent == selectedColor) 1.0f else 0.0f, 16.0)

            if (accent == selectedColor) {
                nvgInstance.drawCenteredText(
                    LegacyIcon.CHECK,
                    screenX + itemSize / 2f,
                    screenY + itemSize / 2f,
                    Color(255, 255, 255, (accent.animation.value * 255).toInt()),
                    16f,
                    Fonts.LEGACYICON
                )
            }

            offsetX += (itemSize + itemSpacing).toInt()

            if (index % columns == 0) {
                offsetX = 0
                offsetY += (itemSize + itemSpacing).toInt()
            }

            index++
        }

        nvgInstance.restore()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val innerX = getX() + padding
        val innerY = getY() + padding
        val scrollValue = scroll.getValue().toInt()

        var offsetX = 0
        var offsetY = -scrollValue
        var index = 1

        for (accent in accentColorsList) {
            val screenX = innerX + offsetX
            val screenY = innerY + offsetY

            if (MouseUtils.isInside(mouseX, mouseY, screenX, screenY, itemSize, itemSize)) {
                selectedColor = accent
                onColorSelected?.invoke(accent)
                return
            }

            offsetX += (itemSize + itemSpacing).toInt()

            if (index % columns == 0) {
                offsetX = 0
                offsetY += (itemSize + itemSpacing).toInt()
            }

            index++
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        scroll.onKey(keyCode)
        super.keyTyped(typedChar, keyCode)
    }
}
