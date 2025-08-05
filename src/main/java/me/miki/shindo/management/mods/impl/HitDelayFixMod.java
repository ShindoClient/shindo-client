package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class HitDelayFixMod extends Mod {

    private static HitDelayFixMod instance;

    public HitDelayFixMod() {
        super(TranslateText.HIT_DELAY_FIX, TranslateText.HIT_DELAY_FIX_DESCRIPTION, ModCategory.PLAYER, "nodelay", true);

        instance = this;
    }

    public static HitDelayFixMod getInstance() {
        return instance;
    }
}
