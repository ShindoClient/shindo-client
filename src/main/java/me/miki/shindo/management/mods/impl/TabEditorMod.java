package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;

public class TabEditorMod extends Mod {

    private static TabEditorMod instance;

    private final BooleanSetting backgroundSetting = new BooleanSetting(TranslateText.BACKGROUND, this, true);
    private final BooleanSetting headSetting = new BooleanSetting(TranslateText.HEAD, this, true);
    private final BooleanSetting pingSetting = new BooleanSetting(TranslateText.PING_NUMBER, this, true);

    public TabEditorMod() {
        super(TranslateText.TAB_EDITOR, TranslateText.TAB_EDITOR_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    public static TabEditorMod getInstance() {
        return instance;
    }

    public BooleanSetting getBackgroundSetting() {
        return backgroundSetting;
    }

    public BooleanSetting getHeadSetting() {
        return headSetting;
    }

    public BooleanSetting getPingSetting() {
        return pingSetting;
    }
}
