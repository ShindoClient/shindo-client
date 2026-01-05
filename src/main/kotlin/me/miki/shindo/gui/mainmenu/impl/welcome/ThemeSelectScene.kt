package me.miki.shindo.gui.mainmenu.impl.welcome

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.Theme
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation
import me.miki.shindo.utils.buffer.ScreenAlpha
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class ThemeSelectScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    private val screenAlpha = ScreenAlpha()
    private val scroll = Scroll()
    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0
    private var fadeAnimation: Animation? = null
    private var currentTheme: Theme = Theme.DARK

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
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

        BlurUtils.drawBlurScreen(14)

        screenAlpha.wrap({ drawNanoVG() }, fadeAnimation!!.valueFloat)

        if (fadeAnimation!!.isDone(Direction.BACKWARDS)) {
            setCurrentScene(getSceneByClass(AccentColorSelectScene::class.java))
        }
    }

    private fun drawNanoVG() {
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager
        val currentColor: AccentColor = instance.colorManager.currentColor

        var offsetX = 0
        var index = 1

        val panelColor = getPanelColor()
        val controlColor = getControlColor()
        nvg.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 8f, panelColor)
        nvg.drawCenteredText("Choose a theme", x + (width / 2f), y + 10f, Color.WHITE, 16f, Fonts.MEDIUM)
        nvg.drawRect(x.toFloat(), y + 27f, width.toFloat(), 1f, Color.WHITE)

        scroll.onScroll()
        scroll.onAnimation()

        nvg.save()
        nvg.scissor(x.toFloat(), y + 27f, width.toFloat(), height - 27f)
        nvg.translate(scroll.value, 0f)

        for (theme in Theme.values()) {
            theme.animation.setAnimation(if (currentTheme == theme) 1.0f else 0.0f, 16)

            drawModMenuExample(x + offsetX + 14f, y + 42f, theme)
            nvg.save()
            nvg.setAlpha(theme.animation.value)
            nvg.drawGradientOutlineRoundedRect(x + offsetX + 14f, y + 42f, 90f, 56f, 6f, 1f, currentColor.color1, currentColor.color2)
            nvg.restore()
            nvg.drawCenteredText(theme.name, x + offsetX + 14f + (90 / 2f), y + 104f, Color.WHITE, 9.5f, Fonts.REGULAR)

            offsetX += 102
            index++
        }

        scroll.maxScroll = (index - 3.58f) * 102

        nvg.restore()

        nvg.drawRoundedRect(x + width - 86f, y + height - 26f, 80f, 20f, 6f, controlColor)
        nvg.drawCenteredText("Next", x + width - 86f + (80 / 2f), y + height - 20f, Color.WHITE, 10f, Fonts.REGULAR)
    }

    private fun drawModMenuExample(x: Float, y: Float, theme: Theme) {
        val nvg = Shindo.getInstance().nanoVGManager

        val width = 90f
        val height = 56f
        var offsetY = 0

        nvg.drawRoundedRect(x, y, width, height, 6f, theme.normalBackgroundColor)
        nvg.drawRoundedRectVarying(x, y, 12f, height, 6f, 0f, 6f, 0f, theme.darkBackgroundColor)

        for (i in 0..2) {
            nvg.drawRoundedRect(x + 15f, y + offsetY + 6f, width - 20f, 12f, 2.5f, theme.darkBackgroundColor)
            nvg.drawRoundedRect(x + 17f, y + offsetY + 7.5f, 9f, 9f, 2f, theme.normalBackgroundColor)

            offsetY += 16
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        var offsetX = scroll.value

        for (theme in Theme.values()) {
            if (MouseUtils.isInside(mouseX, mouseY, x + offsetX + 14f, y + 42f, 90f, 56f) && mouseButton == 0) {
                currentTheme = theme
            }

            offsetX += 102
        }

        if (MouseUtils.isInside(mouseX, mouseY, x + width - 86f, y + height - 26f, 80f, 20f) && mouseButton == 0) {
            Shindo.getInstance().colorManager.theme = currentTheme
            fadeAnimation!!.setDirection(Direction.BACKWARDS)
        }
    }
}
