package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventCameraRotation;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class FarCameraMod extends Mod {

    private final NumberSetting rangeSetting = new NumberSetting(TranslateText.RANGE, this, 15, 0, 50, true);

    public FarCameraMod() {
        super(TranslateText.FAR_CAMERA, TranslateText.FAR_CAMERA_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onCameraRotation(EventCameraRotation event) {
        event.setThirdPersonDistance(rangeSetting.getValueFloat());
    }
}
