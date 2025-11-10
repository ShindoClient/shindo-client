package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventFireOverlay;
import me.miki.shindo.management.event.impl.EventRenderPumpkinOverlay;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class OverlayEditorMod extends Mod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HIDE_PUMPKIN)
    private boolean hidePumpkinSetting = false;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HIDE_FIRE)
    private boolean hideFireSetting = false;

    public OverlayEditorMod() {
        super(TranslateText.OVERLAY_EDITOR, TranslateText.OVERLAY_EDITOR_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onRenderPumpkinOverlay(EventRenderPumpkinOverlay event) {
        event.setCancelled(hidePumpkinSetting);
    }

    @EventTarget
    public void onFireOverlay(EventFireOverlay event) {
        event.setCancelled(hideFireSetting);
    }
}
