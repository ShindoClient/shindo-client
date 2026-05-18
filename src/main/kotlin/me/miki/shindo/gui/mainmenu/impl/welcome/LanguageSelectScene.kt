package me.miki.shindo.gui.mainmenu.impl.welcome

import eu.shoroa.contrib.render.Blur
import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.language.Language
import me.miki.shindo.management.language.LanguageManager
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.DecelerateAnimation
import me.miki.shindo.ui.animation.v2.screen.ScreenAlpha
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class LanguageSelectScene(
    parent: GuiShindoMainMenu,
) : MainMenuScene(parent) {
    private val screenAlpha = ScreenAlpha()
    private val scroll = Scroll()
    private val languageManager: LanguageManager = Shindo.getInstance().getLanguageManager()
    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0
    private var fadeAnimation: Animation? = null
    private var currentLanguage: Language = languageManager.getCurrentLanguage()

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val sr = ScaledResolution(mc)

        width = 280
        height = 146
        x = sr.scaledWidth / 2 - (width / 2)
        y = sr.scaledHeight / 2 - (height / 2)

        if (fadeAnimation == null) {
            fadeAnimation = DecelerateAnimation(800, 1.0)
            fadeAnimation!!.setDirection(Direction.FORWARDS)
            fadeAnimation!!.reset()
        }

        BlurUtils.drawBlurScreen(14F)

        screenAlpha.wrap(Runnable { drawNanoVG() }, fadeAnimation!!.getValueFloat())

        if (fadeAnimation!!.isDone(Direction.BACKWARDS)) {
            setCurrentScene(getSceneByClass(ThemeSelectScene::class.java))
        }
    }

    private fun drawNanoVG() {
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager!!
        val currentColor: AccentColor = instance.getColorManager().getCurrentColor()

        var offsetX = 0
        var index = 1

        val panelColor = getPanelColor()
        val controlColor = getControlColor()
        Blur.drawBlur(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 8f)
        nvg.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 8f, panelColor)
        nvg.drawCenteredText("Choose a Language", x + (width / 2f), y + 10f, Color.WHITE, 16f, Fonts.MEDIUM)
        nvg.drawRect(x.toFloat(), y + 27f, width.toFloat(), 1f, Color.WHITE)

        scroll.onScroll()
        scroll.onAnimation()

        nvg.save()
        nvg.scissor(x.toFloat(), y + 27f, width.toFloat(), height - 27f)
        nvg.translate(scroll.getValue(), 0f)

        for (lang in Language.values()) {
            nvg.drawRoundedImage(lang.getFlag(), x + offsetX + 14f, y + 42f, 90f, 56f, 4f)
            nvg.drawCenteredText(lang.name, x + offsetX + 14f + (90 / 2f), y + 104f, Color.WHITE, 7f, Fonts.REGULAR)
            if (lang == currentLanguage) {
                nvg.drawGradientOutlineRoundedRect(
                    x + offsetX + 14f,
                    y + 42f,
                    90f,
                    56f,
                    6f,
                    2f,
                    currentColor.getColor1(),
                    currentColor.getColor2(),
                )
            }
            offsetX += 102
            index++
        }

        scroll.maxScroll = (index - 3.58f) * 102

        nvg.restore()

        nvg.drawRoundedRect(x + width - 86f, y + height - 26f, 80f, 20f, 6f, controlColor)
        nvg.drawCenteredText("Next", x + width - 86f + (80 / 2f), y + height - 20f, Color.WHITE, 10f, Fonts.REGULAR)
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        var offsetX = scroll.getValue()

        for (lang in Language.values()) {
            if (MouseUtils.isInside(mouseX, mouseY, x + offsetX + 14f, y + 42f, 90f, 56f) && mouseButton == 0) {
                currentLanguage = lang
            }

            offsetX += 102
        }

        if (MouseUtils.isInside(mouseX, mouseY, x + width - 86f, y + height - 26f, 80f, 20f) && mouseButton == 0) {
            Shindo.getInstance().getLanguageManager().setCurrentLanguage(currentLanguage)
            fadeAnimation!!.setDirection(Direction.BACKWARDS)
        }
    }
}