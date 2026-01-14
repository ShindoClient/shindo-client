package me.miki.shindo.utils

import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.mods.impl.skin3d.opengl.NativeImage
import me.miki.shindo.management.mods.impl.skin3d.render.CustomizableModelPart
import me.miki.shindo.management.mods.impl.skin3d.render.SolidPixelWrapper
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.ITextureObject
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.util.ResourceLocation

object SkinUtils {

    @JvmStatic
    fun hasCustomSkin(player: AbstractClientPlayer): Boolean {
        return DefaultPlayerSkin.getDefaultSkin(player.uniqueID) != player.locationSkin
    }

    private fun getSkinTexture(player: AbstractClientPlayer): NativeImage? {
        return getTexture(player.locationSkin)
    }

    private fun getTexture(resource: ResourceLocation): NativeImage? {
        val skin = NativeImage(64, 64, false)
        val textureManager: TextureManager = Minecraft.getMinecraft().textureManager
        val abstractTexture: ITextureObject? = textureManager.getTexture(resource)

        if (abstractTexture == null) {
            return null
        }

        GlStateManager.bindTexture(abstractTexture.glTextureId)
        skin.downloadTexture(0, false)
        return skin
    }

    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun setup3dLayers(
        abstractClientPlayerEntity: AbstractClientPlayer,
        settings: IMixinEntityPlayer,
        thinArms: Boolean,
        model: ModelPlayer?
    ): Boolean {
        if (!hasCustomSkin(abstractClientPlayerEntity)) {
            return false
        }

        val skin = getSkinTexture(abstractClientPlayerEntity) ?: return false

        val layers = arrayOfNulls<CustomizableModelPart>(5)
        layers[0] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 48, true, 0f)
        layers[1] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 32, true, 0f)

        if (thinArms) {
            layers[2] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 48, 48, true, -2.5f)
            layers[3] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 40, 32, true, -2.5f)
        } else {
            layers[2] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 48, 48, true, -2.5f)
            layers[3] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 40, 32, true, -2.5f)
        }

        layers[4] = SolidPixelWrapper.wrapBox(skin, 8, 12, 4, 16, 32, true, -0.8f)
        settings.setupSkinLayers(layers)
        settings.setupHeadLayers(SolidPixelWrapper.wrapBox(skin, 8, 8, 8, 32, 0, false, 0.6f))
        skin.close()

        return true
    }
}

