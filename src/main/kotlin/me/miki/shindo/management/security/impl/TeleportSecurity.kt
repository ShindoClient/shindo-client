package me.miki.shindo.management.security.impl

import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.security.SecurityFeature
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class TeleportSecurity : SecurityFeature() {

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        val pkt = event.getPacket()
        if (pkt is S08PacketPlayerPosLook) {
            if (kotlin.math.abs(pkt.x) > 1E9 || kotlin.math.abs(pkt.y) > 1E9 || kotlin.math.abs(pkt.z) > 1E9) {
                event.setCancelled(true)
            }
        }
    }
}
