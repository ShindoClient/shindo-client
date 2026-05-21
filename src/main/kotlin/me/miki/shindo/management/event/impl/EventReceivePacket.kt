package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.network.Packet

class EventReceivePacket(
    private var packet: Packet<*>,
) : Event() {
    fun getPacket(): Packet<*> = packet

    fun setPacket(packet: Packet<*>) {
        this.packet = packet
    }
}
