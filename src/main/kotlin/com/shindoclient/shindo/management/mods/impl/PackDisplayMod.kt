package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.injection.interfaces.IMixinMinecraft
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.event.impl.EventSwitchTexture
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.HUDMod
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.utils.ColorUtils.removeColorCode
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.resources.IResourcePack
import net.minecraft.client.resources.ResourcePackRepository
import net.minecraft.util.ResourceLocation
import java.io.IOException

class PackDisplayMod : HUDMod(TranslateText.PACK_DISPLAY, TranslateText.PACK_DISPLAY_DESCRIPTION, Shinconic.MOD_PACK_DISPLAY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.COMPACT)
    private val compactSetting = false

    private val resourcePackRepository: ResourcePackRepository = mc.resourcePackRepository

    private var pack: IResourcePack? = null
    private var currentPack: ResourceLocation? = null
    private var packs: MutableList<ResourcePackRepository.Entry?> = resourcePackRepository.repositoryEntries

    override fun onEnable() {
        super.onEnable()
        this.loadTexture()
    }

    @EventTarget
    fun onRender2D(event: EventNVG) {
        if (pack == null) {
            pack = this.getCurrentPack()
        }

        this.drawNanoVG()
    }

    private fun drawNanoVG() {
        val name = removeColorCode(pack!!.packName).replace(".zip", "")

        val stringWidth: Float = this.getTextWidth(name, 9f, getHudFont(1))!!
        val compact = compactSetting
        val imgSize = if (compact) 12 else 30
        val imgX = if (compact) 5f else 4.5f
        val imgY = if (compact) 3f else 4.5f
        val textX = if (compact) 20.5f else 38f
        val textY = if (compact) 5.5f else 7f

        this.drawBackground((if (compact) 24 else 44) + stringWidth, (if (compact) 18 else 39).toFloat())

        this.drawRoundedImage(
            mc.textureManager.getTexture(currentPack).glTextureId,
            imgX,
            imgY,
            imgSize.toFloat(),
            imgSize.toFloat(),
            (if (compact) 2 else 4).toFloat(),
        )
        this.drawText(name, textX, textY, 9f, getHudFont(1))

        this.setWidth(((if (compact) 24 else 44) + stringWidth).toInt())
        this.setHeight(if (compact) 18 else 39)
    }

    @EventTarget
    fun onSwitchTexture(event: EventSwitchTexture?) {
        packs = resourcePackRepository.repositoryEntries
        pack = this.getCurrentPack()
        this.loadTexture()
    }

    private fun loadTexture() {
        val dynamicTexture: DynamicTexture? =
            try {
                DynamicTexture(getCurrentPack()!!.packImage)
            } catch (e: Exception) {
                try {
                    DynamicTexture(
                        ((mc as IMixinMinecraft).mcDefaultResourcePack as net.minecraft.client.resources.DefaultResourcePack).packImage,
                    )
                } catch (e1: IOException) {
                    TextureUtil.missingTexture
                }
            }

        this.currentPack = mc.textureManager.getDynamicTextureLocation("texturepackicon", dynamicTexture)
    }

    private fun getCurrentPack(): IResourcePack? {
        if (packs.isNotEmpty()) {
            return packs[packs.size - 1]!!.resourcePack
        }
        return (mc as IMixinMinecraft).mcDefaultResourcePack as net.minecraft.client.resources.DefaultResourcePack
    }
}
