package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class ShinyPotsMod extends Mod {

    private static ShinyPotsMod instance;

    public ShinyPotsMod() {
        super(TranslateText.SHINY_POTS, TranslateText.SHINY_POTS_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    public static ShinyPotsMod getInstance() {
        return instance;
    }
}
