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
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.config.PropertyEnum;
import net.minecraft.entity.Entity;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

public class FreelookMod extends Mod {

    @Getter
    private static FreelookMod instance;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.INVERT_YAW, category = "Controls")
    private boolean invertYawSetting;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.INVERT_PITCH, category = "Controls")
    private boolean invertPitchSetting;
    @Property(type = PropertyType.COMBO, translate = TranslateText.MODE, category = "Controls")
    private Mode modeSetting = Mode.KEYDOWN;
    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, category = "Controls", keyCode = Keyboard.KEY_V)
    private int keybindSetting = Keyboard.KEY_V;
    @Getter
    private boolean active;
    private float yaw;
    private float pitch;
    private int previousPerspective;
    private boolean toggled;

    public FreelookMod() {
        super(TranslateText.FREELOOK, TranslateText.FREELOOK_DESCRIPTION, ModCategory.PLAYER, "perspectivemod", true);

        instance = this;
    }

    @EventTarget
    public void onTick(EventTick event) {

        Mode mode = modeSetting;

        if (mode == Mode.KEYDOWN) {
            if (isKeyBindDown()) {
                start();
            } else {
                stop();
            }
        }

        if (mode == Mode.TOGGLE) {
            if (toggled) {
                start();
            } else {
                stop();
            }
        }
    }

    @EventTarget
    public void onKey(EventKey event) {

        Mode mode = modeSetting;

        if (mode == Mode.TOGGLE) {
            if (event.getKeyCode() == keybindSetting && mc.currentScreen == null) {
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

            if (!invertPitchSetting) {
                pitch = -pitch;
            }

            if (invertYawSetting) {
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

    private boolean isKeyBindDown() {
        return Keyboard.isKeyDown(keybindSetting) && !(mc.currentScreen instanceof Gui);
    }

    /**
     * Yaw da câmera enquanto o freelook está ativo (em graus).
     */
    public float getCameraYaw() {
        return yaw;
    }

    /**
     * Pitch da câmera enquanto o freelook está ativo (em graus).
     */
    public float getCameraPitch() {
        return pitch;
    }

    private enum Mode implements PropertyEnum {
        TOGGLE(TranslateText.TOGGLE),
        KEYDOWN(TranslateText.KEYDOWN);

        private final TranslateText translate;

        Mode(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
