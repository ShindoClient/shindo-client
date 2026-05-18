package me.miki.shindo.management.security.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.security.SecurityFeature
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
