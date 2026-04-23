package me.miki.shindo.gui.modmenu.category.impl.setting.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.gui.modmenu.render.ModMenuClipCoordinator
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.GlobalAnimationSettings
import me.miki.shindo.ui.comp.buttons.CompSettingButton
import me.miki.shindo.ui.comp.buttons.CompToggleButton
import me.miki.shindo.ui.comp.inputs.CompComboBox
import me.miki.shindo.ui.comp.inputs.CompSlider
import me.miki.shindo.ui.comp.selectors.CompAccentColorSelector
import me.miki.shindo.ui.comp.selectors.CompThemeSelector
import me.miki.shindo.ui.comp.templates.CompLabel
import me.miki.shindo.ui.comp.templates.PanelStyle
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import kotlin.math.max

class AppearanceScene(parent: SettingsCategory) :
    SettingScene(parent, TranslateText.APPEARANCE, TranslateText.APPEARANCE_DESCRIPTION, LegacyIcon.MONITOR) {

    private val contentScroll = Scroll()

    private lateinit var themeSelector: CompThemeSelector
    private lateinit var accentColorSelector: CompAccentColorSelector
    private lateinit var themeTitle: CompLabel
    private lateinit var accentTitle: CompLabel

    private lateinit var modTheme: CompComboBox
    private lateinit var uiBlur: CompToggleButton
    private lateinit var blurStrength: CompSlider
    private lateinit var clientAnimations: CompToggleButton

    private val settingCards = ArrayList<CompSettingButton>()

    private var themeSectionY = 0f
    private var accentSectionY = 0f
    private var cardY = 0f
    private var cardWidth = 0f
    private var cardHeight = 54f

    override fun initGui() {
        val instance = Shindo.getInstance()
        val colorManager = instance.colorManager
        val palette = colorManager.getPalette()

        modTheme = CompComboBox(110f, requireNotNull(InternalSettingsMod.instance.modThemeSetting))
        uiBlur = CompToggleButton(requireNotNull(InternalSettingsMod.instance.getBlurSetting()))
        blurStrength = CompSlider(0f, 0f, requireNotNull(InternalSettingsMod.instance.getBlurStrengthSetting()), 75f)
        clientAnimations = CompToggleButton(requireNotNull(InternalSettingsMod.instance.getAnimationsSetting()))

        themeSelector = CompThemeSelector().apply {
            setSelectedTheme(colorManager.getTheme())
            setBackgroundColor(ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220))
            setBorder(1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210))
            setShadowStrength(7)
            setStyle(PanelStyle.CARD)
            setOnThemeSelected { theme ->
                colorManager.setTheme(theme)
            }
        }

        accentColorSelector = CompAccentColorSelector(
            accentColors = colorManager.getColors()
        ).apply {
            setSelectedColor(colorManager.getCurrentColor())
            setBackgroundColor(ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220))
            setBorder(1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210))
            setShadowStrength(7)
            setStyle(PanelStyle.CARD)
            setOnColorSelected { accent ->
                colorManager.setCurrentColor(accent)
            }
        }

        themeTitle = CompLabel(0f, 0f, TranslateText.THEME.getText())
            .setFontSize(12.5f)

        accentTitle = CompLabel(0f, 0f, TranslateText.ACCENT_COLOR.getText())
            .setFontSize(12.5f)

        settingCards.clear()
        settingCards.add(
            CompSettingButton(0f, { TranslateText.HUD_THEME.getText() }, { TranslateText.STYLE.getText() })
                .trailing(modTheme)
        )

        settingCards.add(
            CompSettingButton(0f, { TranslateText.UI_BLUR.getText() }, { TranslateText.SMOOTH.getText() })
                .trailing(uiBlur)
                .onClickAction {
                    val setting = uiBlur.getSetting()
                    setting.setToggled(!setting.isToggled())
                }
        )

        settingCards.add(
            CompSettingButton(0f, { TranslateText.BLUR_STRENGTH.getText() }, { TranslateText.SMOOTH.getText() })
                .trailing(blurStrength)
                .onClickAction {
                    if (InternalSettingsMod.instance.getBlurSetting()?.isToggled() == true) {
                        val setting = blurStrength.getSetting()
                        setting.setValue(setting.getValue())
                    }
                }
        )

        settingCards.add(
            CompSettingButton(0f, { TranslateText.ANIMATION.getText() }, { TranslateText.SMOOTH.getText() })
                .trailing(clientAnimations)
                .onClickAction {
                    val setting = clientAnimations.getSetting()
                    setting.setToggled(!setting.isToggled())
                    GlobalAnimationSettings.enabled = setting.isToggled()
                }
        )

        contentScroll.resetAll()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val colorManager = instance.colorManager
        val palette = colorManager.getPalette()
        val currentAccent = colorManager.getCurrentColor()
        val currentTheme = colorManager.getTheme()

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (baseWidth <= 0f || baseHeight <= 0f) {
            return
        }

        themeSelector.setSelectedTheme(currentTheme)
        accentColorSelector.setSelectedColor(currentAccent)

        val containerRadius = 12f
        nvg.drawShadow(baseX, baseY, baseWidth, baseHeight, containerRadius, 7)
        nvg.drawRoundedRect(
            baseX,
            baseY,
            baseWidth,
            baseHeight,
            containerRadius,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210)
        )
        nvg.drawRoundedRect(
            baseX + 1f,
            baseY + 1f,
            baseWidth - 2f,
            baseHeight - 2f,
            containerRadius - 1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230)
        )

        val top = baseY + OUTER_PADDING
        val themeHeight = 122f
        val accentHeight = 120f
        val sectionWidth = max(0f, baseWidth - OUTER_PADDING * 2f)

        themeSectionY = top
        accentSectionY = themeSectionY + themeHeight + SECTION_SPACING
        cardY = accentSectionY + accentHeight + 10f
        cardWidth = sectionWidth

        val contentHeight =
            OUTER_PADDING + themeHeight + SECTION_SPACING + accentHeight + 10f + ((cardHeight * settingCards.size) + 18f) + OUTER_PADDING * 2
        contentScroll.maxScroll = max(0f, contentHeight - baseHeight)

        if (MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight) &&
            !MouseUtils.isInside(
                mouseX,
                mouseY,
                baseX + OUTER_PADDING,
                themeSectionY + contentScroll.getValue(),
                sectionWidth,
                themeHeight
            ) &&
            !MouseUtils.isInside(
                mouseX,
                mouseY,
                baseX + OUTER_PADDING,
                accentSectionY + contentScroll.getValue(),
                sectionWidth,
                accentHeight
            )
        ) {
            contentScroll.onScroll()
        }

        contentScroll.onAnimation()

        val verticalScroll = contentScroll.getValue()
        val themeScreenY = themeSectionY + verticalScroll
        val accentScreenY = accentSectionY + verticalScroll
        val controlsScreenY = cardY + verticalScroll

        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = baseX,
            y = baseY,
            width = baseWidth,
            height = baseHeight
        ) {
            themeTitle.setX(baseX + OUTER_PADDING)
            themeTitle.setY(themeScreenY - 26f)
            themeTitle.draw(mouseX, mouseY, partialTicks)

            themeSelector.setBounds(baseX + OUTER_PADDING, themeScreenY, sectionWidth, themeHeight)
            themeSelector.draw(mouseX, mouseY, partialTicks)

            accentTitle.setX(baseX + OUTER_PADDING)
            accentTitle.setY(accentScreenY - 26f)
            accentTitle.draw(mouseX, mouseY, partialTicks)

            accentColorSelector.setBounds(baseX + OUTER_PADDING, accentScreenY, sectionWidth, accentHeight)
            accentColorSelector.draw(mouseX, mouseY, partialTicks)

            drawControlCards(mouseX, mouseY, partialTicks, controlsScreenY)
        }

        nvg.drawScrollbar(
            baseX,
            baseY,
            baseWidth,
            baseHeight,
            contentHeight,
            verticalScroll,
            palette,
            currentAccent,
            30f
        )
    }


    private fun drawControlCards(mouseX: Int, mouseY: Int, partialTicks: Float, sectionY: Float) {
        var currentY = sectionY
        val cardW = cardWidth - 28f
        val baseX = x.toFloat()

        for (card in settingCards) {
            card.setBounds(baseX + OUTER_PADDING + 14f, currentY, cardW, cardHeight)
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

        themeSelector.mouseClicked(mouseX, mouseY, mouseButton)
        accentColorSelector.mouseClicked(mouseX, mouseY, mouseButton)

        for (card in settingCards) {
            card.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        themeSelector.mouseReleased(mouseX, mouseY, mouseButton)
        accentColorSelector.mouseReleased(mouseX, mouseY, mouseButton)
        for (card in settingCards) {
            card.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        themeSelector.keyTyped(typedChar, keyCode)
        accentColorSelector.keyTyped(typedChar, keyCode)
        for (card in settingCards) {
            card.keyTyped(typedChar, keyCode)
        }
        contentScroll.onKey(keyCode)
    }

    companion object {
        private const val OUTER_PADDING = 36f
        private const val SECTION_SPACING = 40f
    }
}
