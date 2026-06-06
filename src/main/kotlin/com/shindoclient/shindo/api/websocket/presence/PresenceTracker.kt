package com.shindoclient.shindo.api.websocket.presence

import com.google.gson.JsonObject
import java.util.concurrent.ConcurrentHashMap

class PresenceTracker {
    fun handleMessage(
        type: String,
        payload: JsonObject,
    ) {
        when (type) {
            "user.join" -> {
                val uuid = payload.get("uuid").asString
                val name = if (payload.has("name")) payload.get("name").asString else "Unknown"
                val acct = if (payload.has("accountType")) payload.get("accountType").asString else "OFFLINE"
                online[uuid] = PresenceUser(uuid, name, acct, System.currentTimeMillis())
            }

            "user.leave" -> {
                val uuid = payload.get("uuid").asString
                online.remove(uuid)
            }
        }
    }

    fun allOnlineUuids(): Set<String> = online.keys

    fun get(uuid: String): PresenceUser? = online[uuid]

    fun clear() {
        online.clear()
    }

    companion object {
        private val online = ConcurrentHashMap<String, PresenceUser>()

        @JvmStatic
        fun isOnline(uuid: String): Boolean = online.containsKey(uuid)
    }
}
