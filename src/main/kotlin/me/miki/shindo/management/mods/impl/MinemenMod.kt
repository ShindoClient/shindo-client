package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
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
