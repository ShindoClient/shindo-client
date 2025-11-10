package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class WeatherDisplayMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private boolean iconEnabled = true;

    public WeatherDisplayMod() {
        super(TranslateText.WEATHER_DISPLAY, TranslateText.WEATHER_DISPLAY_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @Override
    public String getText() {

        String biome = "";
        String prefix = "Weather: ";
        Chunk chunk = mc.theWorld.getChunkFromBlockCoords(new BlockPos(mc.thePlayer));
        biome = chunk.getBiome(new BlockPos(mc.thePlayer), this.mc.theWorld.getWorldChunkManager()).biomeName;

        if (mc.theWorld.isRaining()) {
            if (biome.contains("Extreme Hills") && mc.thePlayer.posY > 100) {
                return prefix + "Snowing";
            } else {
                return prefix + "Raining";
            }
        }

        if (mc.theWorld.isThundering()) {
            return prefix + "Thundering";
        }

        return prefix + "Cleaning";
    }

    @Override
    public String getIcon() {

        String biome = "";
        Chunk chunk = mc.theWorld.getChunkFromBlockCoords(new BlockPos(mc.thePlayer));
        biome = chunk.getBiome(new BlockPos(mc.thePlayer), this.mc.theWorld.getWorldChunkManager()).biomeName;

        String iconFont = LegacyIcon.SUN;

        if (mc.theWorld.isRaining()) {
            if (biome.contains("Extreme Hills") && mc.thePlayer.posY > 100) {
                iconFont = LegacyIcon.CLOUD_SNOW;
            } else {
                iconFont = LegacyIcon.CLOUD_RAIN;
            }
        }

        if (mc.theWorld.isThundering()) {
            iconFont = LegacyIcon.CLOUD_LIGHTING;
        }

        return iconEnabled ? iconFont : null;
    }
}
