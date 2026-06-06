package com.shindoclient.shindo.management.addons.bridge

import com.shindoclient.addon.api.event.EventManager
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventClickMouse
import com.shindoclient.shindo.management.event.impl.EventJoinServer
import com.shindoclient.shindo.management.event.impl.EventKey
import com.shindoclient.shindo.management.event.impl.EventLeaveServer
import com.shindoclient.shindo.management.event.impl.EventLoadWorld
import com.shindoclient.shindo.management.event.impl.EventReceiveChat
import com.shindoclient.shindo.management.event.impl.EventReceivePacket
import com.shindoclient.shindo.management.event.impl.EventRender2D
import com.shindoclient.shindo.management.event.impl.EventScrollMouse
import com.shindoclient.shindo.management.event.impl.EventSendPacket
import com.shindoclient.shindo.management.event.impl.EventTick

class AddonEventBridge(
    private val addonEventManager: EventManager,
) {
    @EventTarget
    fun onClientTick(event: EventTick) {
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl
                .TickEvent(),
        )
    }

    @EventTarget
    fun onRender2D(event: EventRender2D) {
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl
                .Render2DEvent(event.partialTicks),
        )
    }

    @EventTarget
    fun onKey(event: EventKey) {
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl
                .KeyEvent(event.getKeyCode(), 0),
        )
    }

    @EventTarget
    fun onChat(event: EventReceiveChat) {
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl
                .ChatEvent(event.getMessage().formattedText),
        )
    }

    @EventTarget
    fun onMouseClick(event: EventClickMouse) {
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl.MouseEvent(
                type = com.shindoclient.addon.api.event.impl.MouseEvent.Type.CLICK,
                button = event.getButton(),
            ),
        )
    }

    @EventTarget
    fun onMouseScroll(event: EventScrollMouse) {
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl.MouseEvent(
                type = com.shindoclient.addon.api.event.impl.MouseEvent.Type.SCROLL,
                scrollAmount = event.getAmount(),
            ),
        )
    }

    @EventTarget
    fun onSendPacket(event: EventSendPacket) {
        val packet = event.getPacket()
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl.PacketEvent(
                type = com.shindoclient.addon.api.event.impl.PacketEvent.Type.SEND,
                packetClass = packet.javaClass.name,
                packetString = packet.toString(),
            ),
        )
    }

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        val packet = event.getPacket()
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl.PacketEvent(
                type = com.shindoclient.addon.api.event.impl.PacketEvent.Type.RECEIVE,
                packetClass = packet.javaClass.name,
                packetString = packet.toString(),
            ),
        )
    }

    @EventTarget
    fun onLoadWorld(event: EventLoadWorld) {
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl.WorldEvent(
                type = com.shindoclient.addon.api.event.impl.WorldEvent.Type.LOAD,
            ),
        )
    }

    @EventTarget
    fun onJoinServer(event: EventJoinServer) {
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl.WorldEvent(
                type = com.shindoclient.addon.api.event.impl.WorldEvent.Type.JOIN_SERVER,
                serverIp = event.getIp(),
            ),
        )
    }

    @EventTarget
    fun onLeaveServer(event: EventLeaveServer) {
        addonEventManager.call(
            com.shindoclient.addon.api.event.impl.WorldEvent(
                type = com.shindoclient.addon.api.event.impl.WorldEvent.Type.LEAVE_SERVER,
            ),
        )
    }
}
