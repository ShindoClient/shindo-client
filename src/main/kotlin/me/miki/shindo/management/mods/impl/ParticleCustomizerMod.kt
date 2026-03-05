package me.miki.shindo.management.mods.impl

import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventAttackEntity
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion
import net.minecraft.util.EnumParticleTypes

class ParticleCustomizerMod : Mod(
    TranslateText.PARTICLE_CUSTOMIZER,
    TranslateText.PARTICLE_CUSTOMIZER_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_PARTICLE_CUSTOMIZER
) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ALWAYS_SHARPNESS)
    private val alwaysSharpnessSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ALWAYS_CRITICALS)
    private val alwaysCriticalsSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SHARPNESS)
    private val sharpnessSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CRITICALS)
    private val criticalsSetting = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.SHARPNESS_AMOUNT,
        min = 1.0,
        max = 1.00,
        current = 2.0,
        step = 1.0
    )
    private val sharpnessAmountSetting = 2

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.CRITICALS_AMOUNT,
        min = 1.0,
        max = 1.00,
        current = 2.0,
        step = 1.0
    )
    private val criticalsAmountSetting = 2

    @EventTarget
    fun onAttackEntity(event: EventAttackEntity) {
        val player: EntityPlayer = mc.thePlayer

        val sMultiplier = sharpnessAmountSetting
        val cMultiplier = criticalsAmountSetting

        if (event.getEntity() !is EntityLivingBase) {
            return
        }

        val critical =
            criticalsSetting && player.fallDistance > 0.0f && !player.onGround && !player.isOnLadder && !player.isInWater && !player.isPotionActive(
                Potion.blindness
            ) && player.ridingEntity == null
        val alwaysSharpness = alwaysSharpnessSetting
        val sharpness = sharpnessSetting && EnchantmentHelper.getModifierForCreature(
            player.heldItem,
            (event.getEntity() as EntityLivingBase).creatureAttribute
        ) > 0
        val alwaysCriticals = alwaysCriticalsSetting

        if (critical || alwaysCriticals) {
            for (i in 0 until cMultiplier - 1) {
                mc.effectRenderer.emitParticleAtEntity(event.getEntity(), EnumParticleTypes.CRIT)
            }
        }

        if (alwaysSharpness || sharpness) {
            for (i in 0 until sMultiplier - 1) {
                mc.effectRenderer.emitParticleAtEntity(event.getEntity(), EnumParticleTypes.CRIT_MAGIC)
            }
        }
    }
}





