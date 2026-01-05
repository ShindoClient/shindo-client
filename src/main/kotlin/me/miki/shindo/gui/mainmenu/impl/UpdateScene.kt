package me.miki.shindo.gui.mainmenu.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Desktop
import java.net.URI

class UpdateScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)

        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager

        nvg.setupAndDraw { drawNanoVG(mouseX, mouseY, sr, instance, nvg) }
    }

    private fun drawNanoVG(mouseX: Int, mouseY: Int, sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager) {
        nvg.drawRect(0f, 0f, sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, 100))
        val acWidth = 220
        val acHeight = 190
        val acX = sr.scaledWidth / 2 - (acWidth / 2)
        val acY = sr.scaledHeight / 2 - (acHeight / 2)
        val update = instance.update
        val panelColor = getPanelColor()
        val controlColor = getControlColor()
        nvg.drawRoundedRect(acX.toFloat(), acY.toFloat(), acWidth.toFloat(), acHeight.toFloat(), 8f, panelColor)
        nvg.drawCenteredText("Update Available", acX + (acWidth / 2f), acY + 12f, Color.WHITE, 14f, Fonts.MEDIUM)
        nvg.drawCenteredText("Would you like to update?", acX + (acWidth / 2f), acY + 30f, Color.WHITE, 9f, Fonts.REGULAR)
        nvg.drawCenteredText(instance.version + " -> " + update.versionString, acX + (acWidth / 2f), acY + 48f, Color.WHITE, 9f, Fonts.REGULAR)
        nvg.drawCenteredText(instance.verIdentifier + " -> " + update.buildID, acX + (acWidth / 2f), acY + 60f, Color.WHITE, 5f, Fonts.REGULAR)
        nvg.drawRoundedRect(acX + acWidth / 2f - 90f, acY + acHeight - 64f, 180f, 20f, 4.5f, controlColor)
        nvg.drawCenteredText("Go to update", acX + acWidth / 2f, acY + acHeight - 54f - (nvg.getTextHeight("Go to update", 9.5f, Fonts.REGULAR) / 2), Color.WHITE, 9.5f, Fonts.REGULAR)
        nvg.drawRoundedRect(acX + acWidth / 2f - 90f, acY + acHeight - 32f, 180f, 20f, 4.5f, controlColor)
        nvg.drawCenteredText("Maybe Later", acX + acWidth / 2f, acY + acHeight - 22f - (nvg.getTextHeight("Maybe Later", 9.5f, Fonts.REGULAR) / 2), Color.WHITE, 9.5f, Fonts.REGULAR)
    }

    fun exitGui() {
        val instance = Shindo.getInstance()
        instance.updateNeeded = false
        setCurrentScene(getSceneByClass(MainScene::class.java))
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            val sr = ScaledResolution(mc)
            val acWidth = 220
            val acHeight = 190
            val acX = sr.scaledWidth / 2 - (acWidth / 2)
            val acY = sr.scaledHeight / 2 - (acHeight / 2)
            val instance = Shindo.getInstance()
            if (MouseUtils.isInside(mouseX, mouseY, acX + acWidth / 2f - 90f, acY + acHeight - 64f, 180f, 20f)) {
                try {
                    Desktop.getDesktop().browse(URI(instance.update.updateLink))
                } catch (_: Exception) {
                }
            }
            if (MouseUtils.isInside(mouseX, mouseY, acX + acWidth / 2f - 90f, acY + acHeight - 32f, 180f, 20f)) {
                exitGui()
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            exitGui()
        }
    }
}
