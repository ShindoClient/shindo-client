package com.shindoclient.shindo.ui.components.v2.selectors

import com.shindoclient.shindo.management.color.Theme
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.ui.components.v2.templates.CompPanel
import com.shindoclient.shindo.ui.components.v2.templates.PanelStyle
import com.shindoclient.shindo.utils.ColorUtils
import com.shindoclient.shindo.utils.mouse.MouseUtils
import com.shindoclient.shindo.utils.mouse.Scroll
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class CompThemeSelector(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 122f,
) : CompPanel(x, y, width, height) {
    private val themes: List<Theme> = Theme.values().toList()
    private val scroll = Scroll()
    private var selectedTheme: Theme? = null
    private var onThemeSelected: ((Theme) -> Unit)? = null

    private val itemWidth: Float = 112f
    private val itemSpacing: Float = 18f
    private val innerPadding: Float = 18f

    init {
        setWidth(width)
        setHeight(height)
        setRadius(10f)
        setStyle(PanelStyle.CARD)
    }

    fun getSelectedTheme(): Theme? = selectedTheme

    fun setSelectedTheme(theme: Theme) {
        this.selectedTheme = theme
    }

    fun setOnThemeSelected(callback: ((Theme) -> Unit)?): CompThemeSelector {
        this.onThemeSelected = callback
        return this
    }

    override fun drawPanelContent(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val innerX = getX() + innerPadding
        val innerY = getY() + innerPadding
        val visibleWidth = getWidth() - innerPadding * 2f
        val itemHeight = min(88f, getHeight() - innerPadding * 2f)

        val totalWidth = themes.size * itemWidth + (themes.size - 1) * itemSpacing
        scroll.maxScroll = max(0f, totalWidth - visibleWidth)

        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            scroll.onScroll()
        }

        scroll.onAnimation()

        val scrollValue = scroll.getValue()

        nvg.save()
        nvg.intersectScissor(getX(), getY(), getWidth(), getHeight())

        var cardX = innerX + scrollValue
        for (theme in themes) {
            val screenX = cardX
            val hovered = MouseUtils.isInside(mouseX, mouseY, screenX, innerY, itemWidth, itemHeight)
            val selected = theme == selectedTheme

            theme.getAnimation().setAnimation(if (selected) 1.0f else 0.0f, 18.0)

            val baseColor =
                ColorUtils.applyAlpha(theme.getNormalBackgroundColor(), if (hovered || selected) 240 else 205)
            val overlayColor =
                ColorUtils.applyAlpha(theme.getDarkBackgroundColor(), if (hovered || selected) 220 else 185)

            nvg.drawRoundedRect(screenX, innerY, itemWidth, itemHeight, 10f, baseColor)
            nvg.drawGradientRoundedRect(screenX, innerY, itemWidth, itemHeight, 10f, baseColor, overlayColor)

            nvg.drawRoundedRect(
                screenX + 12f,
                innerY + 16f,
                itemWidth - 24f,
                12f,
                4f,
                ColorUtils.applyAlpha(theme.getDarkFontColor(), 210),
            )
            nvg.drawRoundedRect(
                screenX + 12f,
                innerY + 34f,
                itemWidth - 24f,
                7f,
                3f,
                ColorUtils.applyAlpha(theme.getNormalFontColor(), 190),
            )

            val label = nvg.getLimitText(theme.name, 9.5f, Fonts.MEDIUM, itemWidth - 24f)
            nvg.drawText(label, screenX + 12f, innerY + itemHeight - 22f, Color.WHITE, 9.5f, Fonts.MEDIUM)

            if (selected) {
                nvg.drawText(
                    Lucide.CHECK,
                    screenX + itemWidth - 18f,
                    innerY + 12f,
                    Color(255, 255, 255, min(255, 180 + (theme.getAnimation().getValue() * 60f).toInt())),
                    12f,
                    Fonts.LUCIDE,
                )
            } else if (hovered) {
                nvg.drawOutlineRoundedRect(
                    screenX,
                    innerY,
                    itemWidth,
                    itemHeight,
                    10f,
                    2f,
                    ColorUtils.applyAlpha(accent.getColor2(), 160),
                )
            }

            cardX += itemWidth + itemSpacing
        }

        nvg.restore()
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!isVisible() || mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val innerX = getX() + innerPadding
        val innerY = getY() + innerPadding
        val itemHeight = min(88f, getHeight() - innerPadding * 2f)
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

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        scroll.onKey(keyCode)
        super.keyTyped(typedChar, keyCode)
    }
}
