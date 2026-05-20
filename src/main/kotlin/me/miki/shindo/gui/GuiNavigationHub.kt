package me.miki.shindo.gui

import me.miki.shindo.Shindo
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.DecelerateAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color

class GuiNavigationHub : GuiScreen() {
    private val introAnimation: DecelerateAnimation = DecelerateAnimation(800, 1.0)
    private var centerX = 0f
    private var centerY = 0f

    private var mainHovered = 0f
    private var icon1Hovered = 0f
    private var icon2Hovered = 0f
    private var icon3Hovered = 0f

    override fun initGui() {
        val sr = ScaledResolution(mc)
        centerX = sr.scaledWidth / 2f
        centerY = sr.scaledHeight / 2f

        introAnimation.reset()
        introAnimation.setDirection(Direction.FORWARDS)
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager ?: return

        nvg.setupAndDraw(
            Runnable {
                nvg.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Color(0, 0, 0, 120))
                drawNanoVG(nvg, mouseX, mouseY)
            },
        )
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
    ) {
        val logoY = centerY - 78f
        val nameY = centerY - 42f
        val mainButtonW = 120f
        val mainButtonH = 22f
        val mainButtonX = centerX
        val mainButtonY = centerY - 12f

        val anim = introAnimation.getValueFloat()

        nvg.drawCenteredText(Shinconic.SHINDO, centerX, logoY, Color.WHITE, 34f, Fonts.SHINCONIC)
        nvg.drawCenteredText("Shindo", centerX, nameY, Color.WHITE, 16f, Fonts.SEMIBOLD)

        mainHovered =
            lerp(
                mainHovered,
                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        mainButtonX - (mainButtonW / 2f),
                        mainButtonY,
                        mainButtonW,
                        mainButtonH,
                    )
                ) {
                    1f
                } else {
                    0f
                },
                0.2f,
            )
        nvg.drawGlassButton(
            TranslateText.OPEN_MOD_MENU.getText(),
            mainButtonX,
            mainButtonY,
            mainButtonW,
            mainButtonH,
            mainHovered,
            anim,
            false,
        )

        val iconSize = 20f
        val button1X = centerX - 40
        val button2X = button1X + 30
        val button3X = button2X + 30
        val buttonY = mainButtonY + 30f

        icon1Hovered =
            lerp(
                icon1Hovered,
                if (MouseUtils.isInside(mouseX, mouseY, button1X, buttonY, iconSize, iconSize)) 1f else 0f,
                0.2f,
            )
        nvg.drawGlassButtonWithIcon(Lucide.MAP_PIN, button1X, buttonY, iconSize, icon1Hovered, true, anim)

        icon2Hovered =
            lerp(
                icon2Hovered,
                if (MouseUtils.isInside(mouseX, mouseY, button2X, buttonY, iconSize, iconSize)) 1f else 0f,
                0.2f,
            )
        nvg.drawGlassButtonWithIcon(Lucide.CODE, button2X, buttonY, iconSize, icon2Hovered, true, anim)

        icon3Hovered =
            lerp(
                icon3Hovered,
                if (MouseUtils.isInside(mouseX, mouseY, button3X, buttonY, iconSize, iconSize)) 1f else 0f,
                0.2f,
            )
        nvg.drawGlassButtonWithIcon(Lucide.LAYOUT, button3X, buttonY, iconSize, icon3Hovered, true, anim)
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) return

        val mainButtonW = 120f
        val mainButtonH = 22f
        val mainButtonX = centerX - (mainButtonW / 2f)
        val mainButtonY = centerY - 12f

        if (MouseUtils.isInside(mouseX, mouseY, mainButtonX, mainButtonY, mainButtonW, mainButtonH)) {
            openNow(Shindo.getInstance().getShindoAPI().modMenu)
            return
        }

        val iconSize = 20f
        val button1X = centerX - 40
        val button2X = button1X + 30
        val button3X = button2X + 30
        val buttonY = mainButtonY + 30f

        if (MouseUtils.isInside(mouseX, mouseY, button1X, buttonY, iconSize, iconSize)) {
            openNow(GuiWaypoint())
            return
        }

        if (MouseUtils.isInside(mouseX, mouseY, button2X, buttonY, iconSize, iconSize)) {
            openNow(GuiAutoTextManager(this))
            return
        }

        if (MouseUtils.isInside(mouseX, mouseY, button3X, buttonY, iconSize, iconSize)) {
            openNow(GuiEditHUD(false))
            return
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null)
            mc.setIngameFocus()
        }
    }

    override fun doesGuiPauseGame(): Boolean = false

    private fun openNow(screen: GuiScreen) {
        mc.displayGuiScreen(screen)
    }

    private fun lerp(
        current: Float,
        target: Float,
        factor: Float,
    ): Float = current + (target - current) * factor
}
