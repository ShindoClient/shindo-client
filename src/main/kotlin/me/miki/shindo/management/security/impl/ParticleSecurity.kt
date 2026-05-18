package me.miki.shindo.management.security.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.security.SecurityFeature
import net.minecraft.network.play.server.S2APacketParticles

class ParticleSecurity : SecurityFeature() {
    private var particles: Int = 0

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        val pkt = event.getPacket()
        if (pkt is S2APacketParticles) {
            particles += pkt.particleCount
            particles -= 6
            particles = minOf(particles, 150)
            if (particles > 100 ||
                pkt.particleCount < 1 ||
                kotlin.math.abs(pkt.particleCount) > 20 ||
                pkt.particleSpeed < 0f ||
                pkt.particleSpeed > 1000f
            ) {
                event.setCancelled(true)
            }
        }
    }
}
