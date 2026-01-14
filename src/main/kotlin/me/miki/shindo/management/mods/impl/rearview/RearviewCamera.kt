package me.miki.shindo.management.mods.impl.rearview

import me.miki.shindo.injection.mixin.interfaces.client.IMixinMinecraft
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.ARBFramebufferObject
import org.lwjgl.opengl.GL11
import java.nio.IntBuffer

class RearviewCamera {
    private val mc: Minecraft = Minecraft.getMinecraft()

    private val mirrorFBO: Int
    val texture: Int
    private val mirrorDepth: Int
    private val mirrorRenderGlobal: RenderGlobalHelper
    private var renderEndNanoTime: Long = 0
    private var fov: Float
    private var firstUpdate = false
    var isRecording: Boolean = false
        private set
    private var lockCamera: Boolean

    init {
        mirrorFBO = ARBFramebufferObject.glGenFramebuffers()
        this.texture = GL11.glGenTextures()
        mirrorDepth = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, 800, 600, 0, GL11.GL_RGBA, GL11.GL_INT,
            null as IntBuffer?
        )
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorDepth)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, 800, 600, 0, GL11.GL_DEPTH_COMPONENT,
            GL11.GL_INT, null as IntBuffer?
        )
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

        mirrorRenderGlobal = RenderGlobalHelper()
        fov = 70f
        lockCamera = true
    }

    fun updateMirror() {
        val w: Int
        val h: Int
        val y: Float
        val py: Float
        val p: Float
        val pp: Float
        val hide: Boolean
        val view: Int
        val limit: Int
        var endTime: Long = 0

        val currentScreen: GuiScreen?

        if (!this.firstUpdate) {
            mc.renderGlobal.loadRenderers()
            this.firstUpdate = true
        }

        w = mc.displayWidth
        h = mc.displayHeight
        y = (mc as IMixinMinecraft).getRenderViewEntity().rotationYaw
        py = (mc as IMixinMinecraft).getRenderViewEntity().prevRotationYaw
        p = (mc as IMixinMinecraft).getRenderViewEntity().rotationPitch
        pp = (mc as IMixinMinecraft).getRenderViewEntity().prevRotationPitch
        hide = mc.gameSettings.hideGUI
        view = mc.gameSettings.thirdPersonView
        limit = mc.gameSettings.limitFramerate
        fov = mc.gameSettings.fovSetting
        currentScreen = mc.currentScreen

        switchToFB()

        if (limit != 0) {
            endTime = renderEndNanoTime
        }

        mc.currentScreen = null
        mc.displayHeight = 600
        mc.displayWidth = 800
        mc.gameSettings.hideGUI = true
        mc.gameSettings.thirdPersonView = 0
        mc.gameSettings.limitFramerate = 0
        mc.gameSettings.fovSetting = fov

        (mc as IMixinMinecraft).getRenderViewEntity().rotationYaw += 180f
        (mc as IMixinMinecraft).getRenderViewEntity().prevRotationYaw += 180f

        if (lockCamera) {
            (mc as IMixinMinecraft).getRenderViewEntity().rotationPitch = 0f
            (mc as IMixinMinecraft).getRenderViewEntity().prevRotationPitch = 0f
        } else {
            (mc as IMixinMinecraft).getRenderViewEntity().rotationPitch = -p + 18
            (mc as IMixinMinecraft).getRenderViewEntity().prevRotationPitch = -pp + 18
        }

        this.isRecording = true
        mirrorRenderGlobal.switchTo()

        GL11.glPushAttrib(272393)

        mc.entityRenderer.renderWorld((mc as IMixinMinecraft).getTimer().renderPartialTicks, System.nanoTime())
        mc.entityRenderer.setupOverlayRendering()

        if (limit != 0) {
            renderEndNanoTime = endTime
        }

        GL11.glPopAttrib()

        mirrorRenderGlobal.switchFrom()
        this.isRecording = false

        mc.currentScreen = currentScreen
        (mc as IMixinMinecraft).getRenderViewEntity().rotationYaw = y
        (mc as IMixinMinecraft).getRenderViewEntity().prevRotationYaw = py
        (mc as IMixinMinecraft).getRenderViewEntity().rotationPitch = p
        (mc as IMixinMinecraft).getRenderViewEntity().prevRotationPitch = pp
        mc.gameSettings.limitFramerate = limit
        mc.gameSettings.thirdPersonView = view
        mc.gameSettings.hideGUI = hide
        mc.displayWidth = w
        mc.displayHeight = h
        mc.gameSettings.fovSetting = fov

        switchFromFB()
    }

    private fun switchToFB() {
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()

        OpenGlHelper.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, mirrorFBO)
        OpenGlHelper.glFramebufferTexture2D(
            OpenGlHelper.GL_FRAMEBUFFER,
            OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
            this.texture, 0
        )
        OpenGlHelper.glFramebufferTexture2D(
            OpenGlHelper.GL_FRAMEBUFFER,
            OpenGlHelper.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
            mirrorDepth, 0
        )
    }

    private fun switchFromFB() {
        OpenGlHelper.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, 0)
    }

    fun setFov(fov: Float) {
        this.fov = fov
    }

    fun setLockCamera(lockCamera: Boolean) {
        this.lockCamera = lockCamera
    }
}
