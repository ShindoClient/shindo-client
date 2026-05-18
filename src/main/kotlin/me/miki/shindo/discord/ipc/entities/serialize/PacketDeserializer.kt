package me.miki.shindo.discord.ipc.entities.serialize

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import me.miki.shindo.discord.ipc.entities.Packet
import java.lang.reflect.Type

class PacketDeserializer(
    private val op: Packet.OpCode,
) : JsonDeserializer<Packet> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Packet {
        val jsonObject = json.asJsonObject
        jsonObject.entrySet().removeIf { it.value.isJsonNull }
        return Packet(op, jsonObject)
    }
}
