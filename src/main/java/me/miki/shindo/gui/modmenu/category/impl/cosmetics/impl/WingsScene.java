package me.miki.shindo.gui.modmenu.category.impl.cosmetics.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.category.impl.CosmeticsCategory;
import me.miki.shindo.gui.modmenu.category.impl.cosmetics.CosmeticScene;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;

public class WingsScene extends CosmeticScene {


    public WingsScene(CosmeticsCategory parent) {
        super(parent, "Wings", "Customize your wings", "");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        AccentColor accentColor = colorManager.getCurrentColor();
        ColorPalette palette = colorManager.getPalette();

        nvg.drawCenteredText("UNDER CONSTRUCTION", getX()+(getWidth()/2F), getY() + (getHeight()/2F) - 14, palette.getFontColor(ColorType.DARK), 12, Fonts.SEMIBOLD);
        nvg.drawCenteredText("This page is not available yet.", getX()+(getWidth()/2F), getY() + (getHeight()/2F), palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);
    }
}
