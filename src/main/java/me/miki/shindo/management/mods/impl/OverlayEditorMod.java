package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventFireOverlay;
import me.miki.shindo.management.event.impl.EventRenderPumpkinOverlay;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;

public class OverlayEditorMod extends Mod {

    private final BooleanSetting hidePumpkinSetting = new BooleanSetting(TranslateText.HIDE_PUMPKIN, this, false);
    private final BooleanSetting hideFireSetting = new BooleanSetting(TranslateText.HIDE_FIRE, this, false);

    public OverlayEditorMod() {
        super(TranslateText.OVERLAY_EDITOR, TranslateText.OVERLAY_EDITOR_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onRenderPumpkinOverlay(EventRenderPumpkinOverlay event) {
        event.setCancelled(hidePumpkinSetting.isToggled());
    }

    @EventTarget
    public void onFireOverlay(EventFireOverlay event) {
        event.setCancelled(hideFireSetting.isToggled());
    }
}
