package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class DamageTiltMod extends Mod {

    private static DamageTiltMod instance;

    public DamageTiltMod() {
        super(TranslateText.DAMAGE_TILT, TranslateText.DAMAGE_TILT_DESCRIPTION, ModCategory.PLAYER);

        instance = this;
    }

    public static DamageTiltMod getInstance() {
        return instance;
    }
}
