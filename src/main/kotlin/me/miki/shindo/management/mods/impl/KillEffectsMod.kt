package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventLoadWorld
import me.miki.shindo.management.event.impl.EventMotionUpdate
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.block.Block
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation

class KillEffectsMod : Mod(
    TranslateText.KILL_EFFECTS,
    TranslateText.KILL_EFFECTS_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_KILL_EFFECTS
) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SOUND)
    private val soundSetting = true

    @Property(type = PropertyType.COMBO, translate = TranslateText.EFFECT)
    private val effectType = EffectType.BLOOD

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.MULTIPLIER,
        min = 1.0,
        max = 10.0,
        step = 1.0,
        current = 1.0
    )
    private val multiplierSetting = 1

    private var target: EntityLivingBase? = null
    private var entityID = 0

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null) {
            if (mc.objectMouseOver.entityHit is EntityLivingBase) {
                target = mc.objectMouseOver.entityHit as EntityLivingBase
            }
        }
    }

    @EventTarget
    fun onPreMotionUpdate(event: EventMotionUpdate?) {
        if (target != null && !mc.theWorld.loadedEntityList.contains(target) && mc.thePlayer.getDistanceSq(
                target!!.posX,
                mc.thePlayer.posY,
                target!!.posZ
            ) < 100
        ) {
            if (mc.thePlayer.ticksExisted > 3) {
                if (effectType == EffectType.LIGHTNING) {
                    val entityLightningBolt =
                        EntityLightningBolt(mc.theWorld, target!!.posX, target!!.posY, target!!.posZ)
                    mc.theWorld.addEntityToWorld(entityID--, entityLightningBolt)

                    if (soundSetting) {
                        mc.soundHandler.playSound(
                            PositionedSoundRecord.create(
                                ResourceLocation("ambient.weather.thunder"),
                                (target!!.posX.toFloat()),
                                (target!!.posY.toFloat()),
                                (target!!.posZ.toFloat())
                            )
                        )
                    }
                } else if (effectType == EffectType.FLAMES) {
                    for (i in 0 until multiplierSetting) {
                        mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.FLAME)
                    }

                    if (soundSetting) {
                        mc.soundHandler.playSound(
                            PositionedSoundRecord.create(
                                ResourceLocation("item.fireCharge.use"),
                                (target!!.posX.toFloat()),
                                (target!!.posY.toFloat()),
                                (target!!.posZ.toFloat())
                            )
                        )
                    }
                } else if (effectType == EffectType.CLOUD) {
                    for (i in 0 until multiplierSetting) {
                        mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.CLOUD)
                    }

                    if (soundSetting) {
                        mc.soundHandler.playSound(
                            PositionedSoundRecord.create(
                                ResourceLocation("fireworks.twinkle"),
                                (target!!.posX.toFloat()),
                                (target!!.posY.toFloat()),
                                (target!!.posZ.toFloat())
                            )
                        )
                    }
                } else if (effectType == EffectType.BLOOD) {
                    for (i in 0..49) {
                        mc.theWorld.spawnParticle(
                            EnumParticleTypes.BLOCK_CRACK,
                            target!!.posX,
                            target!!.posY + target!!.height - 0.75,
                            target!!.posZ,
                            0.0,
                            0.0,
                            0.0,
                            Block.getStateId(Blocks.redstone_block.defaultState)
                        )
                    }

                    if (soundSetting) {
                        mc.soundHandler.playSound(
                            PositionedSoundRecord(
                                ResourceLocation("dig.stone"),
                                4.0f,
                                1.2f,
                                (target!!.posX.toFloat()),
                                (target!!.posY.toFloat()),
                                (target!!.posZ.toFloat())
                            )
                        )
                    }
                }
            }
            target = null
        }
    }

    @EventTarget
    fun onLoadWorld(event: EventLoadWorld?) {
        entityID = 0
    }

    private enum class EffectType(private val translate: TranslateText) : PropertyEnum {
        LIGHTNING(TranslateText.LIGHTING),
        FLAMES(TranslateText.FLAMES),
        CLOUD(TranslateText.CLOUD),
        BLOOD(TranslateText.BLOOD);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }
}





