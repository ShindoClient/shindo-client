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

class BowZoomMod :
    Mod(TranslateText.BOW_ZOOM, TranslateText.BOW_ZOOM_DESCRIPTION, ModCategory.PLAYER, LegacyIcon.MOD_BOW_ZOOM) {
    @Property(type = PropertyType.NUMBER, translate = TranslateText.FACTOR, min = 1.0, max = 1.05, current = 5.0, step = 1.0)
    private val factorSetting = 5

    @EventTarget
    fun onFovUpdate(event: EventFovUpdate) {
        var base = 1.0f
        val entity: EntityPlayer = event.getEntity()
        val item = entity.getItemInUse()
        val useDuration = entity.getItemInUseDuration()

        val bowFov = factorSetting.toFloat()

        if (item != null && item.getItem() === Items.bow) {
            val duration = min(useDuration.toFloat(), 20.0f).toInt()
            val modifier: Float = PlayerUtils.MODIFIER_BY_TICK.get(duration)!!
            base -= modifier * bowFov
            event.setFov(base)
        }
        mc.renderGlobal.setDisplayListEntitiesDirty()
    }
}




