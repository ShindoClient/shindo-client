package me.miki.shindo.gui

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.quickplay.QuickPlay
import me.miki.shindo.management.quickplay.QuickPlayCommand
import me.miki.shindo.management.quickplay.QuickPlayManager
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.easing.EaseBackIn
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation
import me.miki.shindo.utils.buffer.ScreenAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard

class GuiQuickPlay : GuiScreen(), IShindoScreen {

    private val scroll = Scroll()
    private val screenAnimation = ScreenAnimation()
    private lateinit var introAnimation: Animation
    private lateinit var sceneChangeAnimation: Animation

    private var currentQuickPlay: QuickPlay? = null
    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0

    override fun initGui() {
        val sr = ScaledResolution(mc)

        val addX = 190
        val addY = 110

        x = (sr.scaledWidth / 2) - addX
        y = (sr.scaledHeight / 2) - addY
        width = addX * 2
        height = addY * 2

        introAnimation = EaseBackIn(320, 1.0, 2.0f)
        introAnimation.setDirection(Direction.FORWARDS)
        sceneChangeAnimation = SmoothStepAnimation(260, 1.0)
        sceneChangeAnimation.setValue(1.0)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvg = Shindo.getInstance().nanoVGManager

        BlurUtils.drawBlurScreen(20F)

        screenAnimation.wrap(Runnable {
            nvg!!.drawShadow(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 12f)
        }, 2 - introAnimation.getValueFloat(), introAnimation.getValueFloat().coerceAtMost(1f))

        screenAnimation.wrap({ drawNanoVG() }, x, y, width, height, 2 - introAnimation.getValueFloat(),
            introAnimation.getValueFloat().coerceAtMost(1f), true)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG() {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager
        val palette: ColorPalette = instance.colorManager.palette
        val quickPlayManager: QuickPlayManager = instance.quickPlayManager

        var offsetX = 0
        var offsetY = 0
        var index = 1

        if (introAnimation.isDone(Direction.BACKWARDS)) {
            mc.displayGuiScreen(null)
        }

        if (sceneChangeAnimation.isDone(Direction.FORWARDS)) {
            currentQuickPlay = null
        }

        nvg!!.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 12f, palette.getBackgroundColor(ColorType.NORMAL))
        nvg.drawCenteredText("Choose a " + if (currentQuickPlay != null) "Mode" else "Game", x + (width / 2f), y + 10f, palette.getFontColor(ColorType.DARK), 15f, Fonts.MEDIUM)

        nvg.save()
        nvg.translate(-(600 - (sceneChangeAnimation.getValue() * 600)).toFloat(), 0f)

        for (q in quickPlayManager.quickPlays) {
            nvg.drawRoundedRect(x + 15f + offsetX, y + 42f + offsetY, 110f, 42f, 6f, palette.getBackgroundColor(ColorType.DARK))
            nvg.drawRoundedImage(q.icon, x + 20f + offsetX, y + 47f + offsetY, 32f, 32f, 6f)

            nvg.drawText(q.name, x + 58f + offsetX, y + 50f + offsetY, palette.getFontColor(ColorType.DARK), 10f, Fonts.MEDIUM)

            offsetX += 120

            if (index % 3 == 0) {
                offsetX = 0
                offsetY += 52
            }

            index++
        }

        nvg.restore()

        nvg.save()
        nvg.translate((sceneChangeAnimation.getValue() * 600).toFloat(), 0f)

        val selected = currentQuickPlay
        if (selected != null) {
            var prevIndex = 0

            index = 1
            offsetX = 0
            offsetY = 0

            scroll.onScroll()
            scroll.onAnimation()

            nvg.scissor(x.toFloat(), y + 29f, width.toFloat(), height.toFloat())
            nvg.translate(0f, scroll.getValue())

            nvg.drawRoundedImage(selected.icon, x + (width / 2f) - (46 / 2f), y + 40f, 46f, 46f, 6f)
            nvg.drawCenteredText(selected.name, x + (width / 2f), y + 94f, palette.getFontColor(ColorType.DARK), 12f, Fonts.MEDIUM)

            for (c in selected.commands) {
                nvg.drawRoundedRect(x + 15f + offsetX, y + 112f + offsetY, 110f, 20f, 6f, palette.getBackgroundColor(ColorType.DARK))
                nvg.drawCenteredText(c.name, x + 15f + offsetX + (110 / 2f), y + 118.5f + offsetY, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.REGULAR)

                offsetX += 120

                if (index % 3 == 0) {
                    offsetY += 30
                    offsetX = 0
                    prevIndex++
                }

                index++
            }

            scroll.maxScroll = if (prevIndex <= 3) 0f else (((prevIndex + if (prevIndex % 3 == 0) 0.5f else 0f) * 30) / 1.48f) - 30
        }

        nvg.restore()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val instance = Shindo.getInstance()
        val quickPlayManager = instance.quickPlayManager

        var offsetX = 0
        var offsetY = 0
        var index = 1

        super.mouseClicked(mouseX, mouseY, mouseButton)

        if (currentQuickPlay == null) {
            for (q in quickPlayManager.quickPlays) {
                if (MouseUtils.isInside(mouseX, mouseY, x + 15f + offsetX, y + 42f + offsetY, 110f, 42f) && mouseButton == 0) {
                    scroll.resetAll()
                    currentQuickPlay = q
                    sceneChangeAnimation.setDirection(Direction.BACKWARDS)
                    return
                }

                offsetX += 120

                if (index % 3 == 0) {
                    offsetX = 0
                    offsetY += 52
                }

                index++
            }
        } else {
            index = 1
            offsetX = 0
            offsetY = (0 + scroll.getValue()).toInt()

            val selected = currentQuickPlay
            if (selected != null) {
                for (c in selected.commands) {
                    if (MouseUtils.isInside(mouseX, mouseY, x + 15f + offsetX, y + 112f + offsetY, 110f, 20f) && mouseButton == 0 && sceneChangeAnimation.isDone(Direction.BACKWARDS)) {
                        mc.thePlayer.sendChatMessage(c.command)
                    }

                    offsetX += 120

                    if (index % 3 == 0) {
                        offsetY += 30
                        offsetX = 0
                    }

                    index++
                }
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (currentQuickPlay != null) {
                sceneChangeAnimation.setDirection(Direction.FORWARDS)
            } else {
                introAnimation.setDirection(Direction.BACKWARDS)
            }
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}
