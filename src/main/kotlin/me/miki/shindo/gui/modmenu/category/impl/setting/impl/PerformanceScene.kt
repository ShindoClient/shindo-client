package me.miki.shindo.gui.modmenu.category.impl.setting.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.buttons.CompSettingButton
import me.miki.shindo.ui.comp.buttons.CompToggleButtonWithRestart
import me.miki.shindo.ui.comp.layout.CompScrollableContainer
import me.miki.shindo.ui.comp.templates.PanelStyle
import me.miki.shindo.utils.ColorUtils

class PerformanceScene(parent: SettingsCategory) :
    SettingScene(parent, TranslateText.PERFORMANCE, TranslateText.PERFORMANCE_DESCRIPTION, LegacyIcon.PERFORMANCE) {

    private lateinit var container: CompScrollableContainer
    private lateinit var textureOptimizationToggle: CompToggleButtonWithRestart
    private val settingCards = ArrayList<CompSettingButton>()

    override fun initGui() {
        val instance = Shindo.getInstance()
        val colorManager = instance.colorManager
        val palette = colorManager.getPalette()
        val settingsMod = InternalSettingsMod.instance

        textureOptimizationToggle = CompToggleButtonWithRestart(
            settingsMod.getTextureOptimizationSetting()
                ?: throw IllegalStateException("Texture optimization setting missing"),
            requiresRestart = true
        )

        container = CompScrollableContainer().apply {
            setScrollbarGutter(14f)
            setBackgroundColor(ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210))
            setBorder(1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))
            setShadowStrength(7)
            setStyle(PanelStyle.PANEL)
        }
        settingCards.clear()
        settingCards.add(
            CompSettingButton(
                0f,
                { TranslateText.PERFORMANCE_TEXTURE_OPTIMIZATION.getText() },
                { TranslateText.PERFORMANCE_TEXTURE_OPTIMIZATION_DESCRIPTION.getText() })
                .trailing(textureOptimizationToggle)
                .onClick {
                    val setting = textureOptimizationToggle.getSetting()
                    setting.setToggled(!setting.isToggled())
                    textureOptimizationToggle.setShowWarning(true)
                }
        )
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (baseHeight <= 0f || baseWidth <= 0f) {
            return
        }

        container.setBounds(baseX, baseY, baseWidth, baseHeight)

        val cardHeight = 52f
        val cardSpacing = 14f
        val padding = 18f
        val cardCount = settingCards.size
        val totalContentHeight = padding * 2f + cardCount * cardHeight + kotlin.math.max(0, cardCount - 1) * cardSpacing

        container.render(
            mouseX,
            mouseY,
            partialTicks,
            totalContentHeight
        ) { mouseXInner, mouseYInner, partialInner, scrollValue ->
            var currentY = baseY + padding + scrollValue
            val cardWidth = baseWidth - 28f

            for (card in settingCards) {
                card.setBounds(baseX + 14f, currentY, cardWidth, cardHeight)
                card.draw(mouseXInner, mouseYInner, partialInner)
                currentY += cardHeight + cardSpacing
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        container.mouseClicked(mouseX, mouseY, mouseButton)

        for (card in settingCards) {
            card.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        container.mouseReleased(mouseX, mouseY, mouseButton)

        for (card in settingCards) {
            card.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        container.keyTyped(typedChar, keyCode)

        for (card in settingCards) {
            card.keyTyped(typedChar, keyCode)
        }
    }
}
