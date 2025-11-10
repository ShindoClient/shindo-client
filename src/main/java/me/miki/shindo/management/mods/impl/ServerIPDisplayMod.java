package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.ServerUtils;

public class ServerIPDisplayMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private boolean showIcon = true;

    public ServerIPDisplayMod() {
        super(TranslateText.SERVER_IP, TranslateText.SERVER_IP_DISPLAY_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @Override
    public String getText() {
        return ServerUtils.getServerIP();
    }

    @Override
    public String getIcon() {
        return showIcon ? LegacyIcon.SERVER : null;
    }
}
