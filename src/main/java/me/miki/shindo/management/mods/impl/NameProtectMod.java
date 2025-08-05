package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventText;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.TextSetting;

public class NameProtectMod extends Mod {

    private final TextSetting nameSetting = new TextSetting(TranslateText.NAME, this, "You");

    public NameProtectMod() {
        super(TranslateText.NAME_PROTECT, TranslateText.NAME_PROTECT_DESCRIPTION, ModCategory.PLAYER, "nickhider");
    }

    @EventTarget
    public void onText(EventText event) {
        event.replace(mc.getSession().getUsername(), nameSetting.getText());
    }
}
