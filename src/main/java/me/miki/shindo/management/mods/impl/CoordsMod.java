package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.ComboSetting;
import me.miki.shindo.management.mods.settings.impl.combo.Option;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Arrays;

public class CoordsMod extends SimpleHUDMod {

    private final ComboSetting designSetting = new ComboSetting(TranslateText.DESIGN, this, TranslateText.SIMPLE, new ArrayList<Option>(Arrays.asList(
            new Option(TranslateText.SIMPLE), new Option(TranslateText.FANCY))));
    private final BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);

    public CoordsMod() {
        super(TranslateText.COORDS, TranslateText.COORDS_DEDSCRIPTION, "coordinates");
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        Option design = designSetting.getOption();
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        if (design.getTranslate().equals(TranslateText.SIMPLE)) {
            this.draw();
        } else {
            nvg.setupAndDraw(() -> drawNanoVG());
        }
    }

    private void drawNanoVG() {

        String biome = "";
        Chunk chunk = mc.theWorld.getChunkFromBlockCoords(new BlockPos(mc.thePlayer));
        int maxWidth = 100;
        biome = chunk.getBiome(new BlockPos(mc.thePlayer), this.mc.theWorld.getWorldChunkManager()).biomeName;

        if (maxWidth < (this.getTextWidth("Biome: " + biome, 9, getHudFont(1)))) {
            maxWidth = (int) (this.getTextWidth("Biome: " + biome, 9, getHudFont(1)) + 12);
        } else {
            maxWidth = 107;
        }

        this.drawBackground(maxWidth, 48);
        this.drawText("X: " + (int) mc.thePlayer.posX, 5.5F, 5.5F, 9, getHudFont(1));
        this.drawText("Y: " + (int) mc.thePlayer.posY, 5.5F, 15.5F, 9, getHudFont(1));
        this.drawText("Z: " + (int) mc.thePlayer.posZ, 5.5F, 25.5F, 9, getHudFont(1));
        this.drawText("Biome: " + biome, 5.5F, 35.5F, 9, getHudFont(1));

        this.setWidth(maxWidth);
        this.setHeight(48);
    }

    @Override
    public String getText() {
        return "X: " + (int) mc.thePlayer.posX + " Y: " + (int) mc.thePlayer.posY + " Z: " + (int) mc.thePlayer.posZ + " ";
    }

    @Override
    public String getIcon() {
        return iconSetting.isToggled() ? LegacyIcon.MAP_PIN : null;
    }
}
