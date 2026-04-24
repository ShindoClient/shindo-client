package me.miki.shindo.gui

import me.miki.shindo.Shindo
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.easing.EaseLiner
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color

class GuiNavigationHub : GuiScreen(), IShindoScreen {

    private data class IconButton(val icon: String, val action: () -> Unit)

    private lateinit var introAnimation: Animation
    private var centerX = 0f
    private var centerY = 0f
    private val iconButtons = ArrayList<IconButton>()

    override fun initGui() {
        val sr = ScaledResolution(mc)
        centerX = sr.scaledWidth / 2f
        centerY = sr.scaledHeight / 2f

        iconButtons.clear()
        iconButtons.add(IconButton(LegacyIcon.MAP_PIN) { openNow(GuiWaypoint()) })
        iconButtons.add(IconButton(LegacyIcon.MOD_AUTO_TEXT) { openNow(GuiAutoTextManager(this)) })
        //iconButtons.add(IconButton(LegacyIcon.USERS) { openNow(GuiFriendsChat(this)) })
        iconButtons.add(IconButton(LegacyIcon.LAYOUT) { openNow(GuiEditHUD(false)) })

        introAnimation = EaseLiner(220, 1.0)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvg = Shindo.getInstance().nanoVGManager ?: return
        val progress = introAnimation.getValueFloat().coerceIn(0f, 1f)


        nvg.setupAndDraw(Runnable {
            nvg.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Color(0, 0, 0, 120))
            drawNanoVG(nvg, mouseX, mouseY, progress)
        })
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(nvg: NanoVGManager, mouseX: Int, mouseY: Int, progress: Float) {
        val topOffset = (1f - progress) * -56f
        val bottomOffset = (1f - progress) * 52f

        val logoY = centerY - 78f + topOffset
        val nameY = centerY - 45f + topOffset
        val mainButtonW = 120f
        val mainButtonH = 22f
        val mainButtonX = centerX - (mainButtonW / 2f)
        val mainButtonY = centerY - 12f + bottomOffset

        nvg.drawCenteredText(LegacyIcon.SHINDO, centerX, logoY, Color.WHITE, 34f, Fonts.LEGACYICON)
        nvg.drawCenteredText("Shindo", centerX, nameY, Color.WHITE, 16f, Fonts.SEMIBOLD)

        val mainHovered = MouseUtils.isInside(mouseX, mouseY, mainButtonX, mainButtonY, mainButtonW, mainButtonH)
        nvg.drawRoundedRect(
            mainButtonX,
            mainButtonY,
            mainButtonW,
            mainButtonH,
            5f,
            if (mainHovered) Color(255, 255, 255, 70) else Color(255, 255, 255, 45)
        )
        nvg.drawCenteredText(
            TranslateText.OPEN_MOD_MENU.getText(),
            centerX,
            mainButtonY + 7f,
            Color.WHITE,
            9.5f,
            Fonts.MEDIUM
        )

        val iconSize = 20f
        val iconGap = 7f
        val totalWidth = iconButtons.size * iconSize + (iconButtons.size - 1) * iconGap
        val rowX = centerX - (totalWidth / 2f)
        val rowY = mainButtonY + 30f

        for ((index, button) in iconButtons.withIndex()) {
            val buttonX = rowX + index * (iconSize + iconGap)
            val hovered = MouseUtils.isInside(mouseX, mouseY, buttonX, rowY, iconSize, iconSize)
            nvg.drawRoundedRect(
                buttonX,
                rowY,
                iconSize,
                iconSize,
                4f,
                if (hovered) Color(255, 255, 255, 70) else Color(255, 255, 255, 45)
            )
            nvg.drawCenteredText(
                button.icon,
                buttonX + (iconSize / 2f),
                rowY + 6.5f,
                Color.WHITE,
                9.5f,
                Fonts.LEGACYICON
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        val progress = introAnimation.getValueFloat().coerceIn(0f, 1f)
        val bottomOffset = (1f - progress) * 52f

        val mainButtonW = 120f
        val mainButtonH = 22f
        val mainButtonX = centerX - (mainButtonW / 2f)
        val mainButtonY = centerY - 12f + bottomOffset
        if (MouseUtils.isInside(mouseX, mouseY, mainButtonX, mainButtonY, mainButtonW, mainButtonH)) {
            openNow(Shindo.getInstance().shindoAPI.modMenu)
            return
        }

        val iconSize = 20f
        val iconGap = 7f
        val totalWidth = iconButtons.size * iconSize + (iconButtons.size - 1) * iconGap
        val rowX = centerX - (totalWidth / 2f)
        val rowY = mainButtonY + 30f

        for ((index, button) in iconButtons.withIndex()) {
            val buttonX = rowX + index * (iconSize + iconGap)
            if (MouseUtils.isInside(mouseX, mouseY, buttonX, rowY, iconSize, iconSize)) {
                button.action.invoke()
                return
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null)
            mc.setIngameFocus()
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    private fun openNow(screen: GuiScreen) {
        mc.displayGuiScreen(screen)
    }
}
