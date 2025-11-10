package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventAttackEntity;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class ParticleCustomizerMod extends Mod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ALWAYS_SHARPNESS)
    private boolean alwaysSharpnessSetting = false;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ALWAYS_CRITICALS)
    private boolean alwaysCriticalsSetting = false;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SHARPNESS)
    private boolean sharpnessSetting = true;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CRITICALS)
    private boolean criticalsSetting = false;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.SHARPNESS_AMOUNT, min = 1, max = 10, current = 2, step = 1)
    private int sharpnessAmountSetting = 2;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.CRITICALS_AMOUNT, min = 1, max = 10, current = 2, step = 1)
    private int criticalsAmountSetting = 2;

    public ParticleCustomizerMod() {
        super(TranslateText.PARTICLE_CUSTOMIZER, TranslateText.PARTICLE_CUSTOMIZER_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onAttackEntity(EventAttackEntity event) {

        EntityPlayer player = mc.thePlayer;

        int sMultiplier = sharpnessAmountSetting;
        int cMultiplier = criticalsAmountSetting;

        if (!(event.getEntity() instanceof EntityLivingBase)) {
            return;
        }

        boolean critical = criticalsSetting && player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Potion.blindness) && player.ridingEntity == null;
        boolean alwaysSharpness = alwaysSharpnessSetting;
        boolean sharpness = sharpnessSetting && EnchantmentHelper.getModifierForCreature(player.getHeldItem(), ((EntityLivingBase) event.getEntity()).getCreatureAttribute()) > 0;
        boolean alwaysCriticals = alwaysCriticalsSetting;

        if (critical || alwaysCriticals) {
            for (int i = 0; i < cMultiplier - 1; i++) {
                mc.effectRenderer.emitParticleAtEntity(event.getEntity(), EnumParticleTypes.CRIT);
            }
        }

        if (alwaysSharpness || sharpness) {
            for (int i = 0; i < sMultiplier - 1; i++) {
                mc.effectRenderer.emitParticleAtEntity(event.getEntity(), EnumParticleTypes.CRIT_MAGIC);
            }
        }
    }
}
