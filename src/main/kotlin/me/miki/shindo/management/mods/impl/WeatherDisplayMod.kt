package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.util.BlockPos

class WeatherDisplayMod :
    SimpleHUDMod(
        TranslateText.WEATHER_DISPLAY,
        TranslateText.WEATHER_DISPLAY_DESCRIPTION,
        Shinconic.MOD_WEATHER_DISPLAY,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconEnabled = true

    @EventTarget
    fun onRender2D(event: EventNVG) {
        this.draw()
    }

    override fun getText(): String {
        var biome = ""
        val prefix = "Weather: "
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(mc.thePlayer))
        biome = chunk.getBiome(BlockPos(mc.thePlayer), this.mc.theWorld.worldChunkManager).biomeName

        if (mc.theWorld.isRaining) {
            return if (biome.contains("Extreme Hills") && mc.thePlayer.posY > 100) {
                prefix + "Snowing"
            } else {
                prefix + "Raining"
            }
        }

        if (mc.theWorld.isThundering) {
            return prefix + "Thundering"
        }

        return prefix + "Cleaning"
    }

    override fun getIcon(): String? {
        var biome = ""
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(mc.thePlayer))
        biome = chunk.getBiome(BlockPos(mc.thePlayer), this.mc.theWorld.worldChunkManager).biomeName

        var iconFont = Lucide.SUN

        if (mc.theWorld.isRaining) {
            iconFont =
                if (biome.contains("Extreme Hills") && mc.thePlayer.posY > 100) {
                    Lucide.CLOUD_SNOW
                } else {
                    Lucide.CLOUD_RAIN
                }
        }

        if (mc.theWorld.isThundering) {
            iconFont = Lucide.CLOUD_LIGHTNING
        }

        return if (iconEnabled) iconFont else null
    }
}
