package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventFovUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.PlayerUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import kotlin.math.min

class FovModifierMod : Mod(
    TranslateText.FOV_MODIFIER,
    TranslateText.FOV_MODIFIER_DESCRIPTION,
    ModCategory.PLAYER,
    LegacyIcon.MOD_FOV_MODIFIER
) {
    @Property(type = PropertyType.NUMBER, translate = TranslateText.SPRINTING, min = -5.0, max = 5.0, current = 1.0)
    private val sprintingSetting = 1.0

    @Property(type = PropertyType.NUMBER, translate = TranslateText.BOW, min = -5.0, max = 5.0, current = 1.0)
    private val bowSetting = 1.0

    @Property(type = PropertyType.NUMBER, translate = TranslateText.SPEED, min = -5.0, max = 5.0, current = 1.0)
    private val speedSetting = 1.0

    @Property(type = PropertyType.NUMBER, translate = TranslateText.SLOWNESS, min = -5.0, max = 5.0, current = 1.0)
    private val slownessSetting = 1.0

    @EventTarget
    fun onFovUpdate(event: EventFovUpdate) {
        var base = 1.0f
        val entity: EntityPlayer = event.getEntity()
        val item = entity.itemInUse
        val useDuration = entity.itemInUseDuration

        val sprintingFov = sprintingSetting.toFloat()
        val bowFov = bowSetting.toFloat()
        val speedFov = speedSetting.toFloat()
        val slownessFov = slownessSetting.toFloat()

        if (entity.isSprinting) {
            base += (0.15000000596046448 * sprintingFov).toFloat()
        }

        if (item != null && item.item === Items.bow) {
            val duration = min(useDuration.toFloat(), 20.0f).toInt()
            val modifier: Float = PlayerUtils.MODIFIER_BY_TICK.get(duration)!!
            base -= modifier * bowFov
        }

        val effects = entity.activePotionEffects
        if (!effects.isEmpty()) {
            for (effect in effects) {
                val potionID = effect.potionID
                if (potionID == 1) {
                    base += 0.1f * (effect.amplifier + 1) * speedFov
                }

                if (potionID == 2) {
                    base += -0.075f * (effect.amplifier + 1) * slownessFov
                }
            }
        }

        event.setFov(base)
    }
}




