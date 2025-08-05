package me.miki.shindo.management.security.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventReceivePacket;
import me.miki.shindo.management.security.SecurityFeature;
import net.minecraft.network.play.server.S2BPacketChangeGameState;

public class DemoSecurity extends SecurityFeature {

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getPacket() instanceof S2BPacketChangeGameState) {

            S2BPacketChangeGameState state = ((S2BPacketChangeGameState) event.getPacket());

            if (state.getGameState() == 5 && state.func_149137_d() == 0) {
                event.setCancelled(true);
            }
        }
    }
}