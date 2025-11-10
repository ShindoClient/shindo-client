package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventLoadWorld;
import me.miki.shindo.management.event.impl.EventMotionUpdate;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import net.minecraft.block.Block;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

public class KillEffectsMod extends Mod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SOUND, category = "Audio")
    private boolean soundSetting = true;
    @Property(type = PropertyType.COMBO, translate = TranslateText.EFFECT)
    private EffectType effectType = EffectType.BLOOD;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.MULTIPLIER, category = "Effects", min = 1, max = 10, step = 1, current = 1)
    private int multiplierSetting = 1;
    private EntityLivingBase target;
    private int entityID;

    public KillEffectsMod() {
        super(TranslateText.KILL_EFFECTS, TranslateText.KILL_EFFECTS_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.objectMouseOver != null & mc.objectMouseOver.entityHit != null) {
            if (mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
                target = (EntityLivingBase) mc.objectMouseOver.entityHit;
            }
        }
    }

    @EventTarget
    public void onPreMotionUpdate(EventMotionUpdate event) {

        if (target != null && !mc.theWorld.loadedEntityList.contains(target) && mc.thePlayer.getDistanceSq(target.posX, mc.thePlayer.posY, target.posZ) < 100) {

            if (mc.thePlayer.ticksExisted > 3) {

                if (effectType == EffectType.LIGHTNING) {

                    EntityLightningBolt entityLightningBolt = new EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ);
                    mc.theWorld.addEntityToWorld(entityID--, entityLightningBolt);

                    if (soundSetting) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("ambient.weather.thunder"), ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
                    }
                } else if (effectType == EffectType.FLAMES) {

                    for (int i = 0; i < multiplierSetting; i++) {
                        mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.FLAME);
                    }

                    if (soundSetting) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("item.fireCharge.use"), ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
                    }
                } else if (effectType == EffectType.CLOUD) {

                    for (int i = 0; i < multiplierSetting; i++) {
                        mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.CLOUD);
                    }

                    if (soundSetting) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("fireworks.twinkle"), ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
                    }
                } else if (effectType == EffectType.BLOOD) {

                    for (int i = 0; i < 50; i++) {
                        mc.theWorld.spawnParticle(EnumParticleTypes.BLOCK_CRACK, target.posX, target.posY + target.height - 0.75, target.posZ, 0, 0, 0, Block.getStateId(Blocks.redstone_block.getDefaultState()));
                    }

                    if (soundSetting) {
                        mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation("dig.stone"), 4.0F, 1.2F, ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
                    }
                }
            }
            target = null;
        }
    }

    @EventTarget
    public void onLoadWorld(EventLoadWorld event) {
        entityID = 0;
    }

    private enum EffectType implements PropertyEnum {
        LIGHTNING(TranslateText.LIGHTING),
        FLAMES(TranslateText.FLAMES),
        CLOUD(TranslateText.CLOUD),
        BLOOD(TranslateText.BLOOD);

        private final TranslateText translate;

        EffectType(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
