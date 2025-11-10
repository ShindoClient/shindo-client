package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventText;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class NameProtectMod extends Mod {

    @Property(type = PropertyType.TEXT, translate = TranslateText.NAME, text = "You")
    private String nameSetting = "You";

    public NameProtectMod() {
        super(TranslateText.NAME_PROTECT, TranslateText.NAME_PROTECT_DESCRIPTION, ModCategory.PLAYER, "nickhider");
    }

    @EventTarget
    public void onText(EventText event) {
        event.replace(mc.getSession().getUsername(), nameSetting);
    }
}
