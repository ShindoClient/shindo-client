package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.nanovg.font.LegacyIcon;

public class PlayerCounterMod extends SimpleHUDMod {

    private final BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);

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
        return iconSetting.isToggled() ? LegacyIcon.USERS : null;
    }
}
