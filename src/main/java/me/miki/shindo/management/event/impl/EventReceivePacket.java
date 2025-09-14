package me.miki.shindo.management.event.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.management.event.Event;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;

@Getter
@Setter
public class EventReceivePacket extends Event {

    private Packet<?> packet;

    public EventReceivePacket(Packet<?> packet) {
        this.packet = packet;
    }

}