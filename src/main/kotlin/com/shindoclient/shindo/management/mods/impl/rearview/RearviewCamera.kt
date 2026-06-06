package com.shindoclient.shindo.management.mods.impl.rearview

import com.shindoclient.shindo.injection.interfaces.IMixinMinecraft
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.entity.Entity
import org.lwjgl.opengl.ARBFramebufferObject
import org.lwjgl.opengl.GL11
import java.nio.IntBuffer

class RearviewCamera {
    private val mc: Minecraft = Minecraft.getMinecraft()

    private val mirrorFBO: Int = ARBFramebufferObject.glGenFramebuffers()
    val texture: Int = GL11.glGenTextures()
    private val mirrorDepth: Int = GL11.glGenTextures()
    private val mirrorRenderGlobal: RenderGlobalHelper
    private var renderEndNanoTime: Long = 0
    private var fov: Float
    private var firstUpdate = false
    var isRecording: Boolean = false
        private set
    private var lockCamera: Boolean

    init {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGB8,
            800,
            600,
            0,
            GL11.GL_RGBA,
            GL11.GL_INT,
            null as IntBuffer?,
        )
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorDepth)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_DEPTH_COMPONENT,
            800,
            600,
            0,
            GL11.GL_DEPTH_COMPONENT,
            GL11.GL_INT,
            null as IntBuffer?,
        )
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

        mirrorRenderGlobal = RenderGlobalHelper()
        fov = 70f
        lockCamera = true
    }

    fun updateMirror() {
        val y: Float
        val py: Float
        val p: Float
        val pp: Float
        var endTime: Long = 0

        if (!this.firstUpdate) {
            mc.renderGlobal.loadRenderers()
            this.firstUpdate = true
        }

        val w: Int = mc.displayWidth
        val h: Int = mc.displayHeight
        val rve = (mc as IMixinMinecraft).renderViewEntity as Entity
        y = rve.rotationYaw
        py = rve.prevRotationYaw
        p = rve.rotationPitch
        pp = rve.prevRotationPitch
        val hide: Boolean = mc.gameSettings.hideGUI
        val view: Int = mc.gameSettings.thirdPersonView
        val limit: Int = mc.gameSettings.limitFramerate
        fov = mc.gameSettings.fovSetting
        val currentScreen: GuiScreen? = mc.currentScreen

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

        rve.rotationYaw += 180f
        rve.prevRotationYaw += 180f

        if (lockCamera) {
            rve.rotationPitch = 0f
            rve.prevRotationPitch = 0f
        } else {
            rve.rotationPitch = -p + 18
            rve.prevRotationPitch = -pp + 18
        }

        this.isRecording = true
        mirrorRenderGlobal.switchTo()

        GL11.glPushAttrib(272393)

        mc.entityRenderer.renderWorld(
            ((mc as IMixinMinecraft).timer as net.minecraft.util.Timer).renderPartialTicks,
            System.nanoTime(),
        )
        mc.entityRenderer.setupOverlayRendering()

        if (limit != 0) {
            renderEndNanoTime = endTime
        }

        GL11.glPopAttrib()

        mirrorRenderGlobal.switchFrom()
        this.isRecording = false

        mc.currentScreen = currentScreen
        rve.rotationYaw = y
        rve.prevRotationYaw = py
        rve.rotationPitch = p
        rve.prevRotationPitch = pp
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
            OpenGlHelper.GL_COLOR_ATTACHMENT0,
            GL11.GL_TEXTURE_2D,
            this.texture,
            0,
        )
        OpenGlHelper.glFramebufferTexture2D(
            OpenGlHelper.GL_FRAMEBUFFER,
            OpenGlHelper.GL_DEPTH_ATTACHMENT,
            GL11.GL_TEXTURE_2D,
            mirrorDepth,
            0,
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
