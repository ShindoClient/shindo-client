package com.shindoclient.shindo.discord.ipc

import com.google.gson.JsonObject
import com.shindoclient.shindo.discord.ipc.entities.Packet
import com.shindoclient.shindo.discord.ipc.entities.User

interface IPCListener {
    fun onPacketSent(
        client: IPCClient,
        packet: Packet,
    ) {}

    fun onPacketReceived(
        client: IPCClient,
        packet: Packet,
    ) {}

    fun onActivityJoin(
        client: IPCClient,
        secret: String,
    ) {}

    fun onActivitySpectate(
        client: IPCClient,
        secret: String,
    ) {}

    fun onActivityJoinRequest(
        client: IPCClient,
        secret: String?,
        user: User,
    ) {}

    fun onReady(client: IPCClient) {}

    fun onClose(
        client: IPCClient,
        json: JsonObject,
    ) {}

    fun onDisconnect(
        client: IPCClient,
        t: Throwable,
    ) {}
}
