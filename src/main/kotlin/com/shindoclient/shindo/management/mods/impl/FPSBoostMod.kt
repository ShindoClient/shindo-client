package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventUpdate
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.BooleanSetting
import com.shindoclient.shindo.management.settings.impl.NumberSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getNumberSetting
import com.shindoclient.shindo.utils.ServerUtils.isInTabList

class FPSBoostMod : Mod(TranslateText.FPS_BOOST, TranslateText.FPS_BOOST_DESCRIPTION, ModCategory.OTHER, Shinconic.MOD_FPS_BOOST) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CHUNK_DELAY)
    @JvmField
    var chunkDelaySetting = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.DELAY,
        min = 1.0,
        max = 12.0,
        current = 5.0,
        step = 1.0,
    )
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
