package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;

public class ClearGlassMod extends Mod {

    private static ClearGlassMod instance;

    private final BooleanSetting normalSetting = new BooleanSetting(TranslateText.NORMAL, this, true);
    private final BooleanSetting stainedSetting = new BooleanSetting(TranslateText.STAINED, this, true);

    private boolean prevNormal, prevStained;

    public ClearGlassMod() {
        super(TranslateText.CLEAR_GLASS, TranslateText.CLEAR_GLASS_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    public static ClearGlassMod getInstance() {
        return instance;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {

        if (prevNormal != normalSetting.isToggled()) {
            prevNormal = normalSetting.isToggled();
            mc.renderGlobal.loadRenderers();
        }

        if (prevStained != stainedSetting.isToggled()) {
            prevStained = stainedSetting.isToggled();
            mc.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onEnable() {
        prevNormal = normalSetting.isToggled();
        prevStained = stainedSetting.isToggled();
        super.onEnable();
        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.renderGlobal.loadRenderers();
    }

    public BooleanSetting getNormalSetting() {
        return normalSetting;
    }

    public BooleanSetting getStainedSetting() {
        return stainedSetting;
    }
}
