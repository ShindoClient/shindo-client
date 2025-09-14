package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
import me.miki.shindo.utils.ServerUtils;
import net.minecraft.entity.Entity;

public class FPSBoostMod extends Mod {

    private static FPSBoostMod instance;

    private final BooleanSetting chunkDelaySetting = new BooleanSetting(TranslateText.CHUNK_DELAY, this, false);
    private final NumberSetting delaySetting = new NumberSetting(TranslateText.DELAY, this, 5, 1, 12, true);
    private final BooleanSetting removeBotSetting = new BooleanSetting(TranslateText.REMOVE_BOT, this, false);

    public FPSBoostMod() {
        super(TranslateText.FPS_BOOST, TranslateText.FPS_BOOST_DESCRIPTION, ModCategory.OTHER);

        instance = this;
    }

    public static FPSBoostMod getInstance() {
        return instance;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {

        if (removeBotSetting.isToggled()) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity.isInvisible() && !ServerUtils.isInTabList(entity)) {
                    mc.theWorld.removeEntity(entity);
                }
            }
        }
    }

    public BooleanSetting getChunkDelaySetting() {
        return chunkDelaySetting;
    }

    public NumberSetting getDelaySetting() {
        return delaySetting;
    }
}
