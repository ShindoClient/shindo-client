package me.miki.shindo.gui.mainmenu.impl.welcome

import eu.shoroa.contrib.render.Blur
import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.curve.DecelerateAnimation
import me.miki.shindo.ui.animation.screen.ScreenAlpha
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import java.awt.Color

class AccentColorSelectScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    private val screenAlpha = ScreenAlpha()
    private val scroll = Scroll()
    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0
    private var currentColor: AccentColor
    private var fadeAnimation: Animation? = null

    init {
        currentColor = Shindo.getInstance().colorManager.getColorByName("Default")
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)

        width = 280
        height = 172
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
            setCurrentScene(getSceneByClass(CheckingDataScene::class.java))
        }
    }

    private fun drawNanoVG() {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager
        val colorManager: ColorManager = instance.colorManager

        var offsetX = 0
        var offsetY = 0
        var index = 1

        val panelColor = getPanelColor()
        val controlColor = getControlColor()
        Blur.drawBlur(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 8f);
        nvg!!.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 8f, panelColor)
        nvg.drawCenteredText("Choose a accent color", x + (width / 2f), y + 10f, Color.WHITE, 16f, Fonts.MEDIUM)
        nvg.drawRect(x.toFloat(), y + 27f, width.toFloat(), 1f, Color.WHITE)

        nvg.drawRoundedImage(ResourceLocation("shindo/backgrounds/example-vertical.png"), x + width - 108f, y + 40f, 96f, 96f, 6f)
        drawExampleHud(x + width - 96f, y + 70.5f, currentColor)

        scroll.onScroll()
        scroll.onAnimation()

        nvg.save()
        nvg.scissor(x.toFloat(), y + 28f, width.toFloat(), height - 28f)
        nvg.translate(0f, scroll.getValue())

        for (color in colorManager.getColors()) {
            nvg.drawGradientRoundedRect(x + offsetX + 10f, y + offsetY + 40f, 32f, 32f, 6f, color.getColor1(), color.getColor2())

            color.getAnimation().setAnimation(if (color == currentColor) 1.0f else 0.0f, 16)

            nvg.drawCenteredText(LegacyIcon.CHECK, x + offsetX + 10f + (32 / 2f), y + offsetY + 48f, Color(255, 255, 255, (color.getAnimation().value * 255).toInt()), 16f, Fonts.LEGACYICON)

            offsetX += 40

            if (index % 4 == 0) {
                offsetX = 0
                offsetY += 40
            }

            index++
        }

        scroll.maxScroll = 0f.coerceAtLeast(offsetY - (height - 82f))

        nvg.restore()

        nvg.drawRoundedRect(x + width - 108f, y + height - 26f, 96f, 20f, 6f, controlColor)
        nvg.drawCenteredText("Next", x + width - 108f + (96 / 2f), y + height - 20f, Color.WHITE, 10f, Fonts.REGULAR)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val instance = Shindo.getInstance()
        val colorManager: ColorManager = instance.colorManager

        var offsetX = 0
        var offsetY = scroll.getValue().toInt()
        var index = 1

        for (color in colorManager.getColors()) {
            if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + 28f, width.toFloat(), height - 28f)
                && MouseUtils.isInside(mouseX, mouseY, x + offsetX + 10f, y + offsetY + 40f, 32f, 32f)
                && mouseButton == 0) {
                currentColor = color
            }

            offsetX += 40

            if (index % 4 == 0) {
                offsetX = 0
                offsetY += 40
            }

            index++
        }

        if (MouseUtils.isInside(mouseX, mouseY, x + width - 86f, y + height - 26f, 80f, 20f) && mouseButton == 0) {
            Shindo.getInstance().colorManager.setCurrentColor(currentColor)
            fadeAnimation!!.setDirection(Direction.BACKWARDS)
        }
    }

    private fun drawExampleHud(x: Float, y: Float, accentColor: AccentColor) {
        val nvg = Shindo.getInstance().nanoVGManager

        val width = 71f
        val height = 34f

        nvg!!.drawGradientRoundedRect(x, y, width, height, 5f, ColorUtils.applyAlpha(accentColor.getColor1(), 220), ColorUtils.applyAlpha(accentColor.getColor2(), 220))

        nvg.drawText("X: 190", x + 3.9f, y + 3.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
        nvg.drawText("Y: 60", x + 3.9f, y + 10.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
        nvg.drawText("Z: 20", x + 3.9f, y + 17.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
        nvg.drawText("Biome: Plains", x + 3.9f, y + 24.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
    }
}

