package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
public class NameDisplayMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private boolean iconSetting = true;

    @Property(type = PropertyType.COMBO, translate = TranslateText.PREFIX)
    private Prefix prefix = Prefix.NAME;

    public NameDisplayMod() {
        super(TranslateText.NAME_DISPLAY, TranslateText.NAME_DISPLAY_DESCRIPTION);
    }

    @Override
    public String getText() {

        String label;

        switch (prefix) {
            case NAME:
                label = "Name";
                break;
            case IGN:
                label = "Ign";
                break;
            default:
                label = "Name";
        }

        return label + ": " + mc.thePlayer.getGameProfile().getName();
    }

    @Override
    public String getIcon() {
        return iconSetting ? LegacyIcon.USER : null;
    }

    private enum Prefix implements PropertyEnum {
        NAME(TranslateText.NAME),
        IGN(TranslateText.IGN);

        private final TranslateText translate;

        Prefix(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
