package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.injection.mixin.interfaces.client.IMixinMinecraft
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventSwitchTexture
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ColorUtils.removeColorCode
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.resources.IResourcePack
import net.minecraft.client.resources.ResourcePackRepository
import net.minecraft.util.ResourceLocation
import java.io.IOException

class PackDisplayMod :
    HUDMod(TranslateText.PACK_DISPLAY, TranslateText.PACK_DISPLAY_DESCRIPTION, LegacyIcon.MOD_PACK_DISPLAY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.COMPACT)
    private val compactSetting = false

    private val resourcePackRepository: ResourcePackRepository = mc.getResourcePackRepository()

    private var pack: IResourcePack? = null
    private var currentPack: ResourceLocation? = null
    private var packs: MutableList<ResourcePackRepository.Entry?> = resourcePackRepository.getRepositoryEntries()

    public override fun onEnable() {
        super.onEnable()
        this.loadTexture()
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val instance = getInstance()
        val nvg = instance.nanoVGManager

        if (pack == null) {
            pack = this.getCurrentPack()
        }

        nvg!!.setupAndDraw(Runnable { this.drawNanoVG() })
    }

    private fun drawNanoVG() {
        val name = removeColorCode(pack!!.getPackName()).replace(".zip", "")

        val stringWidth: Float = this.getTextWidth(name, 9f, getHudFont(1))!!
        val compact = compactSetting
        val imgSize = if (compact) 12 else 30
        val imgX = if (compact) 5f else 4.5f
        val imgY = if (compact) 3f else 4.5f
        val textX = if (compact) 20.5f else 38f
        val textY = if (compact) 5.5f else 7f

        this.drawBackground((if (compact) 24 else 44) + stringWidth, (if (compact) 18 else 39).toFloat())

        this.drawRoundedImage(
            mc.getTextureManager().getTexture(currentPack).getGlTextureId(),
            imgX,
            imgY,
            imgSize.toFloat(),
            imgSize.toFloat(),
            (if (compact) 2 else 4).toFloat()
        )
        this.drawText(name, textX, textY, 9f, getHudFont(1))

        this.setWidth(((if (compact) 24 else 44) + stringWidth).toInt())
        this.setHeight(if (compact) 18 else 39)
    }

    @EventTarget
    fun onSwitchTexture(event: EventSwitchTexture?) {
        packs = resourcePackRepository.getRepositoryEntries()
        pack = this.getCurrentPack()
        this.loadTexture()
    }

    private fun loadTexture() {
        var dynamicTexture: DynamicTexture?
        try {
            dynamicTexture = DynamicTexture(getCurrentPack()!!.getPackImage())
        } catch (e: Exception) {
            try {
                dynamicTexture = DynamicTexture((mc as IMixinMinecraft).getMcDefaultResourcePack().getPackImage())
            } catch (e1: IOException) {
                dynamicTexture = TextureUtil.missingTexture
            }
        }

        this.currentPack = mc.getTextureManager().getDynamicTextureLocation("texturepackicon", dynamicTexture)
    }

    private fun getCurrentPack(): IResourcePack? {
        if (!packs.isEmpty()) {
            return packs.get(packs.size - 1)!!.getResourcePack()
        }
        return (mc as IMixinMinecraft).getMcDefaultResourcePack()
    }
}




