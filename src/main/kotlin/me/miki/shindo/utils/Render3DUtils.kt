package me.miki.shindo.utils

import me.miki.shindo.injection.interfaces.IMixinMinecraft
import me.miki.shindo.injection.interfaces.IMixinRenderManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.ConcurrentModificationException
import kotlin.math.cos
import kotlin.math.sin

object Render3DUtils {

    private val mc: Minecraft = Minecraft.getMinecraft()

    @JvmStatic
    fun drawFillBox(box: AxisAlignedBB) {
        GlStateManager.disableCull()
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer

        worldrenderer.begin(6, DefaultVertexFormats.POSITION)
        worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex()
        worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex()
        worldrenderer.pos(box.maxX, box.maxY, box.minZ).endVertex()
        worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex()
        worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex()
        tessellator.draw()

        worldrenderer.begin(6, DefaultVertexFormats.POSITION)
        worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex()
        worldrenderer.pos(box.maxX, box.minY, box.maxZ).endVertex()
        worldrenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex()
        worldrenderer.pos(box.maxX, box.maxY, box.minZ).endVertex()
        worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex()
        tessellator.draw()

        worldrenderer.begin(6, DefaultVertexFormats.POSITION)
        worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex()
        worldrenderer.pos(box.maxX, box.minY, box.maxZ).endVertex()
        worldrenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex()
        worldrenderer.pos(box.minX, box.maxY, box.maxZ).endVertex()
        worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex()
        tessellator.draw()

        worldrenderer.begin(6, DefaultVertexFormats.POSITION)
        worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex()
        worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex()
        worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex()
        worldrenderer.pos(box.minX, box.maxY, box.maxZ).endVertex()
        worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex()
        tessellator.draw()

        worldrenderer.begin(6, DefaultVertexFormats.POSITION)
        worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex()
        worldrenderer.pos(box.maxX, box.maxY, box.minZ).endVertex()
        worldrenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex()
        worldrenderer.pos(box.minX, box.maxY, box.maxZ).endVertex()
        worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex()
        tessellator.draw()

        worldrenderer.begin(6, DefaultVertexFormats.POSITION)
        worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex()
        worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex()
        worldrenderer.pos(box.maxX, box.minY, box.maxZ).endVertex()
        worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex()
        worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex()
        tessellator.draw()

        GlStateManager.enableCull()
    }

    @JvmStatic
    fun drawBoundingBox(aa: AxisAlignedBB) {
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ))
        GL11.glEnd()

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ))
        GL11.glEnd()

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ))
        GL11.glEnd()

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ))
        GL11.glEnd()

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ))
        GL11.glEnd()

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ))
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ))
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ))
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ))
        GL11.glEnd()
    }

    private fun glVertex3D(vector3d: Vec3) {
        GL11.glVertex3d(vector3d.xCoord, vector3d.yCoord, vector3d.zCoord)
    }

    private fun getRenderPos(x: Double, y: Double, z: Double): Vec3 {
        var rx = x
        var ry = y
        var rz = z
        val renderManager = mc.renderManager as IMixinRenderManager
        rx -= renderManager.renderPosX
        ry -= renderManager.renderPosY
        rz -= renderManager.renderPosZ
        return Vec3(rx, ry, rz)
    }

    @JvmStatic
    fun drawTargetIndicator(entity: Entity, rad: Double, color: Color) {
        GL11.glPushMatrix()
        GL11.glDisable(3553)
        GL11.glEnable(2848)
        GL11.glEnable(2832)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glHint(3154, 4354)
        GL11.glHint(3155, 4354)
        GL11.glHint(3153, 4354)
        GL11.glDepthMask(false)
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GlStateManager.disableCull()
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)

        val mixinMc = mc as IMixinMinecraft
        val renderManager = mc.renderManager as IMixinRenderManager

        val x =
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mixinMc.timer.renderPartialTicks - renderManager.renderPosX
        val y =
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mixinMc.timer.renderPartialTicks - renderManager.renderPosY + sin(
                System.currentTimeMillis() / 2e2
            ) + 1
        val z =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mixinMc.timer.renderPartialTicks - renderManager.renderPosZ

        var angle = 0f
        val increment = (Math.PI * 2 / 64.0).toFloat()
        while (angle < (Math.PI * 2).toFloat()) {
            val vecX = x + rad * cos(angle.toDouble())
            val vecZ = z + rad * sin(angle.toDouble())

            GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, 0f)
            GL11.glVertex3d(vecX, y - cos(System.currentTimeMillis() / 2e2) / 2.0f, vecZ)
            GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, 0.40f)
            GL11.glVertex3d(vecX, y, vecZ)
            angle += increment
        }

        GL11.glEnd()
        GL11.glShadeModel(GL11.GL_FLAT)
        GL11.glDepthMask(true)
        GL11.glEnable(2929)
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f)
        GlStateManager.enableCull()
        GL11.glDisable(2848)
        GL11.glDisable(2848)
        GL11.glEnable(2832)
        GL11.glEnable(3553)
        GL11.glPopMatrix()
        GL11.glColor3f(255f, 255f, 255f)
    }

    private fun drawFilledCircleNoGL(x: Int, y: Int, r: Double, c: Int, quality: Int) {
        val f = (c shr 24 and 0xff) / 255f
        val f1 = (c shr 16 and 0xff) / 255f
        val f2 = (c shr 8 and 0xff) / 255f
        val f3 = (c and 0xff) / 255f

        GL11.glColor4f(f1, f2, f3, f)
        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        for (i in 0..360 / quality) {
            val x2 = sin(i * quality * Math.PI / 180) * r
            val y2 = cos(i * quality * Math.PI / 180) * r
            GL11.glVertex2d(x + x2, y + y2)
        }
        GL11.glEnd()
    }

    @JvmStatic
    fun renderBreadCrumbs(vec3s: List<Vec3>, color: Color) {
        GlStateManager.disableDepth()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        var i = 0
        try {
            for (v in vec3s) {
                i++
                var draw = true

                val renderManager = mc.renderManager as IMixinRenderManager
                val x = v.xCoord - renderManager.renderPosX
                val y = v.yCoord - renderManager.renderPosY
                val z = v.zCoord - renderManager.renderPosZ

                val distanceFromPlayer = mc.thePlayer.getDistance(v.xCoord, v.yCoord - 1, v.zCoord)
                var quality = (distanceFromPlayer * 4 + 10).toInt()

                if (quality > 350) quality = 350

                if (i % 10 != 0 && distanceFromPlayer > 25) {
                    draw = false
                }

                if (i % 3 == 0 && distanceFromPlayer > 15) {
                    draw = false
                }

                if (draw) {
                    GL11.glPushMatrix()
                    GL11.glTranslated(x, y, z)

                    val scale = 0.04f
                    GL11.glScalef(-scale, -scale, -scale)

                    GL11.glRotated(-mc.renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
                    GL11.glRotated(mc.renderManager.playerViewX.toDouble(), 1.0, 0.0, 0.0)

                    drawFilledCircleNoGL(0, 0, 0.7, Color(color.red, color.green, color.blue, 100).hashCode(), quality)

                    if (distanceFromPlayer < 4) {
                        drawFilledCircleNoGL(
                            0,
                            0,
                            1.4,
                            Color(color.red, color.green, color.blue, 50).hashCode(),
                            quality
                        )
                    }

                    if (distanceFromPlayer < 20) {
                        drawFilledCircleNoGL(
                            0,
                            0,
                            2.3,
                            Color(color.red, color.green, color.blue, 30).hashCode(),
                            quality
                        )
                    }

                    GL11.glScalef(0.8f, 0.8f, 0.8f)

                    GL11.glPopMatrix()
                }
            }
        } catch (_: ConcurrentModificationException) {
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.enableDepth()

        GL11.glColor3d(255.0, 255.0, 255.0)
    }
}
