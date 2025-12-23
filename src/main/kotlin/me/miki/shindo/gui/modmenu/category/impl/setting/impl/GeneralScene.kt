package me.miki.shindo.gui.modmenu.category.impl.setting.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.ui.comp.impl.CompKeybind
import me.miki.shindo.ui.comp.impl.CompSettingButton
import me.miki.shindo.ui.comp.impl.CompToggleButton
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import kotlin.math.max

class GeneralScene(parent: SettingsCategory) :
    SettingScene(parent, TranslateText.GENERAL, TranslateText.GENERAL_DESCRIPTION, me.miki.shindo.management.nanovg.font.LegacyIcon.LIST) {

    private val contentScroll = Scroll()
    private lateinit var modMenuKeybind: CompKeybind
    private lateinit var clickEffectSetting: CompToggleButton
    private lateinit var soundsUISetting: CompToggleButton
    private lateinit var mcFontSetting: CompToggleButton
    private lateinit var borderlessSetting: CompToggleButton
    private val settingCards = ArrayList<CompSettingButton>()

    override fun initGui() {
        modMenuKeybind = CompKeybind(75f, InternalSettingsMod.getInstance().modMenuKeybindSetting)
        clickEffectSetting = CompToggleButton(InternalSettingsMod.getInstance().clickEffectsSetting)
        soundsUISetting = CompToggleButton(InternalSettingsMod.getInstance().soundsUISetting)
        mcFontSetting = CompToggleButton(InternalSettingsMod.getInstance().mchudFont)
        borderlessSetting = CompToggleButton(InternalSettingsMod.getInstance().borderlessFullscreenSetting)
        contentScroll.resetAll()

        settingCards.clear()
        settingCards.add(
            CompSettingButton(0f, { TranslateText.OPEN_MOD_MENU.text }, { TranslateText.OPEN_MOD_MENU_DESCRIPTION.text })
                .trailing(modMenuKeybind)
        )
        settingCards.add(
            CompSettingButton(0f, { TranslateText.CLICK_EFFECT.text }, { TranslateText.CLICK_EFFECT_DESCRIPTION.text })
                .trailing(clickEffectSetting)
                .onClick {
                    val setting = clickEffectSetting.getSetting()
                    setting.setToggled(!setting.isToggled())
                }
        )
        settingCards.add(
            CompSettingButton(0f, { TranslateText.UI_SOUNDS.text }, { TranslateText.UI_SOUNDS_DESCRIPTION.text })
                .trailing(soundsUISetting)
                .onClick {
                    val setting = soundsUISetting.getSetting()
                    setting.setToggled(!setting.isToggled())
                }
        )
        settingCards.add(
            CompSettingButton(0f, { TranslateText.MC_FONT.text }, { TranslateText.MC_FONT_DESCRIPTION.text })
                .trailing(mcFontSetting)
                .onClick {
                    val setting = mcFontSetting.getSetting()
                    setting.setToggled(!setting.isToggled())
                }
        )
        settingCards.add(
            CompSettingButton(0f, { TranslateText.BORDERLESS_FULSCREEN.text }, { TranslateText.BORDERLESS_FULLSCREEN_DESCRIPTION.text })
                .trailing(borderlessSetting)
                .onClick {
                    val setting = borderlessSetting.getSetting()
                    setting.setToggled(!setting.isToggled())
                }
        )
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val colorManager = instance.colorManager
        val palette = colorManager.palette
        val accentColor = colorManager.currentColor

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (baseHeight <= 0f || baseWidth <= 0f) {
            return
        }

        val radius = 12f
        nvg.drawShadow(baseX, baseY, baseWidth, baseHeight, radius, 7)
        nvg.drawRoundedRect(baseX, baseY, baseWidth, baseHeight, radius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210))
        nvg.drawRoundedRect(baseX + 1f, baseY + 1f, baseWidth - 2f, baseHeight - 2f, radius - 1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))

        val cardHeight = 52f
        val cardSpacing = 14f
        val padding = 18f
        val cardCount = settingCards.size

        val contentHeight = padding * 2f + cardCount * cardHeight + max(0, cardCount - 1) * cardSpacing
        contentScroll.maxScroll = max(0f, contentHeight - baseHeight)

        if (MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            contentScroll.onScroll()
        }
        contentScroll.onAnimation()
        val scrollValue = contentScroll.getValue()

        nvg.save()
        nvg.scissor(baseX, baseY, baseWidth, baseHeight)

        var currentY = baseY + padding + scrollValue
        val cardWidth = baseWidth - 28f

        for (card in settingCards) {
            card.setBounds(baseX + 14f, currentY, cardWidth, cardHeight)
            card.draw(mouseX, mouseY, partialTicks)
            currentY += cardHeight + cardSpacing
        }

        nvg.restore()

        nvg.drawScrollbar(baseX, baseY, baseWidth, baseHeight, contentHeight, scrollValue, palette, accentColor, 24f)
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
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (card in settingCards) {
            card.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (modMenuKeybind.isBinding) {
            modMenuKeybind.keyTyped(typedChar, keyCode)
        }
        for (card in settingCards) {
            card.keyTyped(typedChar, keyCode)
        }
        contentScroll.onKey(keyCode)
    }
}
