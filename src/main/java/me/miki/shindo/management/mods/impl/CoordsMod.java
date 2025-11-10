package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class CoordsMod extends SimpleHUDMod {

    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private Design design = Design.SIMPLE;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON, category = "Display")
    private boolean iconSetting = true;

    public CoordsMod() {
        super(TranslateText.COORDS, TranslateText.COORDS_DEDSCRIPTION, "coordinates");
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        if (design == Design.SIMPLE) {
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
        return iconSetting ? LegacyIcon.MAP_PIN : null;
    }

    private enum Design implements PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        FANCY(TranslateText.FANCY);

        private final TranslateText translate;

        Design(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
