package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.ColorSetting;
import me.miki.shindo.management.mods.settings.impl.ComboSetting;
import me.miki.shindo.management.mods.settings.impl.combo.Option;
import me.miki.shindo.utils.ColorUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GlintColorMod extends Mod {

    private static GlintColorMod instance;

    private final ComboSetting typeSetting = new ComboSetting(TranslateText.TYPE, this, TranslateText.SYNC, new ArrayList<Option>(Arrays.asList(
            new Option(TranslateText.SYNC), new Option(TranslateText.RAINBOW), new Option(TranslateText.CUSTOM))));

    private final ColorSetting colorSetting = new ColorSetting(TranslateText.COLOR, this, Color.RED, false);

    public GlintColorMod() {
        super(TranslateText.GLINT_COLOR, TranslateText.GLINT_COLOR_DESCRIPTION, ModCategory.RENDER, "changeru");

        instance = this;
    }

    public static GlintColorMod getInstance() {
        return instance;
    }

    public Color getGlintColor() {

        Option type = typeSetting.getOption();

        if (type.getTranslate().equals(TranslateText.SYNC)) {

            AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();

            return currentColor.getInterpolateColor();
        } else if (type.getTranslate().equals(TranslateText.RAINBOW)) {
            return ColorUtils.getRainbow(0, 25, 255);
        } else if (type.getTranslate().equals(TranslateText.CUSTOM)) {
            return ColorUtils.applyAlpha(colorSetting.getColor(), 255);
        } else {
            return Color.RED;
        }
    }
}
