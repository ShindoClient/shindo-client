package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.Shindo.Companion.getInstance
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyEnum
import com.shindoclient.shindo.management.settings.config.PropertyType
import net.minecraft.util.BlockPos

class CoordsMod : SimpleHUDMod(TranslateText.COORDS, TranslateText.COORDS_DEDSCRIPTION, Shinconic.MOD_COORDS, "coordinates") {
    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private val design = Design.SIMPLE

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @EventTarget
    fun onRender2D(event: EventNVG?) {
        getInstance().nanoVGManager

        if (design == Design.SIMPLE) {
            this.draw()
        } else {
            drawNanoVG()
        }
    }

    private fun drawNanoVG() {
        var biome: String? = ""
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(mc.thePlayer))
        var maxWidth = 100
        biome = chunk.getBiome(BlockPos(mc.thePlayer), this.mc.theWorld.worldChunkManager).biomeName

        maxWidth =
            if (maxWidth < (this.getTextWidth("Biome: $biome", 9f, getHudFont(1))!!)) {
                (this.getTextWidth("Biome: $biome", 9f, getHudFont(1))!! + 12).toInt()
            } else {
                107
            }

        this.drawBackground(maxWidth.toFloat(), 48f)
        this.drawText("X: " + mc.thePlayer.posX.toInt(), 5.5f, 5.5f, 9f, getHudFont(1))
        this.drawText("Y: " + mc.thePlayer.posY.toInt(), 5.5f, 15.5f, 9f, getHudFont(1))
        this.drawText("Z: " + mc.thePlayer.posZ.toInt(), 5.5f, 25.5f, 9f, getHudFont(1))
        this.drawText("Biome: $biome", 5.5f, 35.5f, 9f, getHudFont(1))

        this.setWidth(maxWidth)
        this.setHeight(48)
    }

    override fun getText(): String =
        "X: " + mc.thePlayer.posX.toInt() + " Y: " + mc.thePlayer.posY.toInt() + " Z: " + mc.thePlayer.posZ.toInt() +
            " "

    override fun getIcon(): String? = if (iconSetting) Lucide.MAP_PIN else null

    private enum class Design(
        private val translate: TranslateText,
    ) : PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        FANCY(TranslateText.FANCY),
        ;

        override fun getTranslate(): TranslateText = translate
    }
}
