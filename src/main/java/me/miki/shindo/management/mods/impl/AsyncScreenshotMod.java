package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.nanovg.font.LegacyIcon;
import lombok.Getter;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class AsyncScreenshotMod extends Mod {

    private static AsyncScreenshotMod instance;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.MESSAGE)
    private boolean messageEnabled = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CLIPBOARD)
    private boolean clipboardEnabled = false;

    public AsyncScreenshotMod() {
        super(TranslateText.ASYNC_SCREENSHOT, TranslateText.ASYNC_SCREENSHOT_DESCRIPTION, ModCategory.OTHER, LegacyIcon.MOD_ASYNC_SCREENSHOT);

        instance = this;
    }

    public static AsyncScreenshotMod getInstance() {
        return instance;
    }

    public boolean isMessageEnabled() {
        return messageEnabled;
    }

    public boolean isClipboardEnabled() {
        return clipboardEnabled;
    }
}




