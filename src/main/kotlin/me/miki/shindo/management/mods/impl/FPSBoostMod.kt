package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting
import me.miki.shindo.utils.ServerUtils.isInTabList

class FPSBoostMod :
    Mod(TranslateText.FPS_BOOST, TranslateText.FPS_BOOST_DESCRIPTION, ModCategory.OTHER, LegacyIcon.MOD_FPS_BOOST) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CHUNK_DELAY)
    @JvmField
    var chunkDelaySetting = false

    @Property(type = PropertyType.NUMBER, translate = TranslateText.DELAY, min = 1.0, max = 1.02, current = 5.0, step = 1.0)
    @JvmField
    var delaySetting = 5.0

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.REMOVE_BOT)
    private val removeBotSetting = false

    init {
        instance = this
    }

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        if (removeBotSetting) {
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity.isInvisible && !isInTabList(entity)) {
                    mc.theWorld.removeEntity(entity)
                }
            }
        }
    }

    companion object {
        @JvmField
        var instance: FPSBoostMod? = null

        @JvmStatic
        fun getInstance(): FPSBoostMod? = instance
    }

    fun getChunkDelaySetting(): BooleanSetting? = getBooleanSetting(this, "chunkDelaySetting")

    fun getDelaySetting(): NumberSetting? = getNumberSetting(this, "delaySetting")
}



