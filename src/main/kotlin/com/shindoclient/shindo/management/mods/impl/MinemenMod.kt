package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventReceivePacket
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import net.minecraft.network.play.server.S02PacketChat

class MinemenMod : Mod(TranslateText.MINEMEN, TranslateText.MINEMEN_DESCRIPTION, ModCategory.OTHER, Shinconic.MOD_MINEMEN) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.AUTO_PLAY)
    private val autoPlaySetting = false

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        if (autoPlaySetting && event.getPacket() is S02PacketChat) {
            val chatPacket = event.getPacket() as S02PacketChat
            val raw = chatPacket.chatComponent.toString()

            if (raw.contains("clickEvent=ClickEvent{action=RUN_COMMAND, value='/requeue")) {
                mc.thePlayer.sendChatMessage("/requeue")
            }
        }
    }
}
