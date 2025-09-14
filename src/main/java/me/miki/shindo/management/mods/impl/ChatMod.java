package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.event.impl.EventReceiveChat;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
import me.miki.shindo.utils.Sound;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;

public class ChatMod extends Mod {

    @Getter
    private static ChatMod instance;

    private final BooleanSetting smoothSetting = new BooleanSetting(TranslateText.SMOOTH, this, true);
    private final NumberSetting smoothSpeedSetting = new NumberSetting(TranslateText.SMOOTH_SPEED, this, 4, 1, 10, true);
    private final BooleanSetting headSetting = new BooleanSetting(TranslateText.HEAD, this, false);

    private final BooleanSetting infinitySetting = new BooleanSetting(TranslateText.INFINITY, this, false);
    private final BooleanSetting backgroundSetting = new BooleanSetting(TranslateText.BACKGROUND, this, true);
    private final BooleanSetting compactSetting = new BooleanSetting(TranslateText.COMPACT, this, false);

    //private final BooleanSetting pingSetting = new BooleanSetting(TranslateText.PING_SOUND, this, false);

    public ChatMod() {
        super(TranslateText.CHAT, TranslateText.CHAT_DESCRIPTION, ModCategory.OTHER, "betterchatting");

        instance = this;
    }

    //@EventTarget
    public void onChatMessage(EventReceiveChat event) {
        Minecraft mc = Minecraft.getMinecraft();
        IChatComponent component = event.getMessage();

        String name = mc.thePlayer.getName();
        String text = component.getUnformattedText().toLowerCase().replaceFirst("<.+>", "");

        String[] names = new String[]{name};
        for (String n : names) {
            if (text.contains(n.toLowerCase())) {
                Sound.play("shindo/audio/ping.wav", false);
                break;
            }
        }
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
