package me.miki.shindo.ui.comp.selectors

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.Theme
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.templates.CompPanel
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import java.awt.Color
import kotlin.math.max

/**
 * Seletor de tema para a tela de welcome com preview visual.
 * Mantém o estilo original com previews dos temas.
 */
class CompThemeSelectorWelcome(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 88f
) : CompPanel(x, y, width, height) {

    private val themes: List<Theme> = Theme.entries.toList()
    private val scroll = Scroll()
    private var selectedTheme: Theme? = null
    private var onThemeSelected: ((Theme) -> Unit)? = null

    private val itemWidth: Float = 90f
    private val itemSpacing: Float = 12f
    private val itemHeight: Float = 56f
    private val padding: Float = 14f

    init {
        setWidth(width)
        setHeight(height)
        setRadius(0f) // Sem bordas, será desenhado pelo painel pai
        setBackgroundColor(null)
        setShadowEnabled(false)
    }

    fun getSelectedTheme(): Theme? = selectedTheme
    fun setSelectedTheme(theme: Theme) {
        this.selectedTheme = theme
    }

    fun setOnThemeSelected(callback: ((Theme) -> Unit)?): CompThemeSelectorWelcome {
        this.onThemeSelected = callback
        return this
    }

    override fun drawPanelContent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val accentColors = accent

        val innerX = getX() + padding
        val innerY = getY()
        val visibleWidth = getWidth() - padding * 2f

        val totalWidth = themes.size * itemWidth + (themes.size - 1) * itemSpacing
        scroll.maxScroll = max(0f, totalWidth - visibleWidth)

        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            scroll.onScroll()
        }

        scroll.onAnimation()

        val scrollValue = scroll.getValue()

        nvgInstance.save()
        nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())
        nvgInstance.translate(scrollValue, 0f)

        var cardX = innerX
        for (theme in themes) {
            val screenX = cardX
            val hovered = MouseUtils.isInside(mouseX, mouseY, screenX, innerY, itemWidth, itemHeight)
            val selected = theme == selectedTheme

            theme.animation.setAnimation(if (selected) 1.0f else 0.0f, 16.0)

            // Preview do tema
            drawModMenuExample(nvgInstance, screenX, innerY, theme)

            // Borda de seleção
            if (selected) {
                nvgInstance.save()
                nvgInstance.setAlpha(theme.animation.value)
                nvgInstance.drawGradientOutlineRoundedRect(
                    screenX,
                    innerY,
                    itemWidth,
                    itemHeight,
                    6f,
                    1f,
                    accentColors.color1,
                    accentColors.color2
                )
                nvgInstance.restore()
            }

            // Nome do tema
            nvgInstance.drawCenteredText(
                theme.name,
                screenX + itemWidth / 2f,
                innerY + itemHeight + 8f,
                Color.WHITE,
                9.5f,
                Fonts.REGULAR
            )

            cardX += itemWidth + itemSpacing
        }

        nvgInstance.restore()
    }

    private fun drawModMenuExample(nvg: NanoVGManager, x: Float, y: Float, theme: Theme) {
        var offsetY = 0

        nvg.drawRoundedRect(x, y, itemWidth, itemHeight, 6f, theme.normalBackgroundColor)
        nvg.drawRoundedRectVarying(x, y, 12f, itemHeight, 6f, 0f, 6f, 0f, theme.darkBackgroundColor)

        for (i in 0..2) {
            nvg.drawRoundedRect(x + 15f, y + offsetY + 6f, itemWidth - 20f, 12f, 2.5f, theme.darkBackgroundColor)
            nvg.drawRoundedRect(x + 17f, y + offsetY + 7.5f, 9f, 9f, 2f, theme.normalBackgroundColor)
            offsetY += 16
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val innerX = getX() + padding
        val innerY = getY()
        val scrollValue = scroll.getValue()

        var cardX = innerX + scrollValue
        for (theme in themes) {
            val screenX = cardX
            if (MouseUtils.isInside(mouseX, mouseY, screenX, innerY, itemWidth, itemHeight)) {
                selectedTheme = theme
                onThemeSelected?.invoke(theme)
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
