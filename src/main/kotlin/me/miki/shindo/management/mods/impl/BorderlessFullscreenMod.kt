package me.miki.shindo.management.mods.impl

import me.miki.shindo.injection.interfaces.IMixinMinecraft
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventPreRenderTick
import me.miki.shindo.management.event.impl.EventToggleFullscreen
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode

class BorderlessFullscreenMod :
    Mod(
        TranslateText.BORDERLESS_FULSCREEN,
        TranslateText.BORDERLESS_FULLSCREEN_DESCRIPTION,
        ModCategory.OTHER,
        Shinconic.MOD_BORDERLESS_FULLSCREEN,
    ) {
    private var prevX = 0
    private var prevY = 0
    private var prevWidth = 0
    private var prevHeight = 0

    private var fullscreenTime: Long = -1

    @EventTarget
    fun onRenderTick(event: EventPreRenderTick?) {
        if (fullscreenTime != -1L && System.currentTimeMillis() - fullscreenTime >= 100) {
            fullscreenTime = -1

            if (mc.inGameHasFocus) {
                mc.mouseHelper.grabMouseCursor()
            }
        }
    }

    @EventTarget
    fun onFullscreenToggle(event: EventToggleFullscreen) {
        event.isApplyState = false
        setBorderlessFullscreen(event.state)
    }

    override fun onEnable() {
        super.onEnable()

        if (mc.isFullScreen) {
            setBorderlessFullscreen(true)
        }
    }

    override fun onDisable() {
        super.onDisable()

        if (mc.isFullScreen) {
            setBorderlessFullscreen(false)
            mc.toggleFullscreen()
            mc.toggleFullscreen()
        }
    }

    private fun setBorderlessFullscreen(state: Boolean) {
        try {
            System.setProperty("org.lwjgl.opengl.Window.undecorated", state.toString())
            Display.setFullscreen(false)
            Display.setResizable(!state)

            if (state) {
                prevX = Display.getX()
                prevY = Display.getY()
                prevWidth = mc.displayWidth
                prevHeight = mc.displayHeight
                Display.setDisplayMode(
                    DisplayMode(
                        Display.getDesktopDisplayMode().width,
                        Display.getDesktopDisplayMode().height,
                    ),
                )
                Display.setLocation(0, 0)
                (mc as IMixinMinecraft).resizeWindow(
                    Display.getDesktopDisplayMode().width,
                    Display.getDesktopDisplayMode().height,
                )
            } else {
                Display.setDisplayMode(DisplayMode(prevWidth, prevHeight))
                Display.setLocation(prevX, prevY)
                (mc as IMixinMinecraft).resizeWindow(prevWidth, prevHeight)

                if (mc.inGameHasFocus) {
                    mc.mouseHelper.ungrabMouseCursor()
                    fullscreenTime = System.currentTimeMillis()
                }
            }
        } catch (error: LWJGLException) {
            ShindoLogger.error("Could not totggle borderless fullscreen", error)
        }
    }
}
