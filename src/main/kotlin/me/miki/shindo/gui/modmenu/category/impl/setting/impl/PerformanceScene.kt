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
import me.miki.shindo.utils.mouse.MouseUtils

class PerformanceScene(parent: SettingsCategory) :
    SettingScene(parent, TranslateText.PERFORMANCE, TranslateText.PERFORMANCE_DESCRIPTION, LegacyIcon.PERFORMANCE) {

    private lateinit var container: CompScrollableContainer
    private lateinit var textureOptimizationToggle: CompToggleButtonWithRestart
    private lateinit var chunkOptimizationToggle: CompToggleButtonWithRestart
    private lateinit var logOptimizationToggle: CompToggleButtonWithRestart
    private lateinit var soundOptimizationToggle: CompToggleButtonWithRestart
    private lateinit var networkOptimizationToggle: CompToggleButtonWithRestart
    private val settingCards = ArrayList<CompSettingButton>()

    override fun initGui() {
        val settingsMod = InternalSettingsMod.instance
        
        // Inicializa toggles com aviso de reinício
        textureOptimizationToggle = CompToggleButtonWithRestart(
            settingsMod.getTextureOptimizationSetting()
                ?: throw IllegalStateException("Texture optimization setting missing"),
            requiresRestart = true
        )
        chunkOptimizationToggle = CompToggleButtonWithRestart(
            settingsMod.getChunkOptimizationSetting()
                ?: throw IllegalStateException("Chunk optimization setting missing"),
            requiresRestart = true
        )
        logOptimizationToggle = CompToggleButtonWithRestart(
            settingsMod.getLogOptimizationSetting()
                ?: throw IllegalStateException("Log optimization setting missing"),
            requiresRestart = false // Logs não precisam de reinício
        )
        soundOptimizationToggle = CompToggleButtonWithRestart(
            settingsMod.getSoundOptimizationSetting()
                ?: throw IllegalStateException("Sound optimization setting missing"),
            requiresRestart = true
        )
        networkOptimizationToggle = CompToggleButtonWithRestart(
            settingsMod.getNetworkOptimizationSetting()
                ?: throw IllegalStateException("Network optimization setting missing"),
            requiresRestart = true
        )

        // Inicializa container scrollável
        container = CompScrollableContainer()

        settingCards.clear()
        settingCards.add(
            CompSettingButton(0f, { TranslateText.PERFORMANCE_TEXTURE_OPTIMIZATION.text }, { TranslateText.PERFORMANCE_TEXTURE_OPTIMIZATION_DESCRIPTION.text })
                .trailing(textureOptimizationToggle)
                .onClick {
                    val setting = textureOptimizationToggle.getSetting()
                    setting.setToggled(!setting.isToggled())
                    textureOptimizationToggle.setShowWarning(true)
                }
        )
        settingCards.add(
            CompSettingButton(0f, { TranslateText.PERFORMANCE_CHUNK_OPTIMIZATION.text }, { TranslateText.PERFORMANCE_CHUNK_OPTIMIZATION_DESCRIPTION.text })
                .trailing(chunkOptimizationToggle)
                .onClick {
                    val setting = chunkOptimizationToggle.getSetting()
                    setting.setToggled(!setting.isToggled())
                    chunkOptimizationToggle.setShowWarning(true)
                }
        )
        settingCards.add(
            CompSettingButton(0f, { TranslateText.PERFORMANCE_LOG_OPTIMIZATION.text }, { TranslateText.PERFORMANCE_LOG_OPTIMIZATION_DESCRIPTION.text })
                .trailing(logOptimizationToggle)
                .onClick {
                    val setting = logOptimizationToggle.getSetting()
                    setting.setToggled(!setting.isToggled())
                }
        )
        settingCards.add(
            CompSettingButton(0f, { TranslateText.PERFORMANCE_SOUND_OPTIMIZATION.text }, { TranslateText.PERFORMANCE_SOUND_OPTIMIZATION_DESCRIPTION.text })
                .trailing(soundOptimizationToggle)
                .onClick {
                    val setting = soundOptimizationToggle.getSetting()
                    setting.setToggled(!setting.isToggled())
                    soundOptimizationToggle.setShowWarning(true)
                }
        )
        settingCards.add(
            CompSettingButton(0f, { TranslateText.PERFORMANCE_NETWORK_OPTIMIZATION.text }, { TranslateText.PERFORMANCE_NETWORK_OPTIMIZATION_DESCRIPTION.text })
                .trailing(networkOptimizationToggle)
                .onClick {
                    val setting = networkOptimizationToggle.getSetting()
                    setting.setToggled(!setting.isToggled())
                    networkOptimizationToggle.setShowWarning(true)
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

        container.setContentHeight(totalContentHeight)

        // Renderiza os cards dentro do container
        container.drawScrollableContent = { mouseX, mouseY, partialTicks, scrollValue ->
            var currentY = baseY + padding + scrollValue
            val cardWidth = baseWidth - 28f

            for (card in settingCards) {
                card.setBounds(baseX + 14f, currentY, cardWidth, cardHeight)
                card.draw(mouseX, mouseY, partialTicks)
                currentY += cardHeight + cardSpacing
            }
        }

        container.draw(mouseX, mouseY, partialTicks)
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
