package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.network.Packet

class EventSendPacket(
    private var packet: Packet<*>,
) : Event() {
    fun getPacket(): Packet<*> = packet

    fun setPacket(packet: Packet<*>) {
        this.packet = packet
    }
}
