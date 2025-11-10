package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventCameraRotation;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class FarCameraMod extends Mod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.RANGE, min = 0, max = 50, current = 15, step = 1)
    private int rangeSetting = 15;

    public FarCameraMod() {
        super(TranslateText.FAR_CAMERA, TranslateText.FAR_CAMERA_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onCameraRotation(EventCameraRotation event) {
        event.setThirdPersonDistance(rangeSetting);
    }
}
