package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventClickMouse;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

public class CPSDisplayMod extends SimpleHUDMod {

    private final ArrayList<Long> leftPresses = new ArrayList<Long>();
    private final ArrayList<Long> rightPresses = new ArrayList<Long>();

    private final BooleanSetting rightClickSetting = new BooleanSetting(TranslateText.RIGHT_CLICK, this, true);
    private final BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);

    public CPSDisplayMod() {
        super(TranslateText.CPS_DISPLAY, TranslateText.CPS_DISPLAY_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @EventTarget
    public void onClickMouse(EventClickMouse event) {

        if (Mouse.getEventButtonState()) {

            if (event.getButton() == 0) {
                leftPresses.add(System.currentTimeMillis());
            }

            if (event.getButton() == 1) {
                rightPresses.add(System.currentTimeMillis());
            }
        }
    }

    @EventTarget
    public void onTick(EventTick event) {
        leftPresses.removeIf(t -> System.currentTimeMillis() - t > 1000);
        rightPresses.removeIf(t -> System.currentTimeMillis() - t > 1000);
    }

    @Override
    public String getText() {
        return (rightClickSetting.isToggled() ? leftPresses.size() + " | " + rightPresses.size() : leftPresses.size()) + " CPS";
    }

    @Override
    public String getIcon() {
        return iconSetting.isToggled() ? LegacyIcon.MOUSE_POINTER : null;
    }
}
