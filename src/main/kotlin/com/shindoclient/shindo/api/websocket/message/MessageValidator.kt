package com.shindoclient.shindo.api.websocket.message

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.UUID

object MessageValidator {
    @JvmStatic
    fun isValid(
        type: MessageType?,
        payload: JsonObject?,
    ): Boolean {
        if (type == null) return false
        if (payload == null) {
            return type == MessageType.PONG ||
                type == MessageType.SERVER_KEEPALIVE ||
                type == MessageType.SERVER_VERIFY ||
                type == MessageType.UNKNOWN
        }

        return when (type) {
            MessageType.AUTH_OK, MessageType.AUTH_ERROR -> {
                hasString(payload, "uuid")
            }

            MessageType.USER_JOIN, MessageType.USER_LEAVE, MessageType.USER_ROLES -> {
                hasNonEmptyUuid(payload)
            }

            MessageType.PING, MessageType.PONG, MessageType.SERVER_KEEPALIVE, MessageType.SERVER_VERIFY,
            MessageType.WARP_STATUS,
            -> {
                true
            }

            MessageType.ROLES_UPDATE -> {
                val roles: JsonElement? = payload.get("roles")
                roles == null || roles.isJsonArray
            }

            MessageType.PROFILE_SHARE_OK -> {
                hasString(payload, "requestId") && hasString(payload, "code")
            }

            MessageType.PROFILE_SHARE_ERROR -> {
                hasString(payload, "requestId")
            }

            MessageType.PROFILE_FETCH_OK -> {
                hasString(payload, "requestId") && hasString(payload, "profile")
            }

            MessageType.PROFILE_FETCH_ERROR -> {
                hasString(payload, "requestId")
            }

            MessageType.PROFILE_UNSHARE_OK -> {
                hasString(payload, "requestId") && hasString(payload, "code")
            }

            MessageType.PROFILE_UNSHARE_ERROR -> {
                hasString(payload, "requestId")
            }

            MessageType.CHAT_FRIEND_LIST -> {
                hasArray(payload, "friends")
            }

            MessageType.CHAT_FRIEND_REQUESTS -> {
                hasArray(payload, "requests")
            }

            MessageType.CHAT_FRIEND_REQUEST -> {
                hasString(payload, "uuid") && hasString(payload, "name")
            }

            MessageType.CHAT_FRIEND_REQUEST_OK -> {
                hasString(payload, "requestId") && hasString(payload, "targetUuid")
            }

            MessageType.CHAT_FRIEND_REQUEST_ERROR -> {
                hasString(payload, "requestId")
            }

            MessageType.CHAT_FRIEND_ACCEPT_OK -> {
                hasString(payload, "requestId") && hasString(payload, "friendUuid")
            }

            MessageType.CHAT_FRIEND_ACCEPT_ERROR -> {
                hasString(payload, "requestId")
            }

            MessageType.CHAT_FRIEND_REMOVE_OK -> {
                hasString(payload, "requestId") && hasString(payload, "friendUuid")
            }

            MessageType.CHAT_FRIEND_REMOVE_ERROR -> {
                hasString(payload, "requestId")
            }

            MessageType.CHAT_MESSAGE_SEND_OK -> {
                hasString(payload, "requestId")
            }

            MessageType.CHAT_MESSAGE -> {
                hasString(payload, "fromUuid") &&
                    hasString(payload, "fromName") &&
                    hasString(payload, "toUuid") &&
                    hasString(payload, "toName") &&
                    hasString(payload, "message")
            }

            MessageType.CHAT_MESSAGE_ERROR -> {
                hasString(payload, "requestId")
            }

            MessageType.CHAT_TOKEN_OK -> {
                hasString(payload, "token")
            }

            MessageType.BROADCAST_TOKEN_OK -> {
                hasString(payload, "token")
            }

            MessageType.BROADCAST -> {
                hasString(payload, "title") && hasString(payload, "message")
            }

            MessageType.AUTH -> {
                false
            }

            MessageType.PROFILE_SHARE, MessageType.PROFILE_FETCH, MessageType.PROFILE_UNSHARE,
            MessageType.CHAT_FRIEND_ACCEPT, MessageType.CHAT_FRIEND_REMOVE, MessageType.CHAT_MESSAGE_SEND,
            MessageType.CHAT_TOKEN, MessageType.BROADCAST_TOKEN,
            -> {
                false
            }

            MessageType.UNKNOWN -> {
                false
            }
        }
    }

    private fun hasString(
        obj: JsonObject,
        key: String,
    ): Boolean {
        val el = obj.get(key) ?: return false
        return el.isJsonPrimitive && el.asJsonPrimitive.isString && el.asString.trim().isNotEmpty()
    }

    private fun hasNonEmptyUuid(obj: JsonObject): Boolean {
        if (!hasString(obj, "uuid")) return false
        return try {
            UUID.fromString(obj.get("uuid").asString)
            true
        } catch (ignored: Exception) {
            false
        }
    }

    private fun hasArray(
        obj: JsonObject,
        key: String,
    ): Boolean {
        val el = obj.get(key)
        return el != null && el.isJsonArray
    }
}
