package me.miki.shindo.management.mods.impl.skin3d.layers

import com.google.common.collect.Sets
import me.miki.shindo.injection.mixin.interfaces.client.renderer.entity.IMixinRenderPlayer
import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.mods.impl.Skin3DMod
import me.miki.shindo.utils.SkinUtils
import me.miki.shindo.utils.SkinUtils.hasCustomSkin
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.init.Items
import net.minecraft.item.Item

class HeadLayerFeatureRenderer(private val playerRenderer: RenderPlayer) : LayerRenderer<AbstractClientPlayer?> {
    private val thinArms: Boolean
    private val hideHeadLayers: MutableSet<Item?> = Sets.newHashSet<Item?>(Items.skull)

    init {
        thinArms = (playerRenderer as IMixinRenderPlayer).hasThinArms()
    }

    override fun doRenderLayer(
        player: AbstractClientPlayer?,
        paramFloat1: Float,
        paramFloat2: Float,
        paramFloat3: Float,
        deltaTick: Float,
        paramFloat5: Float,
        paramFloat6: Float,
        paramFloat7: Float
    ) {
        if (player == null || !player.hasSkin() || player.isInvisible) {
            return
        }

        val skinMod = Skin3DMod.getInstance() ?: return
        val renderDistance = skinMod.getRenderDistanceLOD()
        if (mc.thePlayer.positionVector
                .squareDistanceTo(player.positionVector) > renderDistance * renderDistance
        ) {
            return
        }

        val itemStack = player.getEquipmentInSlot(1)

        if (itemStack != null && hideHeadLayers.contains(itemStack.item)) {
            return
        }

        val settings = player as IMixinEntityPlayer

        if (settings.getHeadLayers() == null && !setupModel(player, settings)) {
            return
        }

        renderCustomHelmet(settings, player, deltaTick)
    }

    private fun setupModel(abstractClientPlayerEntity: AbstractClientPlayer, settings: IMixinEntityPlayer): Boolean {
        if (!hasCustomSkin(abstractClientPlayerEntity)) {
            return false
        }

        SkinUtils.setup3dLayers(abstractClientPlayerEntity, settings, thinArms, null)

        return true
    }

    fun renderCustomHelmet(settings: IMixinEntityPlayer, abstractClientPlayer: AbstractClientPlayer, deltaTick: Float) {
        if (settings.getHeadLayers() == null) {
            return
        }

        if (playerRenderer.getMainModel().bipedHead.isHidden) {
            return
        }

        val voxelSize: Float = Skin3DMod.getInstance()?.getHeadVoxelSize() ?: return

        GlStateManager.pushMatrix()

        if (abstractClientPlayer.isSneaking) {
            GlStateManager.translate(0.0f, 0.2f, 0.0f)
        }

        playerRenderer.getMainModel().bipedHead.postRender(0.0625f)
        GlStateManager.scale(0.0625, 0.0625, 0.0625)
        GlStateManager.scale(voxelSize, voxelSize, voxelSize)

        val tintRed = abstractClientPlayer.hurtTime > 0 || abstractClientPlayer.deathTime > 0
        settings.getHeadLayers().render(tintRed)
        GlStateManager.popMatrix()
    }

    override fun shouldCombineTextures(): Boolean {
        return false
    }

    companion object {
        private val mc: Minecraft = Minecraft.getMinecraft()
    }
}
