package me.miki.shindo.gui

import eu.shoroa.contrib.render.ShBlur
import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventRenderNotification
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.utils.MathUtils
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.easing.EaseBackIn
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.nanovg.NanoVG
import java.awt.Color
import java.io.IOException
import java.util.Collections
import kotlin.math.max
import kotlin.math.min

class GuiEditHUD(private val fromModMenu: Boolean) : GuiScreen() {

    private val mods: ArrayList<HUDMod> = ArrayList(Shindo.getInstance().modManager.getHudMods())
    private var localMouseX = -1
    private var localMouseY = -1
    private lateinit var introAnimation: Animation
    private var snapping = false
    private var canSnap = false

    init {
        mods.reverse()
    }

    override fun initGui() {
        for (m in mods) {
            m.setDragging(false)
            m.animation.value = 0F
        }

        introAnimation = EaseBackIn(500, 1.0, 0f)
        introAnimation.setDirection(Direction.FORWARDS)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager ?: return
        val palette: ColorPalette = instance.colorManager.palette
        val shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
        localMouseX = mouseX
        localMouseY = mouseY

        snapping = false

        if (introAnimation.isDone(Direction.BACKWARDS)) {
            mc.displayGuiScreen(null)
        }
        if (!InternalSettingsMod.getInstance().blurSetting.isToggled()) {
            BlurUtils.drawBlurScreen((min(introAnimation.getValue(), 1.0) * 20).toFloat() + 1f)
        }

        nvg.setupAndDraw(Runnable {
            nvg.save()
            NanoVG.nvgGlobalAlpha(nvg.getContext(), introAnimation.getValue().toFloat())
            if (InternalSettingsMod.getInstance().blurSetting.isToggled()) {
                ShBlur.getInstance().drawBlur {
                    nvg.drawRect(0f, 0f, sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), Color.WHITE)
                }
            }
            nvg.restore()
            nvg.drawRect(0f, 0f, sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, (introAnimation.getValue() * 100).toInt()))
            val halfScreenWidth = sr.scaledWidth / 2
            val halfScreenHeight = sr.scaledHeight / 2

            // guide lines
            nvg.drawRect(0f, halfScreenHeight.toFloat(), sr.scaledWidth.toFloat(), 0.5f, palette.getBackgroundColor(ColorType.DARK))
            nvg.drawRect(halfScreenWidth.toFloat(), 0f, 0.5f, sr.scaledHeight.toFloat(), palette.getBackgroundColor(ColorType.DARK))
            // todo add more splashers
            nvg.drawCenteredText(TranslateText.HUD_RESIZE_HINT.text, sr.scaledWidth / 2f, sr.scaledHeight - 15f, Color(255, 255, 255, 200), 8f, Fonts.REGULAR)

            for (m in mods) {
                if (m.isToggled() && !m.isHide()) {
                    val topMost = mods.firstOrNull { it.isToggled() && MouseUtils.isInside(mouseX, mouseY, it.getX(), it.getY(), it.getWidth(), it.getHeight()) }
                    val isInside = MouseUtils.isInside(mouseX, mouseY, m.getX(), m.getY(), m.getWidth(), m.getHeight()) && topMost == m

                    if (isInside) {
                        val dWheel = Mouse.getDWheel()

                        if (dWheel != 0) {
                            val scaleChange = if (shift) 0.02f else 0.1f

                            var newScale = m.getScale()

                            if (dWheel > 0) {
                                newScale += scaleChange
                            }

                            if (dWheel < 0) {
                                newScale -= scaleChange
                            }

                            val roundedScale = kotlin.math.round(newScale * 100.0f) / 100.0f

                            m.setScale(roundedScale)
                        }
                    }
                    if (shift) canSnap = false
                    m.animation.setAnimation(if (isInside) 1.0f else 0.0f, 14.0)

                    if (m.isDragging()) {
                        m.setX(mouseX + m.getDraggingX())
                        m.setY(mouseY + m.getDraggingY())
                    }

                    val modX = m.getX()
                    val modY = m.getY()
                    val modWidth = m.getWidth()
                    val modHeight = m.getHeight()

                    val snapRange = 5

                    m.setX(max(0, min(modX, sr.scaledWidth - modWidth)))
                    m.setY(max(0, min(modY, sr.scaledHeight - modHeight)))

                    if (canSnap) {
                        if (MathUtils.isInRange(modX + (modWidth / 2f), halfScreenWidth - snapRange.toFloat(), halfScreenWidth + snapRange.toFloat())) {
                            m.setX(halfScreenWidth - (modWidth / 2))
                        }

                        if (MathUtils.isInRange(modY + (modHeight / 2f), halfScreenHeight - snapRange.toFloat(), halfScreenHeight + snapRange.toFloat())) {
                            m.setY(halfScreenHeight - (modHeight / 2))
                        }
                    }

                    for (m2 in instance.modManager.getHudMods()) {
                        if (m2.isToggled() && m.isDragging() && m2 != m && !snapping && canSnap) {
                            val mod2X = m2.getX()
                            val mod2Y = m2.getY()
                            val mod2Width = m2.getWidth()
                            val mod2Height = m2.getHeight()

                            if (MathUtils.isInRange(mod2X.toFloat(), (modX - snapRange).toFloat(), (modX + snapRange).toFloat())) {
                                nvg.drawRect(mod2X.toFloat(), 0f, 0.5f, sr.scaledHeight.toFloat(), Color(217, 60, 255))
                                snapping = true
                                m.setX(mod2X)
                            }

                            if (MathUtils.isInRange(mod2Y.toFloat(), (modY - snapRange).toFloat(), (modY + snapRange).toFloat())) {
                                nvg.drawRect(0f, mod2Y.toFloat(), sr.scaledWidth.toFloat(), 0.5f, Color(217, 60, 255))
                                snapping = true
                                m.setY(mod2Y)
                            }

                            if (MathUtils.isInRange((mod2X + mod2Width).toFloat(), (modX - snapRange).toFloat(), (modX + snapRange).toFloat())) {
                                nvg.drawRect((mod2X + mod2Width).toFloat(), 0f, 0.5f, sr.scaledHeight.toFloat(), Color(217, 60, 255))
                                snapping = true
                                m.setX(mod2X + mod2Width)
                            }

                            if (MathUtils.isInRange((mod2Y + mod2Height).toFloat(), (modY - snapRange).toFloat(), (modY + snapRange).toFloat())) {
                                nvg.drawRect(0f, (mod2Y + mod2Height).toFloat(), sr.scaledWidth.toFloat(), 0.5f, Color(217, 60, 255))
                                snapping = true
                                m.setY(mod2Y + mod2Height)
                            }

                            if (MathUtils.isInRange(mod2X.toFloat(), (modX + modWidth - snapRange).toFloat(), (modX + modWidth + snapRange).toFloat())) {
                                nvg.drawRect(mod2X.toFloat(), 0f, 0.5f, sr.scaledHeight.toFloat(), Color(217, 60, 255))
                                snapping = true
                                m.setX(mod2X - modWidth)
                            }

                            if (MathUtils.isInRange(mod2Y.toFloat(), (modY + modHeight - snapRange).toFloat(), (modY + modHeight + snapRange).toFloat())) {
                                nvg.drawRect(0f, mod2Y.toFloat(), sr.scaledWidth.toFloat(), 0.5f, Color(217, 60, 255))
                                snapping = true
                                m.setY(mod2Y - modHeight)
                            }

                            if (MathUtils.isInRange((mod2X + mod2Width).toFloat(), (modX + modWidth - snapRange).toFloat(), (modX + modWidth + snapRange).toFloat())) {
                                nvg.drawRect((mod2X + mod2Width).toFloat(), 0f, 0.5f, sr.scaledHeight.toFloat(), Color(217, 60, 255))
                                snapping = true
                                m.setX(mod2X + mod2Width - modWidth)
                            }

                            if (MathUtils.isInRange((mod2Y + mod2Height).toFloat(), (modY + modHeight - snapRange).toFloat(), (modY + modHeight + snapRange).toFloat())) {
                                nvg.drawRect(0f, (mod2Y + mod2Height).toFloat(), sr.scaledWidth.toFloat(), 0.5f, Color(217, 60, 255))
                                snapping = true
                                m.setY(mod2Y + mod2Height - modHeight)
                            }
                        }
                    }
                }

                nvg.drawOutlineRoundedRect(m.getX() - 2f, m.getY() - 2f, m.getWidth() + 4f, m.getHeight() + 4f, 6.5f * m.getScale(), 2f, palette.getBackgroundColor(ColorType.DARK, (m.animation.value * 255).toInt()))
            }
        })

        EventRender2D(partialTicks).call()
        EventRenderNotification().call()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (m in mods) {
            if (m.isToggled() && !m.isHide()) {
                val topMost = mods.firstOrNull { it.isToggled() && MouseUtils.isInside(mouseX, mouseY, it.getX(), it.getY(), it.getWidth(), it.getHeight()) }
                val isInside = MouseUtils.isInside(mouseX, mouseY, m.getX(), m.getY(), m.getWidth(), m.getHeight()) && topMost == m

                if (mouseButton == 0) {
                    canSnap = true
                }

                // right click to remove
                if (mouseButton == 1) {
                    if (isInside) {
                        m.toggle()
                        initGui()
                        return
                    }
                }
                // middle click resets scale
                if (mouseButton == 2 && isInside) {
                    m.setScale(1.0f)
                }

                if (isInside) {
                    m.setDragging(true)
                    m.setDraggingX(m.getX() - mouseX)
                    m.setDraggingY(m.getY() - mouseY)
                }
            }
        }

    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (m in mods) {
            m.setDragging(false)
        }

        try {
            super.mouseClicked(mouseX, mouseY, mouseButton)
        } catch (_: IOException) {
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (fromModMenu) {
                mc.displayGuiScreen(Shindo.getInstance().shindoAPI.modMenu)
            } else {
                introAnimation.setDirection(Direction.BACKWARDS)
            }
        }
        for (m in mods) {
            if (m.isToggled() && !m.isHide()) {
                val topMost = mods.firstOrNull { it.isToggled() && MouseUtils.isInside(localMouseX, localMouseY, it.getX(), it.getY(), it.getWidth(), it.getHeight()) }
                val isInside = MouseUtils.isInside(localMouseX, localMouseY, m.getX(), m.getY(), m.getWidth(), m.getHeight()) && topMost == m

                // backspace to remove
                if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) {
                    if (isInside) {
                        m.toggle()
                        initGui()
                        return
                    }
                }
            }
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}
