package me.miki.shindo.management.mods.impl.skin3d.layers

import me.miki.shindo.injection.mixin.interfaces.client.renderer.entity.IMixinRenderPlayer
import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.mods.impl.Skin3DMod
import me.miki.shindo.management.mods.impl.skin3d.render.CustomizableModelPart
import me.miki.shindo.utils.SkinUtils
import me.miki.shindo.utils.SkinUtils.hasCustomSkin
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.entity.player.EnumPlayerModelParts
import java.util.function.Supplier

class BodyLayerFeatureRenderer(playerRenderer: RenderPlayer) : LayerRenderer<AbstractClientPlayer?> {
    private val thinArms: Boolean
    private val bodyLayers: MutableList<Layer> = ArrayList<Layer>()

    init {
        thinArms = (playerRenderer as IMixinRenderPlayer).hasThinArms()
        bodyLayers.add(
            Layer(
                0,
                false,
                EnumPlayerModelParts.LEFT_PANTS_LEG,
                Shape.LEGS,
                Supplier { playerRenderer.getMainModel().bipedLeftLeg })
        )
        bodyLayers.add(
            Layer(
                1,
                false,
                EnumPlayerModelParts.RIGHT_PANTS_LEG,
                Shape.LEGS,
                Supplier { playerRenderer.getMainModel().bipedRightLeg })
        )
        bodyLayers.add(
            Layer(
                2,
                false,
                EnumPlayerModelParts.LEFT_SLEEVE,
                if (thinArms) Shape.ARMS_SLIM else Shape.ARMS,
                Supplier { playerRenderer.getMainModel().bipedLeftArm })
        )
        bodyLayers.add(
            Layer(
                3,
                true,
                EnumPlayerModelParts.RIGHT_SLEEVE,
                if (thinArms) Shape.ARMS_SLIM else Shape.ARMS,
                Supplier { playerRenderer.getMainModel().bipedRightArm })
        )
        bodyLayers.add(
            Layer(
                4,
                false,
                EnumPlayerModelParts.JACKET,
                Shape.BODY,
                Supplier { playerRenderer.getMainModel().bipedBody })
        )
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

        if (mc.theWorld == null) {
            return
        }

        val skinMod = Skin3DMod.getInstance() ?: return
        if (mc.thePlayer.positionVector
                .squareDistanceTo(player.positionVector) > skinMod.getRenderDistanceLOD()
        ) {
            return
        }

        val settings = player as IMixinEntityPlayer

        if (settings.getSkinLayers() == null && !setupModel(player, settings)) {
            return
        }

        renderLayers(player, settings.getSkinLayers(), deltaTick)
    }

    private fun setupModel(abstractClientPlayerEntity: AbstractClientPlayer, settings: IMixinEntityPlayer): Boolean {
        if (!hasCustomSkin(abstractClientPlayerEntity)) {
            return false
        }

        SkinUtils.setup3dLayers(abstractClientPlayerEntity, settings, thinArms, null)

        return true
    }

    fun renderLayers(
        abstractClientPlayer: AbstractClientPlayer,
        layers: Array<CustomizableModelPart?>?,
        deltaTick: Float
    ) {
        if (layers == null) {
            return
        }

        val skinMod = Skin3DMod.getInstance() ?: return
        val pixelScaling: Float = skinMod.getBaseVoxelSize()
        val heightScaling = 1.035f
        var widthScaling: Float = skinMod.getBaseVoxelSize()

        val redTint = abstractClientPlayer.hurtTime > 0 || abstractClientPlayer.deathTime > 0

        for (layer in bodyLayers) {
            if (abstractClientPlayer.isWearing(layer.modelPart) && !layer.vanillaGetter.get()!!.isHidden) {
                GlStateManager.pushMatrix()

                if (abstractClientPlayer.isSneaking) {
                    GlStateManager.translate(0.0f, 0.2f, 0.0f)
                }

                layer.vanillaGetter.get()!!.postRender(0.0625f)

                if (layer.shape == Shape.ARMS) {
                    layers[layer.layersId]!!.x = 0.998f * 16f
                } else if (layer.shape == Shape.ARMS_SLIM) {
                    layers[layer.layersId]!!.x = 0.499f * 16f
                }

                if (layer.shape == Shape.BODY) {
                    widthScaling = skinMod.getBodyVoxelWidthSize()
                } else {
                    widthScaling = skinMod.getBaseVoxelSize()
                }

                if (layer.mirrored) {
                    layers[layer.layersId]!!.x *= -1f
                }
                GlStateManager.scale(0.0625, 0.0625, 0.0625)
                GlStateManager.scale(widthScaling, heightScaling, pixelScaling)
                layers[layer.layersId]!!.y = layer.shape.yOffsetMagicValue

                layers[layer.layersId]!!.render(redTint)
                GlStateManager.popMatrix()
            }
        }
    }

    override fun shouldCombineTextures(): Boolean {
        return false
    }

    private enum class Shape(val yOffsetMagicValue: Float) {
        HEAD(0f), BODY(0.6f), LEGS(-0.2f), ARMS(0.4f), ARMS_SLIM(0.4f)
    }

    private data class Layer(
        var layersId: Int,
        var mirrored: Boolean,
        var modelPart: EnumPlayerModelParts?,
        var shape: Shape,
        var vanillaGetter: Supplier<ModelRenderer?>
    )

    companion object {
        private val mc: Minecraft = Minecraft.getMinecraft()
    }
}
