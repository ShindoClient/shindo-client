package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class ClearGlassMod extends Mod {

    @Getter
    private static ClearGlassMod instance;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NORMAL)
    private boolean normalSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.STAINED)
    private boolean stainedSetting = true;

    private boolean prevNormal, prevStained;

    public ClearGlassMod() {
        super(TranslateText.CLEAR_GLASS, TranslateText.CLEAR_GLASS_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {

        if (prevNormal != normalSetting) {
            prevNormal = normalSetting;
            mc.renderGlobal.loadRenderers();
        }

        if (prevStained != stainedSetting) {
            prevStained = stainedSetting;
            mc.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onEnable() {
        prevNormal = normalSetting;
        prevStained = stainedSetting;
        super.onEnable();
        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.renderGlobal.loadRenderers();
    }

    public BooleanSetting getNormalSetting() {
        return SettingRegistry.getBooleanSetting(this, "normalSetting");
    }

    public BooleanSetting getStainedSetting() {
        return SettingRegistry.getBooleanSetting(this, "stainedSetting");
    }
}
