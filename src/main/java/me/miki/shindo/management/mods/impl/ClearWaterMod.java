package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventWaterOverlay;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class ClearWaterMod extends Mod {

    public ClearWaterMod() {
        super(TranslateText.CLEAR_WATER, TranslateText.CLEAR_WATER_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onWaterOverlay(EventWaterOverlay event) {
        event.setCancelled(true);
    }
}
