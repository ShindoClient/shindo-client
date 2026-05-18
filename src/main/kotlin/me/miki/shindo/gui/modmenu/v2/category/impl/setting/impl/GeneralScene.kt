package me.miki.shindo.gui.modmenu.v2.category.impl.setting.impl

import me.miki.shindo.gui.modmenu.v2.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.v2.category.impl.setting.SettingScene
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.components.v2.buttons.CompSettingButton
import me.miki.shindo.ui.components.v2.buttons.CompToggleButton
import me.miki.shindo.ui.components.v2.inputs.CompKeybind
import me.miki.shindo.ui.components.v2.layout.CompScrollableContainer
import kotlin.math.max

class GeneralScene(
    parent: SettingsCategory,
) : SettingScene(
        parent,
        TranslateText.GENERAL,
        TranslateText.GENERAL_DESCRIPTION,
        LegacyIcon.LIST,
    ) {
    private lateinit var container: CompScrollableContainer
    private lateinit var modMenuKeybind: CompKeybind
    private lateinit var clickEffectSetting: CompToggleButton
    private lateinit var soundsUISetting: CompToggleButton
    private lateinit var mcFontSetting: CompToggleButton
    private lateinit var borderlessSetting: CompToggleButton
    private val settingCards = ArrayList<CompSettingButton>()

    override fun initGui() {
        val settingsMod = InternalSettingsMod.instance

        modMenuKeybind =
            CompKeybind(
                75f,
                settingsMod.getModMenuKeybindSetting()
                    ?: throw IllegalStateException("Mod menu keybind setting missing"),
            )
        clickEffectSetting =
            CompToggleButton(
                settingsMod.getClickEffectsSetting()
                    ?: throw IllegalStateException("Click effect setting missing"),
            )
        soundsUISetting =
            CompToggleButton(
                settingsMod.getSoundsUISetting()
                    ?: throw IllegalStateException("UI sounds setting missing"),
            )
        mcFontSetting =
            CompToggleButton(
                settingsMod.mCHUDFont
                    ?: throw IllegalStateException("MC font setting missing"),
            )
        borderlessSetting =
            CompToggleButton(
                settingsMod.getBorderlessFullscreenSetting()
                    ?: throw IllegalStateException("Borderless fullscreen setting missing"),
            )

        container =
            CompScrollableContainer()
                .setScrollbarGutter(14f)

        settingCards.clear()
        settingCards.add(
            CompSettingButton(
                0f,
                { TranslateText.OPEN_MOD_MENU.getText() },
                { TranslateText.OPEN_MOD_MENU_DESCRIPTION.getText() },
            ).trailing(modMenuKeybind),
        )
        settingCards.add(
            CompSettingButton(
                0f,
                { TranslateText.CLICK_EFFECT.getText() },
                { TranslateText.CLICK_EFFECT_DESCRIPTION.getText() },
            ).trailing(clickEffectSetting)
                .onClickAction {
                    val setting = clickEffectSetting.getSetting()
                    setting.setToggled(!setting.isToggled())
                },
        )
        settingCards.add(
            CompSettingButton(
                0f,
                { TranslateText.UI_SOUNDS.getText() },
                { TranslateText.UI_SOUNDS_DESCRIPTION.getText() },
            ).trailing(soundsUISetting)
                .onClickAction {
                    val setting = soundsUISetting.getSetting()
                    setting.setToggled(!setting.isToggled())
                },
        )
        settingCards.add(
            CompSettingButton(0f, { TranslateText.MC_FONT.getText() }, { TranslateText.MC_FONT_DESCRIPTION.getText() })
                .trailing(mcFontSetting)
                .onClickAction {
                    val setting = mcFontSetting.getSetting()
                    setting.setToggled(!setting.isToggled())
                },
        )
        settingCards.add(
            CompSettingButton(
                0f,
                { TranslateText.BORDERLESS_FULSCREEN.getText() },
                { TranslateText.BORDERLESS_FULLSCREEN_DESCRIPTION.getText() },
            ).trailing(borderlessSetting)
                .onClickAction {
                    val setting = borderlessSetting.getSetting()
                    setting.setToggled(!setting.isToggled())
                },
        )
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
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
        val totalContentHeight = padding * 2f + cardCount * cardHeight + max(0, cardCount - 1) * cardSpacing

        container.renderWithViewport(
            mouseX,
            mouseY,
            partialTicks,
            totalContentHeight,
        ) { mouseXInner, mouseYInner, partialInner, scrollValue, viewport ->
            var currentY = viewport.y + padding + scrollValue
            val cardWidth = viewport.width - 10f

            for (card in settingCards) {
                card.setBounds(viewport.x + 5f, currentY, cardWidth, cardHeight)
                card.draw(mouseXInner, mouseYInner, partialInner)
                currentY += cardHeight + cardSpacing
            }
        }
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        container.mouseClicked(mouseX, mouseY, mouseButton)

        for (card in settingCards) {
            card.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    override fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        container.mouseReleased(mouseX, mouseY, mouseButton)

        for (card in settingCards) {
            card.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        container.keyTyped(typedChar, keyCode)

        if (modMenuKeybind.isBinding()) {
            modMenuKeybind.keyTyped(typedChar, keyCode)
        }
        for (card in settingCards) {
            card.keyTyped(typedChar, keyCode)
        }
    }
}
