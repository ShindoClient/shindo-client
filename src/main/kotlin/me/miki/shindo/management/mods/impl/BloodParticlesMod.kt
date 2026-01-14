package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventAttackEntity
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.block.Block
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation

class BloodParticlesMod : Mod(
    TranslateText.BLOOD_PARTICLES,
    TranslateText.BLOOD_PARTICLES_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_BLOOD_PARTICLES
) {
    @Property(type = PropertyType.NUMBER, translate = TranslateText.AMOUNT, min = 1.0, max = 10.0, current = 2.0, step = 1.0)
    private val amountSetting = 2

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SOUND)
    private val soundSetting = true

    private var target: EntityLivingBase? = null

    @EventTarget
    fun onAttackEntity(event: EventAttackEntity) {
        if (event.getEntity() !is EntityLivingBase) {
            return
        }

        if (target != null) {
            for (i in 0..<amountSetting) {
                mc.theWorld.spawnParticle(
                    EnumParticleTypes.BLOCK_CRACK,
                    target!!.posX,
                    target!!.posY + target!!.height - 0.75,
                    target!!.posZ,
                    0.0,
                    0.0,
                    0.0,
                    Block.getStateId(Blocks.redstone_block.getDefaultState())
                )
            }
        }

        if (soundSetting && target != null) {
            mc.getSoundHandler().playSound(
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

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        if ((mc.objectMouseOver != null) and (mc.objectMouseOver.entityHit != null)) {
            if (mc.objectMouseOver.entityHit is EntityLivingBase) {
                target = mc.objectMouseOver.entityHit as EntityLivingBase
            }
        }
    }
}




