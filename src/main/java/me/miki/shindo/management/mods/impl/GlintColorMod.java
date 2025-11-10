package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.utils.ColorUtils;

import java.awt.*;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
public class GlintColorMod extends Mod {

    private static GlintColorMod instance;

    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    private GlintType glintType = GlintType.SYNC;

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR)
    private Color colorSetting = Color.RED;

    public GlintColorMod() {
        super(TranslateText.GLINT_COLOR, TranslateText.GLINT_COLOR_DESCRIPTION, ModCategory.RENDER, "changeru");

        instance = this;
    }

    public static GlintColorMod getInstance() {
        return instance;
    }

    public Color getGlintColor() {

        switch (glintType) {
            case SYNC: {
                AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();
                return currentColor.getInterpolateColor();
            }
            case RAINBOW:
                return ColorUtils.getRainbow(0, 25, 255);
            case CUSTOM:
                return ColorUtils.applyAlpha(colorSetting, 255);
            default:
                return Color.RED;
        }
    }

    private enum GlintType implements PropertyEnum {
        SYNC(TranslateText.SYNC),
        RAINBOW(TranslateText.RAINBOW),
        CUSTOM(TranslateText.CUSTOM);

        private final TranslateText translate;

        GlintType(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
