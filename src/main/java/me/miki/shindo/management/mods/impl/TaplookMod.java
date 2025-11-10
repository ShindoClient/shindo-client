package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import org.lwjgl.input.Keyboard;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
public class TaplookMod extends Mod {

    @Property(type = PropertyType.COMBO, translate = TranslateText.PERSPECTIVE)
    private Perspective perspective = Perspective.FRONT;
    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_P)
    private int keybindSetting = Keyboard.KEY_P;
    private boolean active;
    private int prevPerspective;

    public TaplookMod() {
        super(TranslateText.TAPLOOK, TranslateText.TAPLOOK_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (Keyboard.isKeyDown(keybindSetting)) {
            if (!active) {
                this.start();
            }
        } else if (active) {
            this.stop();
        }
    }

    private void start() {

        int perspectiveView = perspective == Perspective.FRONT ? 2 : 1;

        active = true;
        prevPerspective = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = perspectiveView;
        mc.renderGlobal.setDisplayListEntitiesDirty();
    }

    private void stop() {
        active = false;
        mc.gameSettings.thirdPersonView = prevPerspective;
        mc.renderGlobal.setDisplayListEntitiesDirty();
    }

    private enum Perspective implements PropertyEnum {
        FRONT(TranslateText.FRONT),
        BEHIND(TranslateText.BEHIND);

        private final TranslateText translate;

        Perspective(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
