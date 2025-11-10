package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.SimpleHUDMod;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class ToggleSprintMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HUD)
    private boolean hudEnabled = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ALWAYS)
    private boolean alwaysSprint = false;

    private long startTime;
    private boolean wasDown;

    private State state;

    public ToggleSprintMod() {
        super(TranslateText.TOGGLE_SPRINT, TranslateText.TOGGLE_SPRINT_DESCRIPTION);
    }

    @Override
    public void setup() {
        state = State.WALK;
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        if (hudEnabled) {
            this.draw();
        }

        this.setDraggable(hudEnabled);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), state.equals(State.HELD) || state.equals(State.TOGGLED) || alwaysSprint);
    }

    @EventTarget
    public void onTick(EventTick event) {

        boolean down = Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode());

        if (alwaysSprint || mc.currentScreen != null) {
            return;
        }

        if (down) {
            if (!wasDown) {

                startTime = System.currentTimeMillis();

                if (state.equals(State.TOGGLED)) {
                    state = State.HELD;
                } else {
                    state = State.TOGGLED;
                }
            } else if ((System.currentTimeMillis() - startTime) > 250) {
                state = State.HELD;
            }
        } else if (state.equals(State.HELD) && mc.thePlayer.isSprinting()) {
            state = State.VANILLA;
        } else if ((state.equals(State.VANILLA) || state.equals(State.HELD)) && !mc.thePlayer.isSprinting()) {
            state = State.WALK;
        }

        wasDown = down;
    }

    @Override
    public String getText() {

        String prefix = "Sprinting";

        if (alwaysSprint) {
            return prefix + " (Always)";
        }

        if (state.equals(State.WALK)) {
            return "Walking";
        }

        return prefix + " (" + state.name + ")";
    }

    private enum State {
        WALK("Walking"), VANILLA("Vanilla"), HELD("Key Held"), TOGGLED("Toggled");

        private final String name;

        State(String name) {
            this.name = name;
        }
    }
}
