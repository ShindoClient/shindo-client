package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class PlayerCounterMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private boolean iconSetting = true;

    public PlayerCounterMod() {
        super(TranslateText.PLAYER_COUNTER, TranslateText.PLAYER_COUNTER_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @Override
    public String getText() {
        return "Player: " + mc.thePlayer.sendQueue.getPlayerInfoMap().size();
    }

    @Override
    public String getIcon() {
        return iconSetting ? LegacyIcon.USERS : null;
    }
}
