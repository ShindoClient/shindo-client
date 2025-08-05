package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class MinimalViewBobbingMod extends Mod {

    private static MinimalViewBobbingMod instance;

    public MinimalViewBobbingMod() {
        super(TranslateText.MINIMAL_VIEW_BOBBING, TranslateText.MINIMAL_VIEW_BOBBING_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    public static MinimalViewBobbingMod getInstance() {
        return instance;
    }
}
