package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventHitOverlay;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.ColorSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

import java.awt.*;

public class HitColorMod extends Mod {

    private final BooleanSetting customColorSetting = new BooleanSetting(TranslateText.CUSTOM_COLOR, this, false);
    private final ColorSetting colorSetting = new ColorSetting(TranslateText.COLOR, this, new Color(255, 0, 0), false);
    private final NumberSetting alphaSetting = new NumberSetting(TranslateText.ALPHA, this, 0.45, 0, 1.0, false);

    public HitColorMod() {
        super(TranslateText.HIT_COLOR, TranslateText.HIT_COLOR_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onHitOverlay(EventHitOverlay event) {

        AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();
        Color lastColor = customColorSetting.isToggled() ? colorSetting.getColor() : currentColor.getInterpolateColor();

        event.setRed(lastColor.getRed() / 255F);
        event.setGreen(lastColor.getGreen() / 255F);
        event.setBlue(lastColor.getBlue() / 255F);
        event.setAlpha(alphaSetting.getValueFloat());
    }
}
