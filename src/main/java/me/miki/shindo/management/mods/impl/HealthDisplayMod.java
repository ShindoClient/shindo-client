package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.nanovg.font.LegacyIcon;

public class HealthDisplayMod extends SimpleHUDMod {

    private final BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);

    public HealthDisplayMod() {
        super(TranslateText.HEALTH_DISPLAY, TranslateText.HEALTH_DISPLAY_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @Override
    public String getText() {
        return (int) mc.thePlayer.getHealth() + " Health";
    }

    @Override
    public String getIcon() {
        return iconSetting.isToggled() ? LegacyIcon.HEART : null;
    }
}
