package com.shindoclient.shindo.gui

import eu.shoroa.contrib.render.Blur
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.ui.animation.v2.Animation
import com.shindoclient.shindo.ui.animation.v2.Direction
import com.shindoclient.shindo.ui.animation.v2.easing.EaseLiner
import com.shindoclient.shindo.ui.animation.v2.screen.ScreenAnimation
import com.shindoclient.shindo.utils.mouse.MouseUtils
import com.shindoclient.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiShareToLan
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.achievement.GuiAchievements
import net.minecraft.client.gui.achievement.GuiStats
import net.minecraft.client.resources.I18n
import org.lwjgl.input.Keyboard
import java.awt.Color

class GuiGameMenu : GuiScreen() {
    private val screenAnimation = ScreenAnimation()
    private lateinit var introAnimation: Animation
    private var x = 0
    private var y = 0
    private var menuWidth = 0
    private var menuHeight = 0
    private var centre = 0
    private var scaledWidth = 0
    private var scaledHeight = 0

    override fun initGui() {
        val sr = ScaledResolution(mc)
        scaledWidth = sr.scaledWidth
        scaledHeight = sr.scaledHeight
        centre = scaledWidth / 2
        x = centre - 90
        y = (scaledHeight / 2) - 110
        menuWidth = 180
        menuHeight = 220

        introAnimation = EaseLiner(80, 1.0)
        introAnimation.setDirection(Direction.FORWARDS)
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        BlurUtils.drawBlurScreen(20F)
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager!!
        drawRect(0, 0, mc.displayWidth, mc.displayHeight, 0x8C000000.toInt())
        Blur.render(10f)
        screenAnimation.wrap(
            Runnable { drawNanoVG(nvg) },
            x.toFloat(),
            y.toFloat(),
            menuWidth.toFloat(),
            menuHeight.toFloat(),
            2 - introAnimation.getValueFloat(),
            introAnimation.getValueFloat().coerceAtMost(1f),
            false,
        )
        if (introAnimation.isDone(Direction.BACKWARDS)) {
            mc.displayGuiScreen(null)
            mc.setIngameFocus()
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(nvg: NanoVGManager) {
        nvg.drawText(Lucide.ARROW_LEFT, x.toFloat(), y + 5f, Color(255, 255, 255, 140), 11f, Fonts.LUCIDE)
        nvg.drawCenteredText(
            I18n.format("menu.game"),
            centre.toFloat(),
            y + 5f,
            Color(255, 255, 255, 200),
            13f,
            Fonts.SEMIBOLD,
        )

        val standardPadding = 29.5f
        var offset = 29.5f
        drawButton(nvg, TranslateText.MENU_GAME_OPTIONS.getText(), Lucide.SLIDERS, offset)
        offset += standardPadding
        if (mc.isSingleplayer && !mc.integratedServer.`public`) {
            drawButton(nvg, I18n.format("menu.shareToLan"), Lucide.USERS, offset)
        } else {
            drawButton(nvg, TranslateText.EDIT_HUD.getText(), Lucide.LAYOUT, offset)
        }
        offset += standardPadding
        drawButton(nvg, I18n.format("gui.stats"), Lucide.ARCHIVE, offset)
        offset += standardPadding
        drawButton(nvg, I18n.format("gui.achievements"), Lucide.MAP, offset)
        offset += standardPadding
        drawButton2(nvg, TranslateText.OPEN_MOD_MENU.getText(), Shinconic.SHINDO, offset)
        offset += standardPadding
        drawButton(
            nvg,
            if (!mc.isIntegratedServerRunning) {
                I18n.format(
                    "menu.disconnect",
                )
            } else {
                TranslateText.EXIT_WORLD_SINGLEPLAYER.getText()
            },
            Lucide.LOG_OUT,
            offset,
        )
    }

    private fun drawButton(
        nvg: NanoVGManager,
        text: String,
        icon: String,
        offset: Float,
    ) {
        Blur.drawBlur(x.toFloat(), y + offset, menuWidth.toFloat(), 22f, 6f)
        nvg.drawRoundedRect(x.toFloat(), y + offset, menuWidth.toFloat(), 22f, 6f, Color(230, 230, 230, 80))
        val startX = (nvg.getTextWidth(text, 9.5f, Fonts.MEDIUM) + 14) / 2
        nvg.drawText(icon, centre - startX, y + offset + 6.5f, Color.WHITE, 9.5f, Fonts.LUCIDE)
        nvg.drawText(text, centre - startX + 14, y + offset + 7f, Color.WHITE, 9.5f, Fonts.MEDIUM)
    }

    private fun drawButton2(
        nvg: NanoVGManager,
        text: String,
        icon: String,
        offset: Float,
    ) {
        Blur.drawBlur(x.toFloat(), y + offset, menuWidth.toFloat(), 22f, 6f)
        nvg.drawRoundedRect(x.toFloat(), y + offset, menuWidth.toFloat(), 22f, 6f, Color(230, 230, 230, 80))
        val startX = (nvg.getTextWidth(text, 9.5f, Fonts.MEDIUM) + 14) / 2
        nvg.drawText(icon, centre - startX, y + offset + 6.5f, Color.WHITE, 9.5f, Fonts.SHINCONIC)
        nvg.drawText(text, centre - startX + 14, y + offset + 7f, Color.WHITE, 9.5f, Fonts.MEDIUM)
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) {
            return
        }
        val standardPadding = 29.5f
        var offset = standardPadding

        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, menuWidth.toFloat(), 22f)) {
            introAnimation.setDirection(Direction.BACKWARDS)
            mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, menuWidth.toFloat(), 22f)) {
            if (mc.isSingleplayer && !mc.integratedServer.`public`) {
                mc.displayGuiScreen(GuiShareToLan(this))
            } else {
                mc.displayGuiScreen(GuiEditHUD(false))
            }
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, menuWidth.toFloat(), 22f)) {
            mc.displayGuiScreen(GuiStats(this, mc.thePlayer.statFileWriter))
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, menuWidth.toFloat(), 22f)) {
            mc.displayGuiScreen(GuiAchievements(this, mc.thePlayer.statFileWriter))
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, menuWidth.toFloat(), 22f)) {
            mc.displayGuiScreen(Shindo.getInstance().getShindoAPI().modMenu)
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, menuWidth.toFloat(), 22f)) {
            val flag = mc.isIntegratedServerRunning
            mc.theWorld.sendQuittingDisconnectingPacket()
            mc.loadWorld(null)

            if (flag) {
                mc.displayGuiScreen(GuiMainMenu())
            } else {
                mc.displayGuiScreen(GuiMultiplayer(GuiMainMenu()))
            }
        }
        if (!MouseUtils.isInside(
                mouseX,
                mouseY,
                x.toFloat(),
                y + standardPadding,
                menuWidth.toFloat(),
                offset - standardPadding + 22,
            )
        ) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }
}
