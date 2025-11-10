package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventHitOverlay;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;

import java.awt.*;

public class HitColorMod extends Mod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_COLOR, category = "Customization")
    private boolean customColorSetting;
    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR, category = "Customization", color = 0xFFFF0000)
    private Color colorSetting = new Color(255, 0, 0);
    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, category = "Appearance", min = 0, max = 1, current = 0.45)
    private double alphaSetting = 0.45;

    public HitColorMod() {
        super(TranslateText.HIT_COLOR, TranslateText.HIT_COLOR_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onHitOverlay(EventHitOverlay event) {

        AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();
        Color lastColor = customColorSetting ? colorSetting : currentColor.getInterpolateColor();

        event.setRed(lastColor.getRed() / 255F);
        event.setGreen(lastColor.getGreen() / 255F);
        event.setBlue(lastColor.getBlue() / 255F);
        event.setAlpha((float) alphaSetting);
    }
}
