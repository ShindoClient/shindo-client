package me.miki.shindo.gui.modmenu.category.impl.setting.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.Language
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.util.ResourceLocation
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class LanguageScene(parent: SettingsCategory) :
    SettingScene(parent, TranslateText.LANGUAGE, TranslateText.LANGUAGE_DESCRIPTION, LegacyIcon.GLOBE) {

    private val languageScroll = Scroll()
    private val languageCards = ArrayList<LanguageCard>()

    private var columns = 0
    private var cardHeight = 0f

    override fun initGui() {
        languageScroll.resetAll()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val colorManager = instance.colorManager
        val palette = colorManager.palette
        val accentColor = colorManager.currentColor
        val languageManager = instance.languageManager

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()
        if (baseHeight <= 0f || baseWidth <= 0f) {
            return
        }
        val containerRadius = 12f

        languageCards.clear()

        nvg.drawShadow(baseX, baseY, baseWidth, baseHeight, containerRadius, 7)
        nvg.drawRoundedRect(baseX, baseY, baseWidth, baseHeight, containerRadius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210))
        nvg.drawRoundedRect(baseX + 1f, baseY + 1f, baseWidth - 2f, baseHeight - 2f, containerRadius - 1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))

        val viewportX = baseX + OUTER_PADDING
        val viewportY = baseY + OUTER_PADDING
        val viewportWidth = baseWidth - OUTER_PADDING * 2f
        val viewportHeight = baseHeight - OUTER_PADDING * 2f

        columns = if (viewportWidth > 420f) 2 else 1
        val cardWidth = (viewportWidth - (ROW_GAP * (columns - 1))) / columns
        val estimatedRows = max(1f, Language.values().size / columns.toFloat())
        cardHeight = max(66f, min(86f, viewportHeight / estimatedRows))

        val totalContentHeight = calculateTotalContentHeight(Language.values().size)
        languageScroll.maxScroll = max(0f, totalContentHeight - viewportHeight)
        if (MouseUtils.isInside(mouseX, mouseY, viewportX, viewportY, viewportWidth, viewportHeight)) {
            languageScroll.onScroll()
        }
        languageScroll.onAnimation()
        val scrollValue = languageScroll.getValue()

        nvg.save()
        nvg.scissor(viewportX, viewportY, viewportWidth, viewportHeight)

        for ((index, language) in Language.values().withIndex()) {
            val row = index / columns
            val column = index % columns

            val cardX = viewportX + column * (cardWidth + ROW_GAP)
            val cardY = viewportY + row * (cardHeight + ROW_GAP) + scrollValue

            val cardHovered = MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight)
            val selected = language == languageManager.currentLanguage

            language.animation.setAnimation(if (selected) 1.0f else 0.0f, 16.0)

            drawLanguageCard(nvg, palette, accentColor, language, cardX, cardY, cardWidth, cardHeight, cardHovered, selected)

            languageCards.add(LanguageCard(language, cardX, cardY, cardWidth, cardHeight))
        }

        nvg.restore()

        nvg.drawScrollbar(viewportX + 20, viewportY, viewportWidth, viewportHeight, totalContentHeight, scrollValue, palette, accentColor, 24f)
    }

    private fun calculateTotalContentHeight(languageCount: Int): Float {
        val rows = ceil(languageCount / columns.toFloat()).toInt()
        return rows * cardHeight + max(0f, rows - 1f) * ROW_GAP
    }

    private fun drawLanguageCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        language: Language,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        hovered: Boolean,
        selected: Boolean
    ) {
        val baseColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), if (hovered || selected) 210 else 175)
        nvg.drawRoundedRect(x, y, width, height, CARD_RADIUS, baseColor)
        nvg.drawGradientRoundedRect(
            x,
            y,
            width,
            height,
            CARD_RADIUS,
            ColorUtils.applyAlpha(accentColor.color1, if (hovered || selected) 45 else 25),
            ColorUtils.applyAlpha(accentColor.color2, if (hovered || selected) 45 else 25)
        )

        val flagSize = min(56f, height - 24f)
        val flagX = x + 16f
        val flagY = y + (height - flagSize) / 2f
        drawFlag(nvg, language.flag, flagX, flagY, flagSize)

        val textX = flagX + flagSize + 34f
        var textWidth = width - (textX - x) - 20f
        textWidth = max(120f, textWidth)

        val languageName = nvg.getLimitText(language.name, 11f, Fonts.MEDIUM, textWidth)
        nvg.drawText(languageName, textX, y + 20f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
        nvg.drawText(language.id.toUpperCase(), textX, y + 34f, palette.getFontColor(ColorType.NORMAL), 8.5f, Fonts.REGULAR)

        if (selected) {
            nvg.drawText(
                LegacyIcon.CHECK,
                x + width - 22f,
                y + 18f,
                ColorUtils.applyAlpha(accentColor.interpolateColor, (language.animation.value * 255).toInt()),
                13f,
                Fonts.LEGACYICON
            )
        } else if (hovered) {
            nvg.drawOutlineRoundedRect(x, y, width, height, CARD_RADIUS, 1.4f, ColorUtils.applyAlpha(accentColor.color2, 160))
        }
    }

    private fun drawFlag(nvg: NanoVGManager, flag: ResourceLocation, x: Float, y: Float, size: Float) {
        val flagWidth = size * 1.6f
        nvg.drawRoundedImage(flag, x, y, flagWidth, size, 6f)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            return
        }

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (!MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            return
        }

        val languageManager = Shindo.getInstance().languageManager

        for (card in languageCards) {
            if (MouseUtils.isInside(mouseX, mouseY, card.x, card.y, card.width, card.height)) {
                languageManager.setCurrentLanguage(card.language)
                break
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        languageScroll.onKey(keyCode)
    }

    private data class LanguageCard(val language: Language, val x: Float, val y: Float, val width: Float, val height: Float)

    companion object {
        private const val OUTER_PADDING = 26f
        private const val ROW_GAP = 14f
        private const val CARD_RADIUS = 10f
    }
}
