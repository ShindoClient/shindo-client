package me.miki.shindo.ui.components.v1.selectors

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.Theme
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.components.v1.style.CompSurfaceVariant
import me.miki.shindo.ui.components.v1.templates.CompPanel
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class CompThemeSelector(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 122f
) : CompPanel(x, y, width, height) {

    private val themes: List<Theme> = Theme.values().toList()
    private val scroll = Scroll()
    private var selectedTheme: Theme? = null
    private var onThemeSelected: ((Theme) -> Unit)? = null

    private val itemWidth: Float = 112f
    private val itemSpacing: Float = 18f
    private val innerPadding: Float = 18f

    init {
        Comp.setWidth(width)
        Comp.setHeight(height)
        setRadius(10f)
        setSurfaceVariant(CompSurfaceVariant.CARD)
        setBackgroundColor(null)
    }

    fun getSelectedTheme(): Theme? = selectedTheme
    fun setSelectedTheme(theme: Theme) {
        this.selectedTheme = theme
    }

    fun setOnThemeSelected(callback: ((Theme) -> Unit)?): CompThemeSelector {
        this.onThemeSelected = callback
        return this
    }

    override fun getBackgroundColor(
        paletteColors: ColorPalette,
        accentColors: AccentColor
    ): Color {
        return ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.MID), 220)
    }

    override fun getBorderColor(palette: ColorPalette, accent: AccentColor): Color {
        return ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210)
    }


    override fun drawPanelContent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = Comp.nvg
        Comp.palette
        val accentColors = Comp.accent

        val innerX = Comp.getX() + innerPadding
        val innerY = Comp.getY() + innerPadding
        val visibleWidth = Comp.getWidth() - innerPadding * 2f
        val itemHeight = min(88f, Comp.getHeight() - innerPadding * 2f)

        val totalWidth = themes.size * itemWidth + (themes.size - 1) * itemSpacing
        scroll.maxScroll = max(0f, totalWidth - visibleWidth)

        if (MouseUtils.isInside(mouseX, mouseY, Comp.getX(), Comp.getY(), Comp.getWidth(), Comp.getHeight())) {
            scroll.onScroll()
        }

        scroll.onAnimation()

        val scrollValue = scroll.getValue()

        nvgInstance.save()
        nvgInstance.intersectScissor(Comp.getX(), Comp.getY(), Comp.getWidth(), Comp.getHeight())

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

            nvgInstance.drawRoundedRect(screenX, innerY, itemWidth, itemHeight, 10f, baseColor)
            nvgInstance.drawGradientRoundedRect(screenX, innerY, itemWidth, itemHeight, 10f, baseColor, overlayColor)

            nvgInstance.drawRoundedRect(
                screenX + 12f,
                innerY + 16f,
                itemWidth - 24f,
                12f,
                4f,
                ColorUtils.applyAlpha(theme.getDarkFontColor(), 210)
            )
            nvgInstance.drawRoundedRect(
                screenX + 12f,
                innerY + 34f,
                itemWidth - 24f,
                7f,
                3f,
                ColorUtils.applyAlpha(theme.getNormalFontColor(), 190)
            )

            val label = nvgInstance.getLimitText(theme.name, 9.5f, Fonts.MEDIUM, itemWidth - 24f)
            nvgInstance.drawText(label, screenX + 12f, innerY + itemHeight - 22f, Color.WHITE, 9.5f, Fonts.MEDIUM)

            if (selected) {
                nvgInstance.drawText(
                    LegacyIcon.CHECK,
                    screenX + itemWidth - 18f,
                    innerY + 12f,
                    Color(255, 255, 255, min(255, 180 + (theme.getAnimation().value * 60f).toInt())),
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
                    ColorUtils.applyAlpha(accentColors.getColor2(), 160)
                )
            }

            cardX += itemWidth + itemSpacing
        }

        nvgInstance.restore()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!Comp.isVisible() || mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val innerX = Comp.getX() + innerPadding
        val innerY = Comp.getY() + innerPadding
        val itemHeight = min(88f, Comp.getHeight() - innerPadding * 2f)
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
