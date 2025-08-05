package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.ServerUtils;

public class ServerIPDisplayMod extends SimpleHUDMod {

    private final BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);

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
        return iconSetting.isToggled() ? LegacyIcon.SERVER : null;
    }
}
