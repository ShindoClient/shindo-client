package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType

class PlayerCounterMod :
    SimpleHUDMod(
        TranslateText.PLAYER_COUNTER,
        TranslateText.PLAYER_COUNTER_DESCRIPTION,
        Shinconic.MOD_PLAYER_COUNTER,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @EventTarget
    fun onRender2D(event: EventNVG) {
        this.draw()
    }

    override fun getText(): String = "Player: " + mc.thePlayer.sendQueue.playerInfoMap.size

    override fun getIcon(): String? = if (iconSetting) Lucide.USERS else null
}
