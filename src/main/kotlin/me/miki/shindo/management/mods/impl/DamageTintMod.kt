package me.miki.shindo.management.mods.impl

import me.miki.extensions.ui.animation.setAnimation
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRenderDamageTint
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.PlayerUtils.isCreative
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation

class DamageTintMod :
    Mod(
        TranslateText.DAMAGE_TINT,
        TranslateText.DAMAGE_TINT_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_DAMAGE_TINT,
    ) {
    private val shape = ResourceLocation("shindo/shape.png")

    private val animation1 = SimpleAnimation(0.0f)

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.HEALTH,
        min = 5.0,
        max = 16.0,
        step = 1.0,
        current = 10.0,
    )
    private val healthSetting = 10.0

    @EventTarget
    fun onRenderDamageTint(event: EventRenderDamageTint?) {
        val threshold = healthSetting.toFloat()

        val sr = ScaledResolution(mc)

        if (isCreative() || mc.thePlayer.isSpectator) {
            return
        }

        animation1.setAnimation(if (mc.thePlayer.health <= threshold) 1.0f else 0.0f, 10)

        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.tryBlendFuncSeparate(0, 769, 1, 0)

        GlStateManager.color(0f, animation1.getValue(), animation1.getValue(), animation1.getValue())
        mc.textureManager.bindTexture(shape)
        val tes = Tessellator.getInstance()
        val wr = tes.worldRenderer

        wr.begin(7, DefaultVertexFormats.POSITION_TEX)
        wr.pos(0.0, sr.scaledHeight_double, -90.0).tex(0.0, 1.0).endVertex()
        wr.pos(sr.scaledWidth_double, sr.scaledHeight_double, -90.0).tex(1.0, 1.0).endVertex()
        wr.pos(sr.scaledWidth_double, 0.0, -90.0).tex(1.0, 0.0).endVertex()
        wr.pos(0.0, 0.0, -90.0).tex(0.0, 0.0).endVertex()
        tes.draw()

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }
}
