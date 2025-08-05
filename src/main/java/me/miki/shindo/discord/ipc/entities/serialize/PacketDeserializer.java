package me.miki.shindo.discord.ipc.entities.serialize;

import com.google.gson.*;
import me.miki.shindo.discord.ipc.entities.Packet;

import java.lang.reflect.Type;

public class PacketDeserializer implements JsonDeserializer<Packet> {

    private final Packet.OpCode op;

    public PacketDeserializer(Packet.OpCode op) {
        this.op = op;
    }

    @Override
    public Packet deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        jsonObject.entrySet().removeIf(entry -> entry.getValue().isJsonNull());
        return new Packet(op, jsonObject);
    }
}