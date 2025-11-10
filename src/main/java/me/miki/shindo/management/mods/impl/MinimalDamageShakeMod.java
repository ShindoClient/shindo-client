package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventHurtCamera;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class MinimalDamageShakeMod extends Mod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.INTENSITY, min = 0, max = 100, current = 0, step = 1)
    private int intensitySetting = 0;

    public MinimalDamageShakeMod() {
        super(TranslateText.MINIMAL_DAMAGE_SHAKE, TranslateText.MINIMAL_DAMAGE_SHAKE_DESCRIPTION, ModCategory.RENDER, "nohurtcam");
    }

    @EventTarget
    public void onHurtCamera(EventHurtCamera event) {
        event.setIntensity(intensitySetting / 100F);
    }
}
