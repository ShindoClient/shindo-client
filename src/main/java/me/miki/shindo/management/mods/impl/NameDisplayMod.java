package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.ComboSetting;
import me.miki.shindo.management.mods.settings.impl.combo.Option;
import me.miki.shindo.management.nanovg.font.LegacyIcon;

import java.util.ArrayList;
import java.util.Arrays;

public class NameDisplayMod extends SimpleHUDMod {

    private final BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);

    private final ComboSetting prefixSetting = new ComboSetting(TranslateText.PREFIX, this, TranslateText.NAME, new ArrayList<Option>(Arrays.asList(
            new Option(TranslateText.NAME), new Option(TranslateText.IGN))));

    public NameDisplayMod() {
        super(TranslateText.NAME_DISPLAY, TranslateText.NAME_DISPLAY_DESCRIPTION);
    }

    @Override
    public String getText() {

        Option option = prefixSetting.getOption();
        String prefix = "null";

        if (option.getTranslate().equals(TranslateText.NAME)) {
            prefix = "Name";
        }

        if (option.getTranslate().equals(TranslateText.IGN)) {
            prefix = "Ign";
        }

        return prefix + ": " + mc.thePlayer.getGameProfile().getName();
    }

    @Override
    public String getIcon() {
        return iconSetting.isToggled() ? LegacyIcon.USER : null;
    }
}
