package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import net.minecraft.client.Minecraft;

public class FPSDisplayMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private boolean iconEnabled = true;

    public FPSDisplayMod() {
        super(TranslateText.FPS_DISPLAY, TranslateText.FPS_DISPLAY_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @Override
    public String getText() {
        return Minecraft.getDebugFPS() + " FPS";
    }

    @Override
    public String getIcon() {
        return iconEnabled ? LegacyIcon.MONITOR : null;
    }
}
