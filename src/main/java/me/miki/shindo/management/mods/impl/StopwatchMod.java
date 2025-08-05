package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.KeybindSetting;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.TimerUtils;
import org.lwjgl.input.Keyboard;

import java.text.DecimalFormat;

public class StopwatchMod extends SimpleHUDMod {

    private final BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);

    private final KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_P);

    private final TimerUtils timer = new TimerUtils();
    private int pressCount;
    private float currentTime;
    private final DecimalFormat timeFormat = new DecimalFormat("0.00");

    public StopwatchMod() {
        super(TranslateText.STOPWATCH, TranslateText.STOPWATCH_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @EventTarget
    public void onTick(EventTick event) {
        switch (pressCount) {
            case 0:
                timer.reset();
                break;
            case 1:
                currentTime = (timer.getElapsedTime() / 1000F);
                break;
            case 3:
                timer.reset();
                currentTime = 0;
                pressCount = 0;
                break;
        }
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (event.getKeyCode() == keybindSetting.getKeyCode()) {
            pressCount++;
        }
    }

    @Override
    public String getText() {
        return timeFormat.format(currentTime) + " s";
    }

    @Override
    public String getIcon() {
        return iconSetting.isToggled() ? LegacyIcon.WATCH : null;
    }

    @Override
    public void onEnable() {

        super.onEnable();

        if (timer != null) {
            timer.reset();
        }

        pressCount = 0;
        currentTime = 0;
    }
}
