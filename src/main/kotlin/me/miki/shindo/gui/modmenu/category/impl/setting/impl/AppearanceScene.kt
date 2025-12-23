package me.miki.shindo.gui.modmenu.category.impl.setting.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.Theme
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.impl.CompComboBox
import me.miki.shindo.ui.comp.impl.CompSettingButton
import me.miki.shindo.ui.comp.impl.CompToggleButton
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class AppearanceScene(parent: SettingsCategory) :
    SettingScene(parent, TranslateText.APPEARANCE, TranslateText.APPEARANCE_DESCRIPTION, LegacyIcon.MONITOR) {

    private val contentScroll = Scroll()
    private val themeScroll = Scroll()
    private val accentScroll = Scroll()
    private val themeHitboxes = ArrayList<CardHitbox<Theme>>()
    private val accentHitboxes = ArrayList<CardHitbox<AccentColor>>()

    private var themeSectionX = 0f
    private var themeSectionY = 0f
    private var themeSectionWidth = 0f
    private var themeSectionHeight = 0f

    private var accentSectionX = 0f
    private var accentSectionY = 0f
    private var accentSectionWidth = 0f
    private var accentSectionHeight = 0f

    private var cardX = 0f
    private var cardY = 0f
    private var cardWidth = 0f
    private var cardHeight = 0f

    private lateinit var modTheme: CompComboBox
    private lateinit var uiBlur: CompToggleButton

    private val settingCards = ArrayList<CompSettingButton>()

    override fun initGui() {
        modTheme = CompComboBox(110f, InternalSettingsMod.getInstance().modThemeSetting)
        uiBlur = CompToggleButton(InternalSettingsMod.getInstance().blurSetting)

        settingCards.clear()

        settingCards.add(
            CompSettingButton(0f, { TranslateText.HUD_THEME.text }, { TranslateText.STYLE.text })
                .trailing(modTheme)
        )

        settingCards.add(
            CompSettingButton(0f, { TranslateText.UI_BLUR.text }, { TranslateText.SMOOTH.text })
                .trailing(uiBlur)
                .onClick {
                    val setting = uiBlur.getSetting()
                    setting.setToggled(!setting.isToggled())
                }
        )

        contentScroll.resetAll()
        themeScroll.resetAll()
        accentScroll.resetAll()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val colorManager = instance.colorManager
        val palette = colorManager.palette
        val currentAccent = colorManager.currentColor
        val currentTheme = colorManager.theme

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (baseWidth <= 0f || baseHeight <= 0f) {
            return
        }

        themeHitboxes.clear()
        accentHitboxes.clear()

        val containerRadius = 12f
        nvg.drawShadow(baseX, baseY, baseWidth, baseHeight, containerRadius, 7)
        nvg.drawRoundedRect(baseX, baseY, baseWidth, baseHeight, containerRadius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210))
        nvg.drawRoundedRect(baseX + 1f, baseY + 1f, baseWidth - 2f, baseHeight - 2f, containerRadius - 1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))

        val top = baseY + OUTER_PADDING
        val themeHeight = 122f
        val accentHeight = 120f
        val controlHeight = 54f

        themeSectionX = baseX + OUTER_PADDING
        themeSectionY = top
        themeSectionWidth = max(0f, baseWidth - OUTER_PADDING * 2f)
        themeSectionHeight = themeHeight

        accentSectionX = themeSectionX
        accentSectionY = themeSectionY + themeSectionHeight + SECTION_SPACING
        accentSectionWidth = themeSectionWidth
        accentSectionHeight = accentHeight

        cardX = themeSectionX
        cardY = accentSectionY + accentSectionHeight + 10f
        cardWidth = themeSectionWidth
        cardHeight = controlHeight

        val contentHeight = OUTER_PADDING + themeSectionHeight + SECTION_SPACING + accentSectionHeight + 10f +
            ((controlHeight * settingCards.size) + 18f) + OUTER_PADDING
        contentScroll.maxScroll = max(0f, contentHeight - baseHeight)

        val rawVertical = contentScroll.getValue()
        val themeAreaTop = themeSectionY + rawVertical
        val accentAreaTop = accentSectionY + rawVertical

        if (MouseUtils.isInside(mouseX, mouseY, themeSectionX, themeAreaTop, themeSectionWidth, themeSectionHeight)) {
            themeScroll.onScroll()
        }
        if (MouseUtils.isInside(mouseX, mouseY, accentSectionX, accentAreaTop, accentSectionWidth, accentSectionHeight)) {
            accentScroll.onScroll()
        }

        if (MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight) &&
            !MouseUtils.isInside(mouseX, mouseY, themeSectionX, themeAreaTop, themeSectionWidth, themeSectionHeight) &&
            !MouseUtils.isInside(mouseX, mouseY, accentSectionX, accentAreaTop, accentSectionWidth, accentSectionHeight)
        ) {
            contentScroll.onScroll()
        }

        contentScroll.onAnimation()
        themeScroll.onAnimation()
        accentScroll.onAnimation()

        val verticalScroll = contentScroll.getValue()
        val themeScreenY = themeSectionY + verticalScroll
        val accentScreenY = accentSectionY + verticalScroll
        val controlsScreenY = cardY + verticalScroll

        nvg.save()
        nvg.scissor(baseX, baseY, baseWidth, baseHeight)

        drawSectionTitle(nvg, TranslateText.THEME.text, TranslateText.THEME_DESCRIPTION.text, themeSectionX, themeScreenY - 26f, palette)
        drawThemeCarousel(mouseX, mouseY, partialTicks, nvg, colorManager, palette, currentTheme, currentAccent, themeScreenY)

        drawSectionTitle(nvg, TranslateText.ACCENT_COLOR.text, TranslateText.DESIGN.text, accentSectionX, accentScreenY - 26f, palette)
        drawAccentCarousel(mouseX, mouseY, partialTicks, nvg, colorManager, palette, currentAccent, accentScreenY)

        drawControlCards(mouseX, mouseY, partialTicks, controlsScreenY)
        nvg.resetScissor()
        nvg.restore()

        nvg.drawScrollbar(baseX, baseY, baseWidth, baseHeight, contentHeight, verticalScroll, palette, currentAccent, 30f)
    }

    private fun drawSectionTitle(nvg: NanoVGManager, title: String, subtitle: String, x: Float, y: Float, palette: ColorPalette) {
        nvg.drawText(title, x, y, palette.getFontColor(ColorType.DARK), 12.5f, Fonts.MEDIUM)
        if (!subtitle.isEmpty() && !"null".equals(subtitle, ignoreCase = true)) {
            nvg.drawText(subtitle, x, y + 12f, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220), 8.5f, Fonts.REGULAR)
        }
    }

    private fun drawThemeCarousel(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        nvg: NanoVGManager,
        colorManager: ColorManager,
        palette: ColorPalette,
        currentTheme: Theme,
        accent: AccentColor,
        sectionY: Float
    ) {
        val radius = 10f
        nvg.drawRoundedRect(themeSectionX, sectionY, themeSectionWidth, themeSectionHeight, radius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 170))
        nvg.drawGradientRoundedRect(themeSectionX, sectionY, themeSectionWidth, themeSectionHeight, radius, ColorUtils.applyAlpha(accent.color1, 35), ColorUtils.applyAlpha(accent.color2, 35))

        val innerX = themeSectionX + INNER_PADDING
        val innerY = sectionY + INNER_PADDING
        val visibleWidth = themeSectionWidth - INNER_PADDING * 2f

        val totalWidth = Theme.values().size * THEME_ITEM_WIDTH + (Theme.values().size - 1) * THEME_ITEM_SPACING
        themeScroll.maxScroll = max(0f, totalWidth - visibleWidth)

        val scroll = themeScroll.getValue()
        val itemHeight = min(88f, themeSectionHeight - INNER_PADDING * 2f)

        nvg.save()
        nvg.intersectScissor(themeSectionX, sectionY, themeSectionWidth, themeSectionHeight)

        var cardX = innerX + scroll
        for (theme in Theme.values()) {
            val screenX = cardX
            val hovered = MouseUtils.isInside(mouseX, mouseY, screenX, innerY, THEME_ITEM_WIDTH, itemHeight)
            val selected = theme == colorManager.theme

            theme.animation.setAnimation(if (selected) 1.0f else 0.0f, 18.0)

            val baseColor = ColorUtils.applyAlpha(theme.normalBackgroundColor, if (hovered || selected) 240 else 205)
            val overlayColor = ColorUtils.applyAlpha(theme.darkBackgroundColor, if (hovered || selected) 220 else 185)

            nvg.drawRoundedRect(screenX, innerY, THEME_ITEM_WIDTH, itemHeight, 10f, baseColor)
            nvg.drawGradientRoundedRect(screenX, innerY, THEME_ITEM_WIDTH, itemHeight, 10f, baseColor, overlayColor)

            nvg.drawRoundedRect(screenX + 12f, innerY + 16f, THEME_ITEM_WIDTH - 24f, 12f, 4f, ColorUtils.applyAlpha(theme.darkFontColor, 210))
            nvg.drawRoundedRect(screenX + 12f, innerY + 34f, THEME_ITEM_WIDTH - 24f, 7f, 3f, ColorUtils.applyAlpha(theme.normalFontColor, 190))

            val label = nvg.getLimitText(theme.name, 9.5f, Fonts.MEDIUM, THEME_ITEM_WIDTH - 24f)
            nvg.drawText(label, screenX + 12f, innerY + itemHeight - 22f, Color.WHITE, 9.5f, Fonts.MEDIUM)

            if (selected) {
                nvg.drawText(
                    LegacyIcon.CHECK,
                    screenX + THEME_ITEM_WIDTH - 18f,
                    innerY + 12f,
                    Color(255, 255, 255, min(255, 180 + (theme.animation.value * 60f).toInt())),
                    12f,
                    Fonts.LEGACYICON
                )
            } else if (hovered) {
                nvg.drawOutlineRoundedRect(screenX, innerY, THEME_ITEM_WIDTH, itemHeight, 10f, 2f, ColorUtils.applyAlpha(accent.color2, 160))
            }

            themeHitboxes.add(CardHitbox(theme, screenX, innerY, THEME_ITEM_WIDTH, itemHeight))
            cardX += THEME_ITEM_WIDTH + THEME_ITEM_SPACING
        }
        nvg.resetScissor()
        nvg.restore()
    }

    private fun drawAccentCarousel(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        nvg: NanoVGManager,
        colorManager: ColorManager,
        palette: ColorPalette,
        currentAccent: AccentColor,
        sectionY: Float
    ) {
        val radius = 10f
        nvg.drawRoundedRect(accentSectionX, sectionY, accentSectionWidth, accentSectionHeight, radius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 165))
        nvg.drawGradientRoundedRect(accentSectionX, sectionY, accentSectionWidth, accentSectionHeight, radius, ColorUtils.applyAlpha(currentAccent.color1, 28), ColorUtils.applyAlpha(currentAccent.color2, 28))

        val innerX = accentSectionX + INNER_PADDING
        val innerY = sectionY + INNER_PADDING
        val visibleWidth = accentSectionWidth - INNER_PADDING * 2f

        val totalWidth = colorManager.colors.size * ACCENT_ITEM_WIDTH + (colorManager.colors.size - 1) * ACCENT_ITEM_SPACING
        accentScroll.maxScroll = max(0f, totalWidth - visibleWidth)

        val scroll = accentScroll.getValue()
        val itemHeight = 76f
        nvg.save()
        nvg.intersectScissor(accentSectionX, sectionY, accentSectionWidth, accentSectionHeight)

        var cardX = innerX + scroll
        for (accent in colorManager.colors) {
            val screenX = cardX
            val hovered = MouseUtils.isInside(mouseX, mouseY, screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight)
            val selected = accent == currentAccent

            accent.animation.setAnimation(if (selected) 1.0f else 0.0f, 18.0)

            nvg.drawRoundedRect(screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight, 10f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), if (hovered || selected) 220 else 190))
            nvg.drawGradientRoundedRect(screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight, 10f, ColorUtils.applyAlpha(accent.color1, if (selected) 220 else 185), ColorUtils.applyAlpha(accent.color2, if (selected) 220 else 185))

            if (selected) {
                nvg.drawText(
                    LegacyIcon.CHECK,
                    screenX + ACCENT_ITEM_WIDTH - 18f,
                    innerY + 10f,
                    Color(255, 255, 255, (accent.animation.value * 255).toInt()),
                    12f,
                    Fonts.LEGACYICON
                )
            } else if (hovered) {
                nvg.drawOutlineRoundedRect(screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight, 10f, 2f, ColorUtils.applyAlpha(accent.color2, 160))
            }

            val label = nvg.getLimitText(accent.name, 8.5f, Fonts.MEDIUM, ACCENT_ITEM_WIDTH - 16f)
            nvg.drawCenteredText(label, screenX + ACCENT_ITEM_WIDTH / 2f, innerY + itemHeight - 18f, Color.WHITE, 8.5f, Fonts.MEDIUM)

            accentHitboxes.add(CardHitbox(accent, screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight))
            cardX += ACCENT_ITEM_WIDTH + ACCENT_ITEM_SPACING
        }
        nvg.resetScissor()
        nvg.restore()
    }

    private fun drawControlCards(mouseX: Int, mouseY: Int, partialTicks: Float, sectionY: Float) {
        var currentY = sectionY
        val cardW = cardWidth - 28f

        for (card in settingCards) {
            card.setBounds(cardX + 14f, currentY, cardW, cardHeight)
            card.draw(mouseX, mouseY, partialTicks)
            currentY += cardHeight + 18f
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (!MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            return
        }

        for (card in settingCards) {
            card.mouseClicked(mouseX, mouseY, mouseButton)
        }

        if (mouseButton != 0) {
            return
        }

        val colorManager = Shindo.getInstance().colorManager

        for (hitbox in themeHitboxes) {
            if (MouseUtils.isInside(mouseX, mouseY, hitbox.x, hitbox.y, hitbox.width, hitbox.height)) {
                colorManager.theme = hitbox.data
                return
            }
        }

        for (hitbox in accentHitboxes) {
            if (MouseUtils.isInside(mouseX, mouseY, hitbox.x, hitbox.y, hitbox.width, hitbox.height)) {
                colorManager.currentColor = hitbox.data
                return
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (card in settingCards) {
            card.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        for (card in settingCards) {
            card.keyTyped(typedChar, keyCode)
        }
        contentScroll.onKey(keyCode)
    }

    private data class CardHitbox<T>(val data: T, val x: Float, val y: Float, val width: Float, val height: Float)

    companion object {
        private const val OUTER_PADDING = 36f
        private const val SECTION_SPACING = 40f
        private const val INNER_PADDING = 18f
        private const val THEME_ITEM_WIDTH = 112f
        private const val THEME_ITEM_SPACING = 18f
        private const val ACCENT_ITEM_WIDTH = 96f
        private const val ACCENT_ITEM_SPACING = 16f
    }
}
