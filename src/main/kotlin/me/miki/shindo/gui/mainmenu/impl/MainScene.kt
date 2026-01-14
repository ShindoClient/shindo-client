package me.miki.shindo.gui.mainmenu.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class MainScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    private val singlePlayerAnimation = SimpleAnimation()
    private val multiplayerAnimation = SimpleAnimation()
    private val optionsAnimation = SimpleAnimation()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()

        if (instance.updateNeeded) {
            instance.updateNeeded = false
            setCurrentScene(getSceneByClass(UpdateScene::class.java))
        }
        val nvg = instance.nanoVGManager

        nvg!!.setupAndDraw(Runnable { drawNanoVG(instance, nvg, mouseX, mouseY) })
    }

    private fun drawNanoVG(instance: Shindo, nvg: NanoVGManager, mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        val centerX = sr.scaledWidth / 2f
        val yPos = sr.scaledHeight / 2f - 22
        val width = 180f
        val height = 20f
        val spacing = 26f

        nvg.drawCenteredText(LegacyIcon.SHINDO, centerX, sr.scaledHeight / 2f - (nvg.getTextHeight(LegacyIcon.SHINDO, 54f, Fonts.LEGACYICON) / 2) - 60, Color.WHITE, 54f, Fonts.LEGACYICON)

        singlePlayerAnimation.setAnimation(if (MouseUtils.isInside(mouseX, mouseY, centerX - (width / 2f), yPos, width, height)) 1.0f else 0.0f, 16.0)
        drawMenuButton(nvg, centerX, yPos, width, height, TranslateText.SINGLEPLAYER.text, singlePlayerAnimation.value)

        multiplayerAnimation.setAnimation(if (MouseUtils.isInside(mouseX, mouseY, centerX - (width / 2f), yPos + spacing, width, height)) 1.0f else 0.0f, 16.0)
        drawMenuButton(nvg, centerX, yPos + spacing, width, height, TranslateText.MULTIPLAYER.text, multiplayerAnimation.value)

        optionsAnimation.setAnimation(if (MouseUtils.isInside(mouseX, mouseY, centerX - (width / 2f), yPos + (spacing * 2), width, height)) 1.0f else 0.0f, 16.0)
        drawMenuButton(nvg, centerX, yPos + (spacing * 2), width, height, TranslateText.SETTINGS.text, optionsAnimation.value)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val sr = ScaledResolution(mc)

        val yPos = sr.scaledHeight / 2f - 22

        if (mouseButton == 0) {
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth / 2f - (160 / 2f), yPos, 160f, 20f)) {
                mc.displayGuiScreen(GuiSelectWorld(getParent()))
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth / 2f - (180 / 2f), yPos + 26, 180f, 20f)) {
                mc.displayGuiScreen(GuiMultiplayer(getParent()))
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth / 2f - (180 / 2f), yPos + (26 * 2), 180f, 20f)) {
                mc.displayGuiScreen(GuiOptions(getParent(), mc.gameSettings))
            }
        }
    }

    private fun drawMenuButton(nvg: NanoVGManager, centerX: Float, y: Float, width: Float, height: Float, label: String, hoverProgress: Float) {
        val radius = 6f
        val buttonX = centerX - (width / 2f)
        val baseColor = getControlColor()
        val accent: AccentColor = getMenuAccent()

        if (hoverProgress > 0.01f) {
            val glowStart = ColorUtils.applyAlpha(accent.color1, (80 + 140 * hoverProgress).toInt())
            val glowEnd = ColorUtils.applyAlpha(accent.color2, (80 + 140 * hoverProgress).toInt())
            nvg.drawGradientShadow(buttonX, y, width, height, radius, glowStart, glowEnd)
        }

        val fillColor = ColorUtils.applyAlpha(baseColor, (200 + 40 * hoverProgress).toInt())
        nvg.drawRoundedRect(buttonX, y, width, height, radius, fillColor)

        if (hoverProgress > 0.01f) {
            val outline = ColorUtils.applyAlpha(accent.color2, (80 + 90 * hoverProgress).toInt())
            nvg.drawOutlineRoundedRect(buttonX, y, width, height, radius, 1.0f, outline)
        }

        nvg.drawCenteredText(label, centerX, y + 6.5f, Color.WHITE, 9.5f, Fonts.REGULAR)
    }
}
