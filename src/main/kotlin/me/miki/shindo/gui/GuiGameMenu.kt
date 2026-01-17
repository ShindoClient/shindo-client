package me.miki.shindo.gui

import me.miki.shindo.Shindo
import me.miki.shindo.management.addons.resourcify.core.ResourcifyAddon
import me.miki.shindo.management.addons.resourcify.model.ResourcifyResourceType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.easing.EaseLiner
import me.miki.shindo.utils.buffer.ScreenAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiShareToLan
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.achievement.GuiAchievements
import net.minecraft.client.gui.achievement.GuiStats
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.Color

class GuiGameMenu : GuiScreen(), IShindoScreen {

    private val screenAnimation = ScreenAnimation()
    private lateinit var introAnimation: Animation
    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0
    private var centre = 0
    private var scaledWidth = 0
    private var scaledHeight = 0
    private var resourcifyButtonX = 0f
    private var resourcifyButtonY = 0f
    private var resourcifyButtonSize = 0f

    override fun initGui() {
        val sr = ScaledResolution(mc)
        scaledWidth = sr.scaledWidth
        scaledHeight = sr.scaledHeight
        centre = scaledWidth / 2
        x = centre - 90
        y = (scaledHeight / 2) - 110
        width = 180
        height = 220

        introAnimation = EaseLiner(80, 1.0)
        introAnimation.setDirection(Direction.FORWARDS)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        BlurUtils.drawBlurScreen(20F)
        val nvg = Shindo.getInstance().nanoVGManager
        screenAnimation.wrap(Runnable { drawNanoVG(nvg) }, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 2 - introAnimation.getValueFloat(),
            introAnimation.getValueFloat().coerceAtMost(1f), false)
        if (introAnimation.isDone(Direction.BACKWARDS)) {
            mc.displayGuiScreen(null)
            mc.setIngameFocus()
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(nvg: NanoVGManager?) {
        nvg!!.drawRect(-5f, -5f, scaledWidth + 10f, scaledHeight + 10f, Color(0, 0, 0, 140))
        nvg.drawText(LegacyIcon.ARROW_LEFT, x.toFloat(), y + 5f, Color(255, 255, 255, 140), 11f, Fonts.LEGACYICON)
        nvg.drawCenteredText(I18n.format("menu.game"), centre.toFloat(), y + 5f, Color(255, 255, 255, 200), 13f, Fonts.SEMIBOLD)

        resourcifyButtonSize = 18f
        resourcifyButtonX = x + width - resourcifyButtonSize - 6f
        resourcifyButtonY = y + 4f
        nvg.drawRoundedRect(resourcifyButtonX, resourcifyButtonY, resourcifyButtonSize, resourcifyButtonSize, 6f, Color(255, 255, 255, 80))
        nvg.drawCenteredText(LegacyIcon.DOWNLOAD, resourcifyButtonX + resourcifyButtonSize / 2f, resourcifyButtonY + resourcifyButtonSize / 2f - 6f, Color.WHITE, 11f, Fonts.LEGACYICON)

        val standardPadding = 29.5f
        var offset = 29.5f
        drawButton(nvg, TranslateText.MENU_GAME_OPTIONS.text, LegacyIcon.SLIDERS, offset)
        offset += standardPadding
        if (mc.isSingleplayer && !mc.integratedServer.`public`) {
            drawButton(nvg, I18n.format("menu.shareToLan"), LegacyIcon.USERS, offset)
        } else {
            drawButton(nvg, TranslateText.EDIT_HUD.text, LegacyIcon.LAYOUT, offset)
        }
        offset += standardPadding
        drawButton(nvg, I18n.format("gui.stats"), LegacyIcon.ARCHIVE, offset)
        offset += standardPadding
        drawButton(nvg, I18n.format("gui.achievements"), LegacyIcon.MAP, offset)
        offset += standardPadding
        drawButton(nvg, TranslateText.OPEN_MOD_MENU.text, LegacyIcon.SHINDO, offset)
        offset += standardPadding
        drawButton(nvg, if (!mc.isIntegratedServerRunning) I18n.format("menu.disconnect") else TranslateText.EXIT_WORLD_SINGLEPLAYER.text, LegacyIcon.LOGOUT, offset)
    }

    private fun drawButton(nvg: NanoVGManager, text: String, icon: String, offset: Float) {
        nvg.drawRoundedRect(x.toFloat(), y + offset, width.toFloat(), 22f, 6f, Color(230, 230, 230, 80))
        val startX = (nvg.getTextWidth(text, 9.5f, Fonts.MEDIUM) + 14) / 2
        nvg.drawText(icon, centre - startX, y + offset + 6.5f, Color.WHITE, 9.5f, Fonts.LEGACYICON)
        nvg.drawText(text, centre - startX + 14, y + offset + 7f, Color.WHITE, 9.5f, Fonts.MEDIUM)
    }

    private fun drawButton(nvg: NanoVGManager, text: String, icon: ResourceLocation, offset: Float) {
        nvg.drawRoundedRect(x.toFloat(), y + offset, width.toFloat(), 22f, 6f, Color(230, 230, 230, 80))
        val startX = (nvg.getTextWidth(text, 9.5f, Fonts.MEDIUM) + 14) / 2
        nvg.drawImage(icon, centre - startX, y + offset + 6.5f, 9.5f, 9.5f)
        nvg.drawText(text, centre - startX + 14, y + offset + 7f, Color.WHITE, 9.5f, Fonts.MEDIUM)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            return
        }
        if (MouseUtils.isInside(mouseX, mouseY, resourcifyButtonX, resourcifyButtonY, resourcifyButtonSize, resourcifyButtonSize)) {
            val addon = ResourcifyAddon.getInstance()
            if (addon != null && addon.isToggled()) {
                mc.displayGuiScreen(GuiResourcify(this, ResourcifyResourceType.RESOURCE_PACK))
            } else {
                Shindo.getInstance().notificationManager.post(
                    "Resourcify",
                    "Enable the Resourcify addon first.",
                    NotificationType.WARNING
                )
            }
            return
        }
        val standardPadding = 29.5f
        var offset = standardPadding

        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, width.toFloat(), 22f)) {
            introAnimation.setDirection(Direction.BACKWARDS)
            mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, width.toFloat(), 22f)) {
            if (mc.isSingleplayer && !mc.integratedServer.`public`) {
                mc.displayGuiScreen(GuiShareToLan(this))
            } else {
                mc.displayGuiScreen(GuiEditHUD(false))
            }
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, width.toFloat(), 22f)) {
            mc.displayGuiScreen(GuiStats(this, mc.thePlayer.statFileWriter))
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, width.toFloat(), 22f)) {
            mc.displayGuiScreen(GuiAchievements(this, mc.thePlayer.statFileWriter))
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, width.toFloat(), 22f)) {
            mc.displayGuiScreen(Shindo.getInstance().shindoAPI.modMenu)
        }
        offset += standardPadding
        if (MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + offset, width.toFloat(), 22f)) {
            val flag = mc.isIntegratedServerRunning
            mc.theWorld.sendQuittingDisconnectingPacket()
            mc.loadWorld(null)

            if (flag) {
                mc.displayGuiScreen(GuiMainMenu())
            } else {
                mc.displayGuiScreen(GuiMultiplayer(GuiMainMenu()))
            }
        }
        if (!MouseUtils.isInside(mouseX, mouseY, x.toFloat(), y + standardPadding, width.toFloat(), offset - standardPadding + 22)) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }
}
