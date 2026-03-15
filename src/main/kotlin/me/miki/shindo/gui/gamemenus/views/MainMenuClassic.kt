package me.miki.shindo.gui.gamemenus.views

import me.miki.shindo.Shindo
import me.miki.shindo.gui.gamemenus.MenuManager
import me.miki.shindo.gui.gamemenus.ShindoScreen
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class MainMenuClassic(manager: MenuManager) : ShindoScreen(manager, "Main Menu") {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance: Shindo = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager!!
        nvg.setupAndDraw(Runnable { drawNanoVG(nvg, instance) })
    }

    private fun drawNanoVG(nvg: NanoVGManager, shindoInstance: Shindo) {
        val sr = ScaledResolution(mc)
        val yPos: Float = (sr.scaledHeight / 2 - 22).toFloat()
        nvg.drawCenteredText(
            LegacyIcon.SHINDO,
            sr.scaledWidth / 2f,
            sr.scaledHeight / 2f - nvg.getTextHeight(LegacyIcon.SHINDO, 54f, Fonts.LEGACYICON) / 2f - 60f,
            Color.WHITE,
            54f,
            Fonts.LEGACYICON
        )
        nvg.drawRoundedRect(sr.scaledWidth / 2f - 180f / 2f, yPos, 180f, 20f, 4.5f, this.getBackgroundColor())
        nvg.drawCenteredText(
            TranslateText.SINGLEPLAYER.getText(),
            sr.scaledWidth / 2f,
            yPos + 11f,
            Color.WHITE,
            9.5f,
            Fonts.REGULAR
        )
        nvg.drawRoundedRect(sr.scaledWidth / 2f - 180f / 2f, yPos + 26f, 180f, 20f, 4.5f, this.getBackgroundColor())
        nvg.drawCenteredText(
            TranslateText.MULTIPLAYER.getText(),
            sr.scaledWidth / 2f,
            yPos + 11f + 26f,
            Color.WHITE,
            9.5f,
            Fonts.REGULAR
        )
        nvg.drawRoundedRect(
            sr.scaledWidth / 2f - 180f / 2f,
            yPos + 26f * 2f,
            180f,
            20f,
            4.5f,
            this.getBackgroundColor()
        )
        nvg.drawCenteredText(
            TranslateText.SETTINGS.getText(),
            sr.scaledWidth / 2f,
            yPos + 11f + 26f * 2f,
            Color.WHITE,
            9.5f,
            Fonts.REGULAR
        )
        val copyright = "Copyright Mojang AB. Do not distribute!"
        nvg.drawText(
            copyright,
            sr.scaledWidth - nvg.getTextWidth(copyright, 9f, Fonts.REGULAR) - 4f,
            sr.scaledHeight - 12f,
            Color(255, 255, 255),
            9f,
            Fonts.REGULAR
        )
        nvg.drawText(
            "Shindo Client v" + shindoInstance.version,
            4f,
            sr.scaledHeight - 12f,
            Color(255, 255, 255),
            9f,
            Fonts.REGULAR
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val sr = ScaledResolution(mc)
        val yPos: Float = (sr.scaledHeight / 2 - 22).toFloat()
        if (mouseButton == 0) {
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth / 2 - 160 / 2, yPos.toInt(), 160, 20)) {
                mc.displayGuiScreen(GuiSelectWorld(this.getMenuManager()))
            }
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth / 2 - 180 / 2, yPos.toInt() + 26, 180, 20)) {
                mc.displayGuiScreen(GuiMultiplayer(this.getMenuManager()))
            }
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth / 2 - 180 / 2, yPos.toInt() + 26 * 2, 180, 20)) {
                mc.displayGuiScreen(GuiOptions(this.getMenuManager(), mc.gameSettings))
            }
        }
    }
}