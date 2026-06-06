package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.injection.interfaces.IMixinMinecraft
import com.shindoclient.shindo.injection.interfaces.IMixinRenderManager
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventJump
import com.shindoclient.shindo.management.event.impl.EventRender3D
import com.shindoclient.shindo.management.event.impl.EventUpdate
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class JumpCircleMod :
    Mod(
        TranslateText.JUMP_CIRCLE,
        TranslateText.JUMP_CIRCLE_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_JUMP_CIRCLE,
    ) {
    private val circles: MutableList<JumpCircle> = ArrayList<JumpCircle>()
    private var jumping = false

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        if (jumping && mc.thePlayer.onGround) {
            jumping = false
            circles.add(JumpCircle(mc.thePlayer.positionVector))
        }

        circles.removeIf { obj: JumpCircle? -> obj!!.update() }
    }

    @EventTarget
    fun onJump(event: EventJump?) {
        jumping = true
    }

    @EventTarget
    fun onRender3D(event: EventRender3D?) {
        val currentColor = Shindo.getInstance().getColorManager().getCurrentColor()

        GL11.glPushMatrix()
        GL11.glEnable(3042)
        GL11.glDisable(3008)
        GL11.glDisable(2884)
        GL11.glDisable(3553)
        GL11.glShadeModel(7425)

        for (circle in circles) {
            GL11.glBegin(8)
            var i = 0
            while (i <= 360) {
                val red = (currentColor.getInterpolateColor().rgb shr 16 and 255).toFloat() / 255.0f
                val green = (currentColor.getInterpolateColor().rgb shr 8 and 255).toFloat() / 255.0f
                val blue = (currentColor.getInterpolateColor().rgb and 255).toFloat() / 255.0f

                val pos = circle.pos()
                val x: Double =
                    cos(Math.toRadians(i.toDouble())) *
                        createAnimation(
                            1.0 -
                                circle.getAnimation(
                                    ((mc as IMixinMinecraft).timer as net.minecraft.util.Timer).renderPartialTicks,
                                ),
                        ) *
                        0.7
                val z: Double =
                    sin(Math.toRadians(i.toDouble())) *
                        createAnimation(
                            1.0 -
                                circle.getAnimation(
                                    ((mc as IMixinMinecraft).timer as net.minecraft.util.Timer).renderPartialTicks,
                                ),
                        ) *
                        0.7
                GL11.glColor4d(
                    red.toDouble(),
                    green.toDouble(),
                    blue.toDouble(),
                    0.6 *
                        circle.getAnimation(
                            ((mc as IMixinMinecraft).timer as net.minecraft.util.Timer).renderPartialTicks,
                        ),
                )
                GL11.glVertex3d(pos.xCoord + x, pos.yCoord + 0.2, pos.zCoord + z)
                GL11.glColor4d(
                    red.toDouble(),
                    green.toDouble(),
                    blue.toDouble(),
                    0.2 *
                        circle.getAnimation(
                            ((mc as IMixinMinecraft).timer as net.minecraft.util.Timer).renderPartialTicks,
                        ),
                )
                GL11.glVertex3d(pos.xCoord + x * 1.4, pos.yCoord + 0.2, pos.zCoord + z * 1.4)
                i += 5
            }
            GL11.glEnd()
        }

        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glEnable(3008)
        GL11.glShadeModel(7424)
        GL11.glEnable(2884)
        GL11.glPopMatrix()
        GlStateManager.resetColor()
    }

    private class JumpCircle(
        private val vector: Vec3,
    ) {
        private val mc: Minecraft = Minecraft.getMinecraft()

        private var tick = 20
        private var prevTick = 20

        fun getAnimation(pt: Float): Double =
            (
                (this.prevTick.toFloat() + (this.tick - this.prevTick).toFloat() * pt) /
                    20.0f
            ).toDouble()

        fun update(): Boolean {
            this.prevTick = this.tick
            return this.tick-- <= 0
        }

        fun pos(): Vec3 =
            Vec3(
                this.vector.xCoord - (mc.renderManager as IMixinRenderManager).renderPosX,
                this.vector.yCoord - (mc.renderManager as IMixinRenderManager).renderPosY,
                this.vector.zCoord - (mc.renderManager as IMixinRenderManager).renderPosZ,
            )
    }

    companion object {
        private fun createAnimation(value: Double): Double = sqrt(1.0 - (value - 1.0).pow(2.0))
    }
}
