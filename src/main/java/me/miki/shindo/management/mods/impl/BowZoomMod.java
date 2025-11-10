package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventFovUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.utils.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class BowZoomMod extends Mod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.FACTOR, min = 1, max = 15, current = 5, step = 1)
    private int factorSetting = 5;

    public BowZoomMod() {
        super(TranslateText.BOW_ZOOM, TranslateText.BOW_ZOOM_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onFovUpdate(EventFovUpdate event) {

        float base = 1.0F;
        EntityPlayer entity = event.getEntity();
        ItemStack item = entity.getItemInUse();
        int useDuration = entity.getItemInUseDuration();

        float bowFov = factorSetting;

        if (item != null && item.getItem() == Items.bow) {
            int duration = (int) Math.min(useDuration, 20.0F);
            float modifier = PlayerUtils.MODIFIER_BY_TICK.get(duration);
            base -= modifier * bowFov;
            event.setFov(base);
        }
        mc.renderGlobal.setDisplayListEntitiesDirty();
    }
}
