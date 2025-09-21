package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventCameraRotation;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.event.impl.EventPlayerHeadRotation;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.ComboSetting;
import me.miki.shindo.management.mods.settings.impl.KeybindSetting;
import me.miki.shindo.management.mods.settings.impl.combo.Option;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;

public class FreelookMod extends Mod {

    private final BooleanSetting invertYawSetting = new BooleanSetting(TranslateText.INVERT_YAW, this, false);
    private final BooleanSetting invertPitchSetting = new BooleanSetting(TranslateText.INVERT_PITCH, this, false);
    private final ComboSetting modeSetting = new ComboSetting(TranslateText.MODE, this, TranslateText.KEYDOWN, new ArrayList<Option>(Arrays.asList(new Option(TranslateText.TOGGLE), new Option(TranslateText.KEYDOWN))));
    private final KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_V);
    @Getter
    private boolean active;
    private float yaw;
    private float pitch;
    private int previousPerspective;
    private boolean toggled;

    @Getter
    private static FreelookMod instance;

    public FreelookMod() {
        super(TranslateText.FREELOOK, TranslateText.FREELOOK_DESCRIPTION, ModCategory.PLAYER, "perspectivemod", true);

        instance = this;
    }

    @EventTarget
    public void onTick(EventTick event) {

        Option mode = modeSetting.getOption();

        if (mode.getTranslate().equals(TranslateText.KEYDOWN)) {
            if (keybindSetting.isKeyDown()) {
                start();
            } else {
                stop();
            }
        }

        if (mode.getTranslate().equals(TranslateText.TOGGLE)) {
            if (toggled) {
                start();
            } else {
                stop();
            }
        }
    }

    @EventTarget
    public void onKey(EventKey event) {

        Option mode = modeSetting.getOption();

        if (mode.getTranslate().equals(TranslateText.TOGGLE)) {
            if (keybindSetting.isKeyDown()) {
                toggled = !toggled;
            }
        }

        if (event.getKeyCode() == mc.gameSettings.keyBindTogglePerspective.getKeyCode()) {
            toggled = false;
        }
    }

    @EventTarget
    public void onCameraRotation(EventCameraRotation event) {
        if (active) {
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }

    @EventTarget
    public void onPlayerHeadRotation(EventPlayerHeadRotation event) {

        if (active) {
            float yaw = event.getYaw();
            float pitch = event.getPitch();
            event.setCancelled(true);
            pitch = -pitch;

            if (!invertPitchSetting.isToggled()) {
                pitch = -pitch;
            }

            if (invertYawSetting.isToggled()) {
                yaw = -yaw;
            }

            this.yaw += yaw * 0.15F;
            this.pitch = MathHelper.clamp_float(this.pitch + (pitch * 0.15F), -90, 90);
            mc.renderGlobal.setDisplayListEntitiesDirty();
        }
    }

    private void start() {
        if (!active) {
            active = true;
            previousPerspective = mc.gameSettings.thirdPersonView;
            mc.gameSettings.thirdPersonView = 3;
            Entity renderView = mc.getRenderViewEntity();
            yaw = renderView.rotationYaw;
            pitch = renderView.rotationPitch;
        }
    }

    private void stop() {
        if (active) {
            active = false;
            mc.gameSettings.thirdPersonView = previousPerspective;
            mc.renderGlobal.setDisplayListEntitiesDirty();
        }
    }

    /** Yaw da câmera enquanto o freelook está ativo (em graus). */
    public float getCameraYaw() {
        return yaw;
    }

    /** Pitch da câmera enquanto o freelook está ativo (em graus). */
    public float getCameraPitch() {
        return pitch;
    }
}
