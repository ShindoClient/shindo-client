package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRenderHitbox
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.awt.Color

class HitBoxMod : Mod(TranslateText.HITBOX, TranslateText.HITBOX_DESCRIPTION, ModCategory.RENDER, Shinconic.MOD_HIT_BOX) {
    private val eyeHeightColor: Color = Color.RED
    private val lookVectorColor: Color = Color.BLUE

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR)
    private val colorSetting = Color(255, 255, 255)

    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0.0, max = 1.0, current = 1.0)
    private val alphaSetting = 1.0

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BOUNDING_BOX)
    private val boundingBoxSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.EYE_HEIGHT)
    private val eyeHeightSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.LOOK_VECTOR)
    private val lookVectorSetting = true

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.LINE_WIDTH,
        min = 1.0,
        max = 5.0,
        current = 2.0,
        step = 1.0,
    )
    private val lineWidthSetting = 2

    @EventTarget
    fun onRenderHitbox(event: EventRenderHitbox) {
        val half = event.getEntity().width / 2.0f

        event.setCancelled(true)

        if (event.getEntity() is EntityArmorStand) {
            return
        }

        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GL11.glLineWidth(lineWidthSetting.toFloat())

        if (boundingBoxSetting) {
            val box = event.getEntity().entityBoundingBox
            val offsetBox =
                AxisAlignedBB(
                    box.minX - event.getEntity().posX + event.getX(),
                    box.minY - event.getEntity().posY + event.getY(),
                    box.minZ - event.getEntity().posZ + event.getZ(),
                    box.maxX - event.getEntity().posX + event.getX(),
                    box.maxY - event.getEntity().posY + event.getY(),
                    box.maxZ - event.getEntity().posZ + event.getZ(),
                )
            val boundingBoxColor = colorSetting
            RenderGlobal.drawOutlinedBoundingBox(
                offsetBox,
                boundingBoxColor.red,
                boundingBoxColor.green,
                boundingBoxColor.blue,
                (alphaSetting * 255).toInt(),
            )
        }

        if (eyeHeightSetting && event.getEntity() is EntityLivingBase) {
            RenderGlobal.drawOutlinedBoundingBox(
                AxisAlignedBB(
                    event.getX() - half,
                    event.getY() + event.getEntity().eyeHeight - 0.009999999776482582,
                    event.getZ() - half,
                    event.getX() + half,
                    event.getY() + event.getEntity().eyeHeight + 0.009999999776482582,
                    event.getZ() + half,
                ),
                eyeHeightColor.red,
                eyeHeightColor.green,
                eyeHeightColor.blue,
                (alphaSetting * 255).toInt(),
            )
        }

        if (lookVectorSetting) {
            val tessellator = Tessellator.getInstance()
            val worldrenderer = tessellator.worldRenderer

            val look = event.getEntity().getLook(event.getPartialTicks())
            worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR)
            worldrenderer
                .pos(event.getX(), event.getY() + event.getEntity().eyeHeight, event.getZ())
                .color(0, 0, 255, 255)
                .endVertex()
            worldrenderer
                .pos(
                    event.getX() + look.xCoord * 2,
                    event.getY() + event.getEntity().eyeHeight + look.yCoord * 2,
                    event.getZ() + look.zCoord * 2,
                ).color(
                    lookVectorColor.red,
                    lookVectorColor.green,
                    lookVectorColor.blue,
                    (alphaSetting * 255).toInt(),
                ).endVertex()
            tessellator.draw()
        }

        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.enableCull()
        GlStateManager.disableBlend()
        GlStateManager.depthMask(true)
    }

    override fun onEnable() {
        super.onEnable()

        if (mc.renderManager != null) {
            mc.renderManager.isDebugBoundingBox = true
        }
    }

    override fun onDisable() {
        super.onDisable()

        if (mc.renderManager != null) {
            mc.renderManager.isDebugBoundingBox = false
        }
    }
}
