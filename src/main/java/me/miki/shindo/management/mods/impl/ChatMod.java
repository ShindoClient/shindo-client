package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class ChatMod extends Mod {

    private static ChatMod instance;

    private final BooleanSetting smoothSetting = new BooleanSetting(TranslateText.SMOOTH, this, true);
    private final NumberSetting smoothSpeedSetting = new NumberSetting(TranslateText.SMOOTH_SPEED, this, 4, 1, 10, true);
    private final BooleanSetting headSetting = new BooleanSetting(TranslateText.HEAD, this, false);

    private final BooleanSetting infinitySetting = new BooleanSetting(TranslateText.INFINITY, this, false);
    private final BooleanSetting backgroundSetting = new BooleanSetting(TranslateText.BACKGROUND, this, true);

    private final BooleanSetting compactSetting = new BooleanSetting(TranslateText.COMPACT, this, false);

    public ChatMod() {
        super(TranslateText.CHAT, TranslateText.CHAT_DESCRIPTION, ModCategory.OTHER, "betterchatting");

        instance = this;
    }

    public static ChatMod getInstance() {
        return instance;
    }

    public BooleanSetting getSmoothSetting() {
        return smoothSetting;
    }

    public NumberSetting getSmoothSpeedSetting() {
        return smoothSpeedSetting;
    }

    public BooleanSetting getHeadSetting() {
        return headSetting;
    }

    public BooleanSetting getInfinitySetting() {
        return infinitySetting;
    }

    public BooleanSetting getBackgroundSetting() {
        return backgroundSetting;
    }

    public BooleanSetting getCompactSetting() {
        return compactSetting;
    }
}
