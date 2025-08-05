package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventGamma;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class FullbrightMod extends Mod {

    public FullbrightMod() {
        super(TranslateText.FULLBRIGHT, TranslateText.FULLBRIGHT_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onGamma(EventGamma event) {
        event.setGamma(20F);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.renderGlobal.loadRenderers();
    }
}
