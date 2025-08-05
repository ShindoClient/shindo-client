package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.ComboSetting;
import me.miki.shindo.management.mods.settings.impl.combo.Option;

import java.util.ArrayList;
import java.util.Arrays;

public class ClientSpooferMod extends Mod {

    private static ClientSpooferMod instance;

    private final ComboSetting typeSetting = new ComboSetting(TranslateText.TYPE, this, TranslateText.VANILLA, new ArrayList<Option>(Arrays.asList(
            new Option(TranslateText.VANILLA), new Option(TranslateText.FORGE))));

    public ClientSpooferMod() {
        super(TranslateText.CLIENT_SPOOFER, TranslateText.CLIENT_SPOOFER_DESCRIPTION, ModCategory.OTHER);

        instance = this;
    }

    public static ClientSpooferMod getInstance() {
        return instance;
    }

    public ComboSetting getTypeSetting() {
        return typeSetting;
    }
}
