package com.shindoclient.shindo.management.security.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventReceivePacket
import com.shindoclient.shindo.management.security.SecurityFeature
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
