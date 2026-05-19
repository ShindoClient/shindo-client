package me.miki.shindo.gui.mainmenu.impl

import me.miki.extensions.ui.animation.setAnimation
import me.miki.extensions.ui.nanovg.drawCenteredText
import me.miki.extensions.ui.nanovg.drawOutlineRoundedRect
import me.miki.extensions.ui.nanovg.drawRoundedRect
import me.miki.extensions.ui.nanovg.drawText
import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.gui.modmenu.v2.GuiModMenu
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.DecelerateAnimation
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils.isInside
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.util.ResourceLocation
import java.awt.Color

class MainScene(
    parent: GuiShindoMainMenu,
) : MainMenuScene(parent) {
    private val introAnimation: DecelerateAnimation = DecelerateAnimation(800, 1.0)

    private var singleHover = 0f
    private var multiHover = 0f
    private var exitHover = 0f
    private var bgHover = 0f

    private var featherHover = 0f
    private var settingsHover = 0f
    private var diamondHover = 0f

    private var confirmingExit = false
    private val confirmAnim: SimpleAnimation = SimpleAnimation(0f)

    override fun initScene() {
        introAnimation.reset()
        introAnimation.setDirection(Direction.FORWARDS)
        confirmingExit = false
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance: Shindo = Shindo.getInstance()

        if (instance.isUpdateNeeded()) {
            instance.setUpdateNeeded(false)
            this.setCurrentScene(this.getSceneByClass(UpdateScene::class.java))
            return
        }

        val nvg = instance.nanoVGManager
        nvg!!.setupAndDraw(Runnable { drawNanoVG(instance, nvg, mouseX, mouseY, partialTicks) })
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

        // Blur + overlay
        // Blur.drawBlur(0f, 0f, sw, sh, 0f)

        // Logo
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
        // nvg.drawText(Lucide.BOOKMARK, centerX + 35, logoY + 12, Color(255, 255, 255, (anim * 150).toInt()), 18, Fonts.LUCIDE)

        val btnW = 180f
        val btnH = 20f
        val spacing = 26f
        val startY = sh / 2f - 10
        val btnX = centerX - (btnW / 2f)

        singleHover = lerp(singleHover, if (isInside(mouseX, mouseY, btnX, startY, btnW, btnH)) 1f else 0f, 0.2f)
        multiHover =
            lerp(multiHover, if (isInside(mouseX, mouseY, btnX, startY + spacing, btnW, btnH)) 1f else 0f, 0.2f)
        exitHover =
            lerp(exitHover, if (isInside(mouseX, mouseY, btnX, startY + spacing * 2, btnW, btnH)) 1f else 0f, 0.2f)

        nvg.drawGlassButton(TranslateText.SINGLEPLAYER.getText(), centerX, startY, btnW, btnH, singleHover, anim, false)
        nvg.drawGlassButton(
            TranslateText.MULTIPLAYER.getText(),
            centerX,
            startY + spacing,
            btnW,
            btnH,
            multiHover,
            anim,
            false,
        )
        nvg.drawGlassButton("QUIT GAME", centerX, startY + spacing * 2, btnW, btnH, exitHover, anim, true)

        val bgSize = 36f
        val bgX = 10f
        val bgY = sh - bgSize - 16
        bgHover = lerp(bgHover, if (isInside(mouseX, mouseY, bgX, bgY, bgSize, bgSize)) 1f else 0f, 0.2f)
        nvg.drawRoundedRect(
            bgX,
            bgY,
            bgSize,
            bgSize,
            6,
            Color(15, 15, 20, (anim * (160 + bgHover * 60)).toInt()),
        )
        nvg.drawOutlineRoundedRect(
            bgX,
            bgY,
            bgSize,
            bgSize,
            6,
            1.2f,
            Color(255, 255, 255, (anim * (40 + bgHover * 80)).toInt()),
        )
        nvg.drawCenteredText(
            Lucide.IMAGE,
            bgX + bgSize / 2f,
            bgY + bgSize / 2f - 10,
            Color(255, 255, 255, (anim * (180 + bgHover * 75)).toInt()),
            22,
            Fonts.LUCIDE,
        )

        confirmAnim.setAnimation(if (confirmingExit) 1f else 0f, 14)
        if (confirmAnim.getValue() > 0.01f) {
            drawConfirmExit(nvg, sw, sh, mouseX, mouseY, anim * confirmAnim.getValue())
        }

        // Top right
        drawTopRight(nvg, mouseX, mouseY, sw, anim)
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

        nvg.drawCenteredText(
            "Are you sure?",
            sw / 2f,
            py + 14,
            Color(255, 255, 255, (alpha * 230).toInt()),
            11,
            Fonts.SEMIBOLD,
        )

        val btnW = 70f
        val btnH = 22f
        val yesX = sw / 2f - btnW - 6
        val btnY = py + ph - 32
        val yesHov = isInside(mouseX, mouseY, yesX, btnY, btnW, btnH)
        nvg.drawRoundedRect(yesX, btnY, btnW, btnH, 5, Color(180, 30, 30, (alpha * (if (yesHov) 220 else 160)).toInt()))
        nvg.drawOutlineRoundedRect(yesX, btnY, btnW, btnH, 5, 1f, Color(255, 80, 80, (alpha * 120).toInt()))
        nvg.drawCenteredText(
            "Quit",
            yesX + btnW / 2f,
            btnY + 6,
            Color(255, 220, 220, (alpha * 230).toInt()),
            9.5f,
            Fonts.SEMIBOLD,
        )

        val noX = sw / 2f + 6
        val noHov = isInside(mouseX, mouseY, noX, btnY, btnW, btnH)
        nvg.drawRoundedRect(noX, btnY, btnW, btnH, 5, Color(40, 40, 50, (alpha * (if (noHov) 220 else 160)).toInt()))
        nvg.drawOutlineRoundedRect(noX, btnY, btnW, btnH, 5, 1f, Color(255, 255, 255, (alpha * 50).toInt()))
        nvg.drawCenteredText(
            "Cancel",
            noX + btnW / 2f,
            btnY + 6,
            Color(200, 200, 200, (alpha * 230).toInt()),
            9.5f,
            Fonts.SEMIBOLD,
        )
    }

    private fun drawTopRight(
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
        sw: Float,
        anim: Float,
    ) {
        val topY = 10f
        val btnS = 24f
        val btnSp = 6f
        var rightX = sw - 10

        // Diamond
        // diamondHover = lerp(diamondHover, if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) 1f else 0f, 0.2f)
        // drawCornerIcon(nvg, rightX - btnS, topY, btnS, Shinconic.SHINDO, diamondHover, anim, true)

        // Settings
        // rightX -= (btnS + btnSp)
        settingsHover =
            lerp(settingsHover, if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) 1f else 0f, 0.2f)
        nvg.drawGlassButtonWithIcon(Lucide.SETTINGS, rightX - btnS, topY, btnS, settingsHover, anim)

        // Logo
        rightX -= (btnS + btnSp)
        featherHover =
            lerp(featherHover, if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) 1f else 0f, 0.2f)
        nvg.drawGlassButtonWithIcon(Shinconic.SHINDO, rightX - btnS, topY, btnS, featherHover, anim)

        // Profile box
        rightX -= (btnS + btnSp)
        val name = mc.session.username
        val nameW = nvg.getTextWidth(name, 10f, Fonts.SEMIBOLD)
        val profW = nameW + 45
        val profX = rightX - profW

        nvg.drawRoundedRect(profX, topY, profW, btnS, 5, Color(20, 20, 25, (anim * 180).toInt()))
        nvg.drawOutlineRoundedRect(profX, topY, profW, btnS, 5, 1f, Color(255, 255, 255, (anim * 30).toInt()))

        val skin: ResourceLocation
        if (mc.session.profile != null && mc.session.profile.id != null) {
            skin = DefaultPlayerSkin.getDefaultSkin(mc.session.profile.id)
        } else {
            skin = DefaultPlayerSkin.getDefaultSkinLegacy()
        }
        nvg.drawPlayerHead(skin, profX + 4, topY + 4, 16f, 16f, 3f, anim)

        nvg.drawText(
            name,
            profX + 26,
            topY + btnS / 2f - 5f,
            Color(255, 255, 255, (anim * 230).toInt()),
            10,
            Fonts.SEMIBOLD,
        )
    }

    private fun lerp(
        current: Float,
        target: Float,
        factor: Float,
    ): Float = current + (target - current) * factor

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) return

        val sr = ScaledResolution(mc)
        val sw = sr.scaledWidth.toFloat()
        val sh = sr.scaledHeight.toFloat()
        val centerX = sw / 2f
        val btnW = 180f
        val btnH = 20f
        val spacing = 26f
        val startY = sh / 2f - 10
        val btnX = centerX - (btnW / 2f)

        if (confirmingExit) {
            val pw = 200f
            val ph = 80f
            val px = sw / 2f - pw / 2f
            val py = sh / 2f - ph / 2f
            val btnBW = 70f
            val btnBH = 22f
            val btnBY = py + ph - 32

            val yesX = sw / 2f - btnBW - 6
            val noX = sw / 2f + 6

            if (isInside(mouseX, mouseY, yesX, btnBY, btnBW, btnBH)) {
                mc.shutdown()
            } else if (isInside(mouseX, mouseY, noX, btnBY, btnBW, btnBH)) {
                confirmingExit = false
            }
            return
        }

        if (isInside(mouseX, mouseY, btnX, startY, btnW, btnH)) {
            mc.displayGuiScreen(GuiSelectWorld(this.getParent()))
        } else if (isInside(mouseX, mouseY, btnX, startY + spacing, btnW, btnH)) {
            mc.displayGuiScreen(GuiMultiplayer(this.getParent()))
        } else if (isInside(mouseX, mouseY, btnX, startY + spacing * 2, btnW, btnH)) {
            confirmingExit = true
            return
        }

        val bgSize = 36f
        val bgX = 10f
        val bgY = sh - bgSize - 10
        if (isInside(mouseX, mouseY, bgX, bgY, bgSize, bgSize)) {
            this.setCurrentScene(this.getSceneByClass(BackgroundScene::class.java))
            return
        }

        val topY = 10f
        val btnS = 24f
        val btnSp = 6f
        var rightX = sw - 10

        // if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) {
        //    mc.displayGuiScreen(GuiModMenu())
        // }
        // rightX -= (btnS + btnSp)
        if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) {
            mc.displayGuiScreen(GuiOptions(this.getParent(), mc.gameSettings))
        }
        rightX -= (btnS + btnSp)
        if (isInside(mouseX, mouseY, rightX - btnS, topY, btnS, btnS)) {
            mc.displayGuiScreen(GuiModMenu())
        }
    }
}
