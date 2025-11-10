package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.utils.ServerUtils;
import net.minecraft.entity.Entity;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class FPSBoostMod extends Mod {

    @Getter
    private static FPSBoostMod instance;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CHUNK_DELAY)
    private boolean chunkDelaySetting = false;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.DELAY, min = 1, max = 12, current = 5, step = 1)
    private int delaySetting = 5;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.REMOVE_BOT)
    private boolean removeBotSetting = false;

    public FPSBoostMod() {
        super(TranslateText.FPS_BOOST, TranslateText.FPS_BOOST_DESCRIPTION, ModCategory.OTHER);

        instance = this;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {

        if (removeBotSetting) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity.isInvisible() && !ServerUtils.isInTabList(entity)) {
                    mc.theWorld.removeEntity(entity);
                }
            }
        }
    }

    public BooleanSetting getChunkDelaySetting() {
        return SettingRegistry.getBooleanSetting(this, "chunkDelaySetting");
    }

    public NumberSetting getDelaySetting() {
        return SettingRegistry.getNumberSetting(this, "delaySetting");
    }
}
