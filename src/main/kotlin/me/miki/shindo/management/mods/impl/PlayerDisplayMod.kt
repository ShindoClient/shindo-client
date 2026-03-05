package me.miki.shindo.management.mods.impl

import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper

class PlayerDisplayMod : HUDMod(
    TranslateText.PLAYER_DISPLAY,
    TranslateText.PLAYER_DISPLAY_DESCRIPTION,
    LegacyIcon.MOD_PLAYER_DISPLAY,
    "paperdoll"
) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.YAW_OFFSET,
        min = -90.0,
        max = 120.0,
        current = 0.0,
        step = 1.0
    )
    private val yawOffsetSetting = 0

    @EventTarget
    fun onRender2D(event: EventRender2D) {
        GlStateManager.enableColorMaterial()
        GlStateManager.enableDepth()
        GlStateManager.pushMatrix()
        GlStateManager.translate(this.getX() + (15 * this.getScale()), this.getY() + (58 * this.getScale()), -500.0f)
        GlStateManager.scale(-this.getScale() * 30, this.getScale() * 30, this.getScale() * 30)
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)
        GlStateManager.rotate(mc.thePlayer.rotationYaw + yawOffsetSetting, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f)
        val rendermanager = Minecraft.getMinecraft().renderManager

        rendermanager.isRenderShadow = false
        rendermanager.doRenderEntity(mc.thePlayer, 0.0, 0.0, 0.0, 0.0f, event.getPartialTicks(), true)
        rendermanager.isRenderShadow = true
        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)

        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.enableDepth()

        this.setWidth(30)
        this.setHeight(60)
    }
}




