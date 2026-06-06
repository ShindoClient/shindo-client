package com.shindoclient.shindo.management.security.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventReceivePacket
import com.shindoclient.shindo.management.security.SecurityFeature
import net.minecraft.network.play.server.S27PacketExplosion

class ExplosionSecurity : SecurityFeature() {
    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        val pkt = event.getPacket()
        if (pkt is S27PacketExplosion) {
            if (pkt.func_149149_c() >= Byte.MAX_VALUE || pkt.func_149144_d() >= Byte.MAX_VALUE) {
                event.setCancelled(true)
            }
        }
    }
}
