package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class TabEditorMod extends Mod {

    @Getter
    private static TabEditorMod instance;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND)
    private boolean backgroundSetting = true;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HEAD)
    private boolean headSetting = true;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PING_NUMBER)
    private boolean pingSetting = true;

    public TabEditorMod() {
        super(TranslateText.TAB_EDITOR, TranslateText.TAB_EDITOR_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    public BooleanSetting getBackgroundSetting() {
        return SettingRegistry.getBooleanSetting(this, "backgroundSetting");
    }

    public BooleanSetting getHeadSetting() {
        return SettingRegistry.getBooleanSetting(this, "headSetting");
    }

    public BooleanSetting getPingSetting() {
        return SettingRegistry.getBooleanSetting(this, "pingSetting");
    }
}
