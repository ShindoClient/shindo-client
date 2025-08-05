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
import net.minecraft.potion.PotionEffect;

import java.util.Collection;

public class FovModifierMod extends Mod {

    private final NumberSetting sprintingSetting = new NumberSetting(TranslateText.SPRINTING, this, 1, -5, 5, false);
    private final NumberSetting bowSetting = new NumberSetting(TranslateText.BOW, this, 1, -5, 5, false);
    private final NumberSetting speedSetting = new NumberSetting(TranslateText.SPEED, this, 1, -5, 5, false);
    private final NumberSetting slownessSetting = new NumberSetting(TranslateText.SLOWNESS, this, 1, -5, 5, false);

    public FovModifierMod() {
        super(TranslateText.FOV_MODIFIER, TranslateText.FOV_MODIFIER_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onFovUpdate(EventFovUpdate event) {

        float base = 1.0F;
        EntityPlayer entity = event.getEntity();
        ItemStack item = entity.getItemInUse();
        int useDuration = entity.getItemInUseDuration();

        float sprintingFov = sprintingSetting.getValueFloat();
        float bowFov = bowSetting.getValueFloat();
        float speedFov = speedSetting.getValueFloat();
        float slownessFov = slownessSetting.getValueFloat();

        if (entity.isSprinting()) {
            base += 0.15000000596046448 * sprintingFov;
        }

        if (item != null && item.getItem() == Items.bow) {
            int duration = (int) Math.min(useDuration, 20.0F);
            float modifier = PlayerUtils.MODIFIER_BY_TICK.get(duration);
            base -= modifier * bowFov;
        }

        Collection<PotionEffect> effects = entity.getActivePotionEffects();
        if (!effects.isEmpty()) {
            for (PotionEffect effect : effects) {
                int potionID = effect.getPotionID();
                if (potionID == 1) {
                    base += 0.1F * (effect.getAmplifier() + 1) * speedFov;
                }

                if (potionID == 2) {
                    base += -0.075F * (effect.getAmplifier() + 1) * slownessFov;
                }
            }
        }

        event.setFov(base);
    }
}
