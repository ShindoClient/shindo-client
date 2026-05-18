package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.network.Packet

class EventReceivePacket(
    private var _packet: Packet<*>,
) : Event() {
    fun getPacket(): Packet<*> = _packet

    fun setPacket(packet: Any) {
        _packet = packet as Packet<*>
    }
}
