package com.shindoclient.shindo.discord.ipc.entities

import com.google.gson.JsonObject
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class Packet(
    val op: OpCode,
    private val data: JsonObject,
) {
    fun toBytes(): ByteArray {
        val d = data.toString().toByteArray(StandardCharsets.UTF_8)
        val packet = ByteBuffer.allocate(d.size + 2 * Integer.BYTES)
        packet.putInt(Integer.reverseBytes(op.ordinal))
        packet.putInt(Integer.reverseBytes(d.size))
        packet.put(d)
        return packet.array()
    }

    fun json(): JsonObject = data

    override fun toString(): String = "Pkt:$op${json()}"

    enum class OpCode {
        HANDSHAKE,
        FRAME,
        CLOSE,
        PING,
        PONG,
    }
}
