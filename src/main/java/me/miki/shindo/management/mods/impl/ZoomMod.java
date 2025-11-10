package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventScrollMouse;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.event.impl.EventZoomFov;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import org.lwjgl.input.Keyboard;

public class ZoomMod extends Mod {

    private final SimpleAnimation zoomAnimation = new SimpleAnimation();

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SCROLL, category = "Behavior")
    private boolean scrollSetting;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SMOOTH_ZOOM, category = "Behavior")
    private boolean smoothZoomSetting;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.ZOOM_SPEED, category = "Behavior", min = 5, max = 20, step = 1, current = 14)
    private double zoomSpeedSetting = 14;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.ZOOM_FACTOR, category = "Behavior", min = 2, max = 15, step = 1, current = 4)
    private double factorSetting = 4;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SMOOTH_CAMERA, category = "Behavior")
    private boolean smoothCameraSetting = true;

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, category = "Controls", keyCode = Keyboard.KEY_C)
    private int zoomKey = Keyboard.KEY_C;

    public boolean wasCinematic;
    private boolean active;
    private float lastSensitivity;
    private float currentFactor = 1;

    public ZoomMod() {
        super(TranslateText.ZOOM, TranslateText.ZOOM_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (Keyboard.isKeyDown(zoomKey)) {
            if (!active) {
                active = true;
                lastSensitivity = mc.gameSettings.mouseSensitivity;
                resetFactor();
                wasCinematic = this.mc.gameSettings.smoothCamera;
                mc.gameSettings.smoothCamera = smoothCameraSetting;
                mc.renderGlobal.setDisplayListEntitiesDirty();
            }
        } else if (active) {
            active = false;
            setFactor(1);
            mc.gameSettings.mouseSensitivity = lastSensitivity;
            mc.gameSettings.smoothCamera = wasCinematic;
        }
    }

    @EventTarget
    public void onFov(EventZoomFov event) {

        zoomAnimation.setAnimation(currentFactor, (float) zoomSpeedSetting);

        event.setFov(event.getFov() * (smoothZoomSetting ? zoomAnimation.getValue() : currentFactor));
    }

    @EventTarget
    public void onScroll(EventScrollMouse event) {
        if (active && scrollSetting) {
            event.setCancelled(true);
            if (event.getAmount() < 0) {
                if (currentFactor < 0.98) {
                    currentFactor += 0.03;
                }
            } else if (event.getAmount() > 0) {
                if (currentFactor > 0.06) {
                    currentFactor -= 0.03;
                }
            }
        }
    }

    public void resetFactor() {
        setFactor(1 / (float) factorSetting);
    }

    public void setFactor(float factor) {
        if (factor != currentFactor) {
            mc.renderGlobal.setDisplayListEntitiesDirty();
        }
        currentFactor = factor;
    }
}
