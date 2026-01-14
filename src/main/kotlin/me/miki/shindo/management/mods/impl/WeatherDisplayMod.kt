package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.util.BlockPos

class WeatherDisplayMod : SimpleHUDMod(
    TranslateText.WEATHER_DISPLAY,
    TranslateText.WEATHER_DISPLAY_DESCRIPTION,
    LegacyIcon.MOD_WEATHER_DISPLAY
) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconEnabled = true

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        this.draw()
    }

    public override fun getText(): String? {
        var biome = ""
        val prefix = "Weather: "
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(mc.thePlayer))
        biome = chunk.getBiome(BlockPos(mc.thePlayer), this.mc.theWorld.getWorldChunkManager()).biomeName

        if (mc.theWorld.isRaining()) {
            if (biome.contains("Extreme Hills") && mc.thePlayer.posY > 100) {
                return prefix + "Snowing"
            } else {
                return prefix + "Raining"
            }
        }

        if (mc.theWorld.isThundering()) {
            return prefix + "Thundering"
        }

        return prefix + "Cleaning"
    }

    public override fun getIcon(): String? {
        var biome = ""
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(mc.thePlayer))
        biome = chunk.getBiome(BlockPos(mc.thePlayer), this.mc.theWorld.getWorldChunkManager()).biomeName

        var iconFont = LegacyIcon.SUN

        if (mc.theWorld.isRaining()) {
            if (biome.contains("Extreme Hills") && mc.thePlayer.posY > 100) {
                iconFont = LegacyIcon.CLOUD_SNOW
            } else {
                iconFont = LegacyIcon.CLOUD_RAIN
            }
        }

        if (mc.theWorld.isThundering()) {
            iconFont = LegacyIcon.CLOUD_LIGHTING
        }

        return if (iconEnabled) iconFont else null
    }
}


