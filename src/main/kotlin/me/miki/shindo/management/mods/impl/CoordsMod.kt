package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.util.BlockPos

class CoordsMod :
    SimpleHUDMod(TranslateText.COORDS, TranslateText.COORDS_DEDSCRIPTION, LegacyIcon.MOD_COORDS, "coordinates") {
    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private val design = Design.SIMPLE

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager

        if (design == Design.SIMPLE) {
            this.draw()
        } else {
            nvg!!.setupAndDraw(Runnable { drawNanoVG() })
        }
    }

    private fun drawNanoVG() {
        var biome: String? = ""
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(mc.thePlayer))
        var maxWidth = 100
        biome = chunk.getBiome(BlockPos(mc.thePlayer), this.mc.theWorld.worldChunkManager).biomeName

        if (maxWidth < (this.getTextWidth("Biome: " + biome, 9f, getHudFont(1))!!)) {
            maxWidth = (this.getTextWidth("Biome: " + biome, 9f, getHudFont(1))!! + 12).toInt()
        } else {
            maxWidth = 107
        }

        this.drawBackground(maxWidth.toFloat(), 48f)
        this.drawText("X: " + mc.thePlayer.posX.toInt(), 5.5f, 5.5f, 9f, getHudFont(1))
        this.drawText("Y: " + mc.thePlayer.posY.toInt(), 5.5f, 15.5f, 9f, getHudFont(1))
        this.drawText("Z: " + mc.thePlayer.posZ.toInt(), 5.5f, 25.5f, 9f, getHudFont(1))
        this.drawText("Biome: " + biome, 5.5f, 35.5f, 9f, getHudFont(1))

        this.setWidth(maxWidth)
        this.setHeight(48)
    }

    override fun getText(): String {
        return "X: " + mc.thePlayer.posX.toInt() + " Y: " + mc.thePlayer.posY.toInt() + " Z: " + mc.thePlayer.posZ.toInt() + " "
    }

    override fun getIcon(): String? {
        return if (iconSetting) LegacyIcon.MAP_PIN else null
    }

    private enum class Design(private val translate: TranslateText) : PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        FANCY(TranslateText.FANCY);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }
}


