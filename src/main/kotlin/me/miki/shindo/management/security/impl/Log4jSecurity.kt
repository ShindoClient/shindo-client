package me.miki.shindo.management.security.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.event.impl.EventSendChat
import me.miki.shindo.management.security.SecurityFeature
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import java.util.regex.Pattern

class Log4jSecurity : SecurityFeature() {

    private val pattern = Pattern.compile(".*\\$\\{[^}]*\\}.*")

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        when (val pkt = event.getPacket()) {
            is S29PacketSoundEffect -> {
                if (pattern.matcher(pkt.soundName).matches()) event.setCancelled(true)
            }
            is S02PacketChat -> {
                val component = pkt.chatComponent
                if (pattern.matcher(component.unformattedText).matches() || pattern.matcher(component.formattedText).matches()) {
                    event.setCancelled(true)
                }
            }
            else -> { }
        }
    }

    @EventTarget
    fun onChat(event: EventSendChat) {
        if (pattern.matcher(event.getMessage()).matches()) event.setCancelled(true)
    }
}
