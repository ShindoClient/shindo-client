package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class FemaleGenderMod extends Mod {

    private static FemaleGenderMod instance;

    public FemaleGenderMod() {
        super(TranslateText.FEMALE_GENDER, TranslateText.FEMALE_GENDER_DESCRIPTION, ModCategory.PLAYER, "boobs");

        instance = this;
    }

    public static FemaleGenderMod getInstance() {
        return instance;
    }
}
