package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender3D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.floor

class ChunkBordersMod : Mod(
    TranslateText.CHUNK_BORDERS,
    TranslateText.CHUNK_BORDERS_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_CHUNK_BORDERS
) {
    @EventTarget
    fun onRender3D(event: EventRender3D) {
        val entity = Minecraft.getMinecraft().thePlayer
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        val frame = event.partialTicks
        val inChunkPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * frame.toDouble()
        val inChunkPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * frame.toDouble()
        val inChunkPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * frame.toDouble()

        var x = 0.0
        var z = 0.0

        val color = Color.BLUE
        val color2 = Color.YELLOW

        var eyeHeightBlock: Int

        GL11.glPushMatrix()

        GL11.glTranslated(-inChunkPosX, -inChunkPosY, -inChunkPosZ)
        GL11.glDisable(3553)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glLineWidth(1f)
        worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR)
        GL11.glTranslatef((entity.chunkCoordX * 16).toFloat(), 0.0f, (entity.chunkCoordZ * 16).toFloat())

        for (eyeHeight in -2..2) {
            eyeHeightBlock = -2
            while (eyeHeightBlock <= 2) {
                if (abs(eyeHeightBlock) != 2 || abs(eyeHeight) != 2) {
                    x = (eyeHeightBlock * 16).toDouble()
                    z = (eyeHeight * 16).toDouble()

                    worldRenderer.pos(x, 0.0, z)
                        .color(color.red, color.green, color.blue, color.alpha).endVertex()
                    worldRenderer.pos(x, 256.0, z)
                        .color(color.red, color.green, color.blue, color.alpha).endVertex()
                    worldRenderer.pos(x + 16.0, 0.0, z)
                        .color(color.red, color.green, color.blue, color.alpha).endVertex()
                    worldRenderer.pos(x + 16.0, 256.0, z)
                        .color(color.red, color.green, color.blue, color.alpha).endVertex()
                    worldRenderer.pos(x, 0.0, z + 16.0)
                        .color(color.red, color.green, color.blue, color.alpha).endVertex()
                    worldRenderer.pos(x, 256.0, z + 16.0)
                        .color(color.red, color.green, color.blue, color.alpha).endVertex()
                    worldRenderer.pos(x + 16.0, 0.0, z + 16.0)
                        .color(color.red, color.green, color.blue, color.alpha).endVertex()
                    worldRenderer.pos(x + 16.0, 256.0, z + 16.0)
                        .color(color.red, color.green, color.blue, color.alpha).endVertex()
                }
                ++eyeHeightBlock
            }
        }

        z = 0.0
        x = 0.0

        val f = (entity.eyeHeight.toDouble() + entity.posY).toFloat()

        eyeHeightBlock = floor(f.toDouble()).toInt()

        for (y in 0..256) {
            if (y < 256) {
                for (n in 1..15) {
                    worldRenderer.pos(n.toDouble(), y.toDouble(), z)
                        .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
                    worldRenderer.pos(n.toDouble(), (y + 1).toDouble(), z)
                        .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
                    worldRenderer.pos(x, y.toDouble(), n.toDouble())
                        .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
                    worldRenderer.pos(x, (y + 1).toDouble(), n.toDouble())
                        .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
                    worldRenderer.pos(n.toDouble(), y.toDouble(), z + 16.0)
                        .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
                    worldRenderer.pos(n.toDouble(), (y + 1).toDouble(), z + 16.0)
                        .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
                    worldRenderer.pos(x + 16.0, y.toDouble(), n.toDouble())
                        .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
                    worldRenderer.pos(x + 16.0, (y + 1).toDouble(), n.toDouble())
                        .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
                }
            }

            worldRenderer.pos(0.0, y.toDouble(), 0.0)
                .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
            worldRenderer.pos(16.0, y.toDouble(), 0.0)
                .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
            worldRenderer.pos(0.0, y.toDouble(), 0.0)
                .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
            worldRenderer.pos(0.0, y.toDouble(), 16.0)
                .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
            worldRenderer.pos(16.0, y.toDouble(), 0.0)
                .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
            worldRenderer.pos(16.0, y.toDouble(), 16.0)
                .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
            worldRenderer.pos(0.0, y.toDouble(), 16.0)
                .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
            worldRenderer.pos(16.0, y.toDouble(), 16.0)
                .color(color2.red, color2.green, color2.blue, color2.alpha).endVertex()
        }

        tessellator.draw()
        GL11.glPopMatrix()
        GL11.glEnable(3553)
        GL11.glDisable(3042)
    }
}




