package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventAttackEntity;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.block.Block;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class BloodParticlesMod extends Mod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.AMOUNT, min = 1, max = 10, current = 2, step = 1)
    private int amountSetting = 2;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SOUND)
    private boolean soundSetting = true;

    private EntityLivingBase target;

    public BloodParticlesMod() {
        super(TranslateText.BLOOD_PARTICLES, TranslateText.BLOOD_PARTICLES_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onAttackEntity(EventAttackEntity event) {

        if (!(event.getEntity() instanceof EntityLivingBase)) {
            return;
        }

        if (target != null) {
            for (int i = 0; i < amountSetting; i++) {
                mc.theWorld.spawnParticle(EnumParticleTypes.BLOCK_CRACK, target.posX, target.posY + target.height - 0.75, target.posZ, 0, 0, 0, Block.getStateId(Blocks.redstone_block.getDefaultState()));
            }
        }

        if (soundSetting && target != null) {
            mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation("dig.stone"), 4.0F, 1.2F, ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.objectMouseOver != null & mc.objectMouseOver.entityHit != null) {
            if (mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
                target = (EntityLivingBase) mc.objectMouseOver.entityHit;
            }
        }
    }
}
