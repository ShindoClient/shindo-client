package me.miki.shindo.management.addons.bridge

import me.miki.addon.api.event.EventManager
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventClickMouse
import me.miki.shindo.management.event.impl.EventJoinServer
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.event.impl.EventLeaveServer
import me.miki.shindo.management.event.impl.EventLoadWorld
import me.miki.shindo.management.event.impl.EventReceiveChat
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventScrollMouse
import me.miki.shindo.management.event.impl.EventSendPacket
import me.miki.shindo.management.event.impl.EventTick

class AddonEventBridge(
    private val addonEventManager: EventManager,
) {
    @EventTarget
    fun onClientTick(event: EventTick) {
        addonEventManager.call(
            me.miki.addon.api.event.impl
                .TickEvent(),
        )
    }

    @EventTarget
    fun onRender2D(event: EventRender2D) {
        addonEventManager.call(
            me.miki.addon.api.event.impl
                .Render2DEvent(event.partialTicks),
        )
    }

    @EventTarget
    fun onKey(event: EventKey) {
        addonEventManager.call(
            me.miki.addon.api.event.impl
                .KeyEvent(event.getKeyCode(), 0),
        )
    }

    @EventTarget
    fun onChat(event: EventReceiveChat) {
        addonEventManager.call(
            me.miki.addon.api.event.impl
                .ChatEvent(event.getMessage().formattedText),
        )
    }

    @EventTarget
    fun onMouseClick(event: EventClickMouse) {
        addonEventManager.call(
            me.miki.addon.api.event.impl.MouseEvent(
                type = me.miki.addon.api.event.impl.MouseEvent.Type.CLICK,
                button = event.getButton(),
            ),
        )
    }

    @EventTarget
    fun onMouseScroll(event: EventScrollMouse) {
        addonEventManager.call(
            me.miki.addon.api.event.impl.MouseEvent(
                type = me.miki.addon.api.event.impl.MouseEvent.Type.SCROLL,
                scrollAmount = event.getAmount(),
            ),
        )
    }

    @EventTarget
    fun onSendPacket(event: EventSendPacket) {
        val packet = event.getPacket()
        addonEventManager.call(
            me.miki.addon.api.event.impl.PacketEvent(
                type = me.miki.addon.api.event.impl.PacketEvent.Type.SEND,
                packetClass = packet.javaClass.name,
                packetString = packet.toString(),
            ),
        )
    }

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        val packet = event.getPacket()
        addonEventManager.call(
            me.miki.addon.api.event.impl.PacketEvent(
                type = me.miki.addon.api.event.impl.PacketEvent.Type.RECEIVE,
                packetClass = packet.javaClass.name,
                packetString = packet.toString(),
            ),
        )
    }

    @EventTarget
    fun onLoadWorld(event: EventLoadWorld) {
        addonEventManager.call(
            me.miki.addon.api.event.impl.WorldEvent(
                type = me.miki.addon.api.event.impl.WorldEvent.Type.LOAD,
            ),
        )
    }

    @EventTarget
    fun onJoinServer(event: EventJoinServer) {
        addonEventManager.call(
            me.miki.addon.api.event.impl.WorldEvent(
                type = me.miki.addon.api.event.impl.WorldEvent.Type.JOIN_SERVER,
                serverIp = event.getIp(),
            ),
        )
    }

    @EventTarget
    fun onLeaveServer(event: EventLeaveServer) {
        addonEventManager.call(
            me.miki.addon.api.event.impl.WorldEvent(
                type = me.miki.addon.api.event.impl.WorldEvent.Type.LEAVE_SERVER,
            ),
        )
    }
}
