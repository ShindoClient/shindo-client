package me.miki.shindo.api.websocket.message

import com.google.gson.JsonObject
import me.miki.shindo.api.websocket.presence.PresenceTracker
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.BiConsumer

class MessageHandler(private val presenceTracker: PresenceTracker?) {

    private val observers: MutableList<BiConsumer<MessageType, JsonObject?>> = CopyOnWriteArrayList()

    fun addObserver(observer: BiConsumer<MessageType, JsonObject?>) {
        observers.add(observer)
    }

    fun handle(rawType: String?, payload: JsonObject?) {
        val type = MessageType.fromWire(rawType)
        if (!MessageValidator.isValid(type, payload)) return
        routeInternal(type, payload)
        for (observer in observers) {
            try {
                observer.accept(type, payload)
            } catch (ignored: Exception) {
            }
        }
    }

    private fun routeInternal(type: MessageType, payload: JsonObject?) {
        if (presenceTracker == null || payload == null) return
        when (type) {
            MessageType.USER_JOIN,
            MessageType.USER_LEAVE,
            MessageType.USER_ROLES -> presenceTracker.handleMessage(type.wireType, payload)
            else -> { }
        }
    }
}
