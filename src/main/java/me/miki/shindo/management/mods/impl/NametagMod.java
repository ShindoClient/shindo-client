package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class NametagMod extends Mod {

    private static NametagMod instance;

    public NametagMod() {
        super(TranslateText.NAMETAG, TranslateText.NAMETAG_DESCRIPTION, ModCategory.PLAYER);

        instance = this;
    }

    public static NametagMod getInstance() {
        return instance;
    }
}
