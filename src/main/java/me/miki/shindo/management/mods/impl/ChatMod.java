package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.event.impl.EventReceiveChat;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.utils.Sound;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;

public class ChatMod extends Mod {

    @Getter
    private static ChatMod instance;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SMOOTH, category = "Animation")
    private boolean smoothSetting = true;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.SMOOTH_SPEED, category = "Animation", min = 1, max = 10, step = 1, current = 4)
    private double smoothSpeedSetting = 4;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HEAD, category = "Display")
    private boolean headSetting;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.INFINITY, category = "Display")
    private boolean infinitySetting;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND, category = "Display")
    private boolean backgroundSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.COMPACT, category = "Display")
    private boolean compactSetting;

    //@Property(type = PropertyType.BOOLEAN, translate = TranslateText.PING_SOUND)
private boolean pingSetting = false;

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
        return SettingRegistry.getBooleanSetting(this, "smoothSetting");
    }

    public NumberSetting getSmoothSpeedSetting() {
        return SettingRegistry.getNumberSetting(this, "smoothSpeedSetting");
    }

    public BooleanSetting getHeadSetting() {
        return SettingRegistry.getBooleanSetting(this, "headSetting");
    }

    public BooleanSetting getInfinitySetting() {
        return SettingRegistry.getBooleanSetting(this, "infinitySetting");
    }

    public BooleanSetting getBackgroundSetting() {
        return SettingRegistry.getBooleanSetting(this, "backgroundSetting");
    }

    public BooleanSetting getCompactSetting() {
        return SettingRegistry.getBooleanSetting(this, "compactSetting");
    }
}
