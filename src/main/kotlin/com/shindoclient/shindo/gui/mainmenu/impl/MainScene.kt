package com.shindoclient.shindo.gui.mainmenu.impl

import com.shindoclient.extensions.ui.animation.setAnimation
import com.shindoclient.extensions.ui.nanovg.drawOutlineRoundedRect
import com.shindoclient.extensions.ui.nanovg.drawRoundedRect
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.gui.mainmenu.GuiShindoMainMenu
import com.shindoclient.shindo.gui.mainmenu.MainMenuScene
import com.shindoclient.shindo.gui.mainmenu.widget.MainMenuWidgetHost
import com.shindoclient.shindo.gui.mainmenu.widget.impl.AccountDropdownWidget
import com.shindoclient.shindo.gui.modmenu.v2.GuiModMenu
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.ui.animation.v2.Direction
import com.shindoclient.shindo.ui.animation.v2.curve.DecelerateAnimation
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import com.shindoclient.shindo.utils.MathUtils.lerp
import com.shindoclient.shindo.utils.mouse.MouseUtils.isInside
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class MainScene(
    parent: GuiShindoMainMenu,
) : MainMenuScene(parent) {
    private val introAnimation = DecelerateAnimation(800, 1.0)
    private val confirmAnim = SimpleAnimation(0f)
    private var confirmingExit = false

    private var singleHover = 0f
    private var multiHover = 0f
    private var exitHover = 0f
    private var bgHover = 0f
    private var settingsHover = 0f
    private var featherHover = 0f

    private val widgetHost = MainMenuWidgetHost()

    init {
        widgetHost.register(
            AccountDropdownWidget(parent).also { w ->
                w.onAddAccountClicked = {
                    setCurrentScene(getSceneByClass(AccountScene::class.java))
                }
            },
        )
    }

    override fun initScene() {
        introAnimation.reset()
        introAnimation.setDirection(Direction.FORWARDS)
        confirmingExit = false
        widgetHost.onSceneInit()
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()

        if (instance.isUpdateNeeded()) {
            instance.setUpdateNeeded(false)
            setCurrentScene(getSceneByClass(UpdateScene::class.java))
            return
        }

        val nvg = instance.nanoVGManager ?: return
        nvg.setupAndDraw(Runnable { drawNanoVG(instance, nvg, mouseX, mouseY, partialTicks) })
    }

    private fun drawNanoVG(
        instance: Shindo,
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val sr = ScaledResolution(mc)
        val sw = sr.scaledWidth.toFloat()
        val sh = sr.scaledHeight.toFloat()
        val anim = introAnimation.getValueFloat()

        val centerX = sw / 2f
        val logoSize = 54f
        val logoY = sh / 2f - (nvg.getTextHeight(Shinconic.SHINDO, logoSize, Fonts.SHINCONIC) / 2f) - 55
        nvg.drawCenteredText(
            Shinconic.SHINDO,
            centerX,
            logoY,
            Color(255, 255, 255, (anim * 255).toInt()),
            logoSize,
            Fonts.SHINCONIC,
        )

        val btnW = 180f
        val btnH = 20f
        val sp = 26f
        val startY = sh / 2f - 10f
        val btnX = centerX - btnW / 2f

        singleHover = lerp(singleHover, if (isInside(mouseX, mouseY, btnX, startY, btnW, btnH)) 1f else 0f, 0.2f)
        multiHover = lerp(multiHover, if (isInside(mouseX, mouseY, btnX, startY + sp, btnW, btnH)) 1f else 0f, 0.2f)
        exitHover = lerp(exitHover, if (isInside(mouseX, mouseY, btnX, startY + sp * 2, btnW, btnH)) 1f else 0f, 0.2f)

        nvg.drawGlassButton(TranslateText.SINGLEPLAYER.getText(), centerX, startY, btnW, btnH, singleHover, anim, false)
        nvg.drawGlassButton(TranslateText.MULTIPLAYER.getText(), centerX, startY + sp, btnW, btnH, multiHover, anim, false)
        nvg.drawGlassButton("QUIT GAME", centerX, startY + sp * 2, btnW, btnH, exitHover, anim, true)

        val bgSz = 36f
        val bgX = 10f
        val bgY = sh - bgSz - 16f
        bgHover = lerp(bgHover, if (isInside(mouseX, mouseY, bgX, bgY, bgSz, bgSz)) 1f else 0f, 0.2f)

        nvg.drawRoundedRect(bgX, bgY, bgSz, bgSz, 6, Color(15, 15, 20, (anim * (160 + bgHover * 60)).toInt()))
        nvg.drawOutlineRoundedRect(bgX, bgY, bgSz, bgSz, 6, 1.2f, Color(255, 255, 255, (anim * (40 + bgHover * 80)).toInt()))
        nvg.drawCenteredText(
            Lucide.IMAGE,
            bgX + bgSz / 2f,
            bgY + bgSz / 2f - 10f,
            Color(255, 255, 255, (anim * (180 + bgHover * 75)).toInt()),
            22f,
            Fonts.LUCIDE,
        )

        confirmAnim.setAnimation(if (confirmingExit) 1f else 0f, 14)
        if (confirmAnim.getValue() > 0.01f) {
            drawConfirmExit(nvg, sw, sh, mouseX, mouseY, anim * confirmAnim.getValue())
        }

        drawTopRightIcons(nvg, mouseX, mouseY, sw, anim)

        widgetHost.draw(nvg, mouseX, mouseY, sw, sh, anim, partialTicks)
    }

    private fun drawTopRightIcons(
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
        sw: Float,
        anim: Float,
    ) {
        val topY = 10f
        val btnS = 24f
        val btnSp = 6f
        var rightX = sw - 10f

        // Settings
        settingsHover = lerp(settingsHover, if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) 1f else 0f, 0.2f)
        nvg.drawGlassButtonWithIcon(Lucide.SETTINGS, rightX - btnS, topY, btnS, settingsHover, true, anim)

        // Logo / mod menu
        rightX -= (btnS + btnSp)
        featherHover = lerp(featherHover, if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) 1f else 0f, 0.2f)
        nvg.drawGlassButtonWithIcon(Shinconic.SHINDO, rightX - btnS, topY, btnS, featherHover, false, anim)
    }

    private fun drawConfirmExit(
        nvg: NanoVGManager,
        sw: Float,
        sh: Float,
        mouseX: Int,
        mouseY: Int,
        alpha: Float,
    ) {
        val pw = 200f
        val ph = 80f
        val px = sw / 2f - pw / 2f
        val py = sh / 2f - ph / 2f

        nvg.drawRoundedRect(px, py, pw, ph, 8, Color(15, 10, 10, (alpha * 230).toInt()))
        nvg.drawOutlineRoundedRect(px, py, pw, ph, 8, 1.2f, Color(200, 50, 50, (alpha * 180).toInt()))
        nvg.drawCenteredText("Are you sure?", sw / 2f, py + 14f, Color(255, 255, 255, (alpha * 230).toInt()), 11f, Fonts.SEMIBOLD)

        val btnW = 70f
        val btnH = 22f
        val btnY = py + ph - 32f
        val yesX = sw / 2f - btnW - 6f
        val noX = sw / 2f + 6f

        val yesHov = isInside(mouseX, mouseY, yesX, btnY, btnW, btnH)
        nvg.drawRoundedRect(yesX, btnY, btnW, btnH, 5, Color(180, 30, 30, (alpha * (if (yesHov) 220 else 160)).toInt()))
        nvg.drawOutlineRoundedRect(yesX, btnY, btnW, btnH, 5, 1f, Color(255, 80, 80, (alpha * 120).toInt()))
        nvg.drawCenteredText("Quit", yesX + btnW / 2f, btnY + 6f, Color(255, 220, 220, (alpha * 230).toInt()), 9.5f, Fonts.SEMIBOLD)

        val noHov = isInside(mouseX, mouseY, noX, btnY, btnW, btnH)
        nvg.drawRoundedRect(noX, btnY, btnW, btnH, 5, Color(40, 40, 50, (alpha * (if (noHov) 220 else 160)).toInt()))
        nvg.drawOutlineRoundedRect(noX, btnY, btnW, btnH, 5, 1f, Color(255, 255, 255, (alpha * 50).toInt()))
        nvg.drawCenteredText("Cancel", noX + btnW / 2f, btnY + 6f, Color(200, 200, 200, (alpha * 230).toInt()), 9.5f, Fonts.SEMIBOLD)
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) return

        val sr = ScaledResolution(mc)
        val sw = sr.scaledWidth.toFloat()
        val sh = sr.scaledHeight.toFloat()
        val nvg = Shindo.getInstance().nanoVGManager ?: return
        val anim = introAnimation.getValueFloat()

        // Confirm-exit dialog intercepts all clicks while visible
        if (confirmingExit) {
            val pw = 200f
            val ph = 80f
            val px = sw / 2f - pw / 2f
            val py = sh / 2f - ph / 2f
            val btnW = 70f
            val btnH = 22f
            val btnY = py + ph - 32f
            val yesX = sw / 2f - btnW - 6f
            val noX = sw / 2f + 6f
            when {
                isInside(mouseX, mouseY, yesX, btnY, btnW, btnH) -> mc.shutdown()
                isInside(mouseX, mouseY, noX, btnY, btnW, btnH) -> confirmingExit = false
            }
            return
        }

        // Widgets (topmost first — host dispatches in reverse order)
        if (widgetHost.mouseClicked(nvg, mouseX, mouseY, sw, sh, anim, 0f, mouseButton)) return

        // Nav buttons
        val btnW = 180f
        val btnH = 20f
        val sp = 26f
        val startY = sh / 2f - 10f
        val btnX = sw / 2f - btnW / 2f

        when {
            isInside(mouseX, mouseY, btnX, startY, btnW, btnH) -> {
                mc.displayGuiScreen(GuiSelectWorld(getParent()))
            }

            isInside(mouseX, mouseY, btnX, startY + sp, btnW, btnH) -> {
                mc.displayGuiScreen(GuiMultiplayer(getParent()))
            }

            isInside(mouseX, mouseY, btnX, startY + sp * 2, btnW, btnH) -> {
                confirmingExit = true
                return
            }
        }

        // Bottom-left background button
        val bgSz = 36f
        val bgX = 10f
        val bgY = sh - bgSz - 10f
        if (isInside(mouseX, mouseY, bgX, bgY, bgSz, bgSz)) {
            setCurrentScene(getSceneByClass(BackgroundScene::class.java))
            return
        }

        val topY = 10f
        val btnS = 24f
        val btnSp = 6f
        var rightX = sw - 10f

        if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) {
            mc.displayGuiScreen(GuiOptions(getParent(), mc.gameSettings))
        }
        rightX -= (btnS + btnSp)
        if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) {
            mc.displayGuiScreen(GuiModMenu())
        }
    }
}
