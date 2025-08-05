package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventHurtCamera;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class MinimalDamageShakeMod extends Mod {

    private final NumberSetting intensitySetting = new NumberSetting(TranslateText.INTENSITY, this, 0, 0, 100, true);

    public MinimalDamageShakeMod() {
        super(TranslateText.MINIMAL_DAMAGE_SHAKE, TranslateText.MINIMAL_DAMAGE_SHAKE_DESCRIPTION, ModCategory.RENDER, "nohurtcam");
    }

    @EventTarget
    public void onHurtCamera(EventHurtCamera event) {
        event.setIntensity(intensitySetting.getValueFloat() / 100F);
    }
}
