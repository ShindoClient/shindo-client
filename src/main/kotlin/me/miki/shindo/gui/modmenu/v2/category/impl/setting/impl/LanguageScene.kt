package me.miki.shindo.gui.modmenu.v2.category.impl.setting.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.v2.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.v2.category.impl.setting.SettingScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.Language
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.ui.components.v2.layout.CompScrollableContainer
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.util.ResourceLocation
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class LanguageScene(
    parent: SettingsCategory,
) : SettingScene(parent, TranslateText.LANGUAGE, TranslateText.LANGUAGE_DESCRIPTION, Lucide.GLOBE) {
    private lateinit var container: CompScrollableContainer
    private val languages = Language.entries.toTypedArray()
    private val languageCards = ArrayList<LanguageCard>(languages.size)

    override fun initGui() {
        container =
            CompScrollableContainer()
                .setScrollbarGutter(14f)
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val colorManager = instance.getColorManager()
        val palette = colorManager.getPalette()
        val accent = colorManager.getCurrentColor()
        val languageManager = instance.getLanguageManager()
        val selectedLanguage = languageManager.getCurrentLanguage()

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()
        if (baseWidth <= 0f || baseHeight <= 0f) return

        container.setBounds(baseX, baseY, baseWidth, baseHeight)
        val estimatedViewportWidth = max(0f, baseWidth - 36f - 14f - OUTER_PADDING * 2f)
        val estimatedViewportHeight = max(0f, baseHeight - 36f - OUTER_PADDING * 2f)
        val estimatedGrid = resolveGridMetrics(estimatedViewportWidth, estimatedViewportHeight)
        val totalContentHeight = calculateTotalContentHeight(estimatedGrid)

        languageCards.clear()
        container.renderWithViewport(
            mouseX,
            mouseY,
            partialTicks,
            totalContentHeight,
        ) { innerMouseX, innerMouseY, _, scrollValue, viewport ->
            val contentX = viewport.x + OUTER_PADDING
            val contentY = viewport.y + OUTER_PADDING
            val contentWidth = max(0f, viewport.width - OUTER_PADDING * 2f)
            val contentHeight = max(0f, viewport.height - OUTER_PADDING * 2f)
            val grid = resolveGridMetrics(contentWidth, contentHeight)

            val cardsStartY = contentY + SECTION_GAP

            for (index in languages.indices) {
                val language = languages[index]
                val row = index / grid.columns
                val column = index % grid.columns
                val cardX = contentX + column * (grid.cardWidth + ROW_GAP)
                val cardY = cardsStartY + row * (grid.cardHeight + ROW_GAP) + scrollValue
                val hovered =
                    MouseUtils.isInside(innerMouseX, innerMouseY, cardX, cardY, grid.cardWidth, grid.cardHeight)
                val selected = language == selectedLanguage

                language.getAnimation().setAnimation(if (selected) 1.0f else 0.0f, 16.0)

                drawLanguageCard(
                    nvg,
                    palette,
                    accent,
                    language,
                    cardX,
                    cardY,
                    grid.cardWidth,
                    grid.cardHeight,
                    hovered,
                    selected,
                )

                languageCards.add(LanguageCard(language, cardX, cardY, grid.cardWidth, grid.cardHeight))
            }
        }
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) return

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()
        if (!MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) return

        val languageManager = Shindo.getInstance().getLanguageManager()
        for (card in languageCards) {
            if (!MouseUtils.isInside(mouseX, mouseY, card.x, card.y, card.width, card.height)) continue
            if (card.language != languageManager.getCurrentLanguage()) {
                languageManager.setCurrentLanguage(card.language)
            }
            break
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        container.keyTyped(typedChar, keyCode)
    }

    private fun resolveGridMetrics(
        availableWidth: Float,
        availableHeight: Float,
    ): GridMetrics {
        val columns = if (availableWidth > 420f) 2 else 1
        val cardWidth = max(120f, (availableWidth - ROW_GAP * (columns - 1)) / columns)
        val rows = max(1, ceil(languages.size / columns.toDouble()).toInt())
        val cardsHeightBudget = max(0f, availableHeight - SECTION_GAP)
        val cardHeight = max(CARD_MIN_HEIGHT, min(CARD_MAX_HEIGHT, (cardsHeightBudget - ROW_GAP * (rows - 1)) / rows))
        return GridMetrics(columns, cardWidth, cardHeight, rows)
    }

    private fun calculateTotalContentHeight(grid: GridMetrics): Float {
        val cardsHeight = grid.rows * grid.cardHeight + max(0f, grid.rows - 1f) * ROW_GAP
        return OUTER_PADDING * 2f + SECTION_GAP + cardsHeight
    }

    private fun drawLanguageCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        language: Language,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        hovered: Boolean,
        selected: Boolean,
    ) {
        val progress = language.getAnimation().getValue()

        nvg.drawShadow(x, y, width, height, CARD_RADIUS, 7)
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            CARD_RADIUS,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220),
        )
        nvg.drawOutlineRoundedRect(
            x,
            y,
            width,
            height,
            CARD_RADIUS,
            1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
        )

        if (selected) {
            val badgeX = x + width - 20f - 2
            val badgeY = y + height - 18f - 2
            val badgeSize = 16f

            nvg.drawShadow(badgeX, badgeY, badgeSize, badgeSize, 3.5f, 7)
            nvg.drawRoundedRect(
                badgeX,
                badgeY,
                badgeSize,
                badgeSize,
                3.5f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220),
            )
            nvg.drawOutlineRoundedRect(
                badgeX,
                badgeY,
                badgeSize,
                badgeSize,
                3.5f,
                1f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
            )

            nvg.drawText(
                Lucide.CHECK,
                x + width - 20f,
                y + height - 18f,
                palette.getFontColor(ColorType.MID),
                12f,
                Fonts.LUCIDE,
            )
        }

        val mediaHeight = max(34f, height - 22f)
        val mediaWidth = min(86f, max(56f, width * 0.28f))
        val mediaX = x + 14f
        val mediaY = y + (height - mediaHeight) / 2f

        nvg.drawRoundedRect(
            mediaX,
            mediaY,
            mediaWidth,
            mediaHeight,
            6.5f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 180),
        )
        drawFlag(nvg, language.getFlag(), mediaX + 1f, mediaY + 1f, mediaWidth - 2f, mediaHeight - 2f)

        val textX = mediaX + mediaWidth + 14f
        val rightPadding = 14f
        val availableTextWidth = max(90f, width - (textX - x) - rightPadding)
        val languageName = nvg.getLimitText(language.getName(), 11.8f, Fonts.MEDIUM, availableTextWidth)
        nvg.drawText(languageName, textX, y + 22f, palette.getFontColor(ColorType.DARK), 11.8f, Fonts.MEDIUM)

        val localeCode = language.getId().uppercase(Locale.ROOT)
        val codeWidth = nvg.getTextWidth(localeCode, 7.4f, Fonts.MEDIUM) + 12f
        val codeHeight = nvg.getTextHeight(localeCode, 7.4f, Fonts.MEDIUM)
        val codeX = x + width - rightPadding - codeWidth
        val codeY = y + 11f

        nvg.drawRoundedRect(
            codeX,
            codeY,
            codeWidth,
            13f,
            6f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), if (selected) 225 else 198),
        )
        nvg.drawCenteredText(
            localeCode,
            codeX + codeWidth / 2f,
            (codeY + 8f) - codeHeight / 2f,
            palette.getFontColor(ColorType.DARK),
            7.4f,
            Fonts.MEDIUM,
        )
    }

    private fun drawFlag(
        nvg: NanoVGManager,
        flag: ResourceLocation,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        nvg.drawRoundedImage(flag, x, y, width, height, 5f)
    }

    private data class GridMetrics(
        val columns: Int,
        val cardWidth: Float,
        val cardHeight: Float,
        val rows: Int,
    )

    private data class LanguageCard(
        val language: Language,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
    )

    companion object {
        private const val OUTER_PADDING = 26f
        private const val ROW_GAP = 14f
        private const val CARD_RADIUS = 10f
        private const val SECTION_GAP = 10f
        private const val CARD_MIN_HEIGHT = 70f
        private const val CARD_MAX_HEIGHT = 90f
    }
}
