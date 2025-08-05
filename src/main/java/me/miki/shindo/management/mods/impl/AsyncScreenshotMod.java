package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;

public class AsyncScreenshotMod extends Mod {

    private static AsyncScreenshotMod instance;

    private final BooleanSetting messageSetting = new BooleanSetting(TranslateText.MESSAGE, this, true);
    private final BooleanSetting clipboardSetting = new BooleanSetting(TranslateText.CLIPBOARD, this, false);

    public AsyncScreenshotMod() {
        super(TranslateText.ASYNC_SCREENSHOT, TranslateText.ASYNC_SCREENSHOT_DESCRIPTION, ModCategory.OTHER);

        instance = this;
    }

    public static AsyncScreenshotMod getInstance() {
        return instance;
    }

    public BooleanSetting getMessageSetting() {
        return messageSetting;
    }

    public BooleanSetting getClipboardSetting() {
        return clipboardSetting;
    }
}
