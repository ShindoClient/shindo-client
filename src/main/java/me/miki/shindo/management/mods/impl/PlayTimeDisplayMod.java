package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class PlayTimeDisplayMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private boolean iconSetting = true;

    public PlayTimeDisplayMod() {
        super(TranslateText.PLAY_TIME_DISPLAY, TranslateText.PLAY_TIME_DISPLAY_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @Override
    public String getText() {

        int sec = (int) ((System.currentTimeMillis() - Shindo.getInstance().getShindoAPI().getLaunchTime()) / 1000);
        int min = (sec % 3600) / 60;
        int hour = sec / 3600;
        sec = sec % 60;

        return String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec);
    }

    @Override
    public String getIcon() {
        return iconSetting ? LegacyIcon.CLOCK : null;
    }
}
