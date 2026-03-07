package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class PlayerCounterMod : SimpleHUDMod(
    TranslateText.PLAYER_COUNTER,
    TranslateText.PLAYER_COUNTER_DESCRIPTION,
    LegacyIcon.MOD_PLAYER_COUNTER
) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @EventTarget
    fun onRender2D(event: EventNVG) {
        this.draw()
    }

    override fun getText(): String {
        return "Player: " + mc.thePlayer.sendQueue.playerInfoMap.size
    }

    override fun getIcon(): String? {
        return if (iconSetting) LegacyIcon.USERS else null
    }
}


