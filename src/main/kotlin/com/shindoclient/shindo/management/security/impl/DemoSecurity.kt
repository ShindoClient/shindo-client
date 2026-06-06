package com.shindoclient.shindo.management.security.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventReceivePacket
import com.shindoclient.shindo.management.security.SecurityFeature
import net.minecraft.network.play.server.S2BPacketChangeGameState

class DemoSecurity : SecurityFeature() {
    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        val pkt = event.getPacket()
        if (pkt is S2BPacketChangeGameState) {
            if (pkt.gameState == 5 && pkt.func_149137_d() == 0f) {
                event.setCancelled(true)
            }
        }
    }
}
