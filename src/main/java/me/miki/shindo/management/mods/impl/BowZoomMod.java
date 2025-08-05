package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventFovUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
import me.miki.shindo.utils.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class BowZoomMod extends Mod {

    private final NumberSetting factorSetting = new NumberSetting(TranslateText.FACTOR, this, 5, 1, 15, true);

    public BowZoomMod() {
        super(TranslateText.BOW_ZOOM, TranslateText.BOW_ZOOM_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onFovUpdate(EventFovUpdate event) {

        float base = 1.0F;
        EntityPlayer entity = event.getEntity();
        ItemStack item = entity.getItemInUse();
        int useDuration = entity.getItemInUseDuration();

        float bowFov = factorSetting.getValueFloat();

        if (item != null && item.getItem() == Items.bow) {
            int duration = (int) Math.min(useDuration, 20.0F);
            float modifier = PlayerUtils.MODIFIER_BY_TICK.get(duration);
            base -= modifier * bowFov;
            event.setFov(base);
        }
        mc.renderGlobal.setDisplayListEntitiesDirty();
    }
}
