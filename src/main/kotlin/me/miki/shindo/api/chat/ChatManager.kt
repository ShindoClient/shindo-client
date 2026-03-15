package me.miki.shindo.api.chat

import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.api.websocket.message.MessageType
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class ChatManager {

    private val instance = Shindo.getInstance()

    private val friends = CopyOnWriteArrayList<ChatFriend>()
    private val requests = CopyOnWriteArrayList<ChatRequest>()
    private val messagesByFriend = ConcurrentHashMap<String, MutableList<ChatMessage>>()

    private val pendingAccept = ConcurrentHashMap<String, () -> Unit>()
    private val pendingRemove = ConcurrentHashMap<String, () -> Unit>()
    private val pendingRequestFriend = ConcurrentHashMap<String, () -> Unit>()
    private val pendingSendMessage = ConcurrentHashMap<String, (MessageSendResult) -> Unit>()

    fun isFeatureAvailable(): Boolean {
        val ws = instance.shindoAPI.ws ?: return false
        return ws.isOpen()
    }

    fun getFriends(): List<ChatFriend> = Collections.unmodifiableList(ArrayList(friends))

    fun getRequests(): List<ChatRequest> = Collections.unmodifiableList(ArrayList(requests))

    fun getMessages(friendUuid: String): List<ChatMessage> {
        val list = messagesByFriend[friendUuid] ?: return emptyList()
        return Collections.unmodifiableList(ArrayList(list))
    }

    fun handleMessage(type: MessageType, payload: JsonObject?) {
        if (payload == null) return
        when (type) {
            MessageType.CHAT_FRIEND_LIST -> {
                friends.clear()
                if (!payload.has("friends") || !payload.get("friends").isJsonArray) return@handleMessage
                val arr = payload.getAsJsonArray("friends")
                for (i in 0 until arr.size()) {
                    val el = arr.get(i)
                    if (!el.isJsonObject) continue
                    val ob = el.asJsonObject
                    val uuid = ob.get("uuid")?.asString ?: continue
                    val name = ob.get("name")?.asString ?: "Unknown"
                    friends.add(ChatFriend(uuid, name))
                }
            }

            MessageType.CHAT_FRIEND_REQUESTS -> {
                requests.clear()
                if (!payload.has("requests") || !payload.get("requests").isJsonArray) return@handleMessage
                val arr = payload.getAsJsonArray("requests")
                for (i in 0 until arr.size()) {
                    val el = arr.get(i)
                    if (!el.isJsonObject) continue
                    val ob = el.asJsonObject
                    val uuid = ob.get("uuid")?.asString ?: continue
                    val name = ob.get("name")?.asString ?: "Unknown"
                    requests.add(ChatRequest(uuid, name))
                }
            }

            MessageType.CHAT_MESSAGE -> {
                val fromUuid = payload.get("fromUuid")?.asString ?: return
                val fromName = payload.get("fromName")?.asString ?: "Unknown"
                val toUuid = payload.get("toUuid")?.asString ?: return
                val message = payload.get("message")?.asString ?: return
                val selfUuid = instance.shindoAPI.getEffectiveUuid().toString()
                val msg = ChatMessage(fromUuid, fromName, message)
                val otherUuid = if (fromUuid == selfUuid) toUuid else fromUuid
                messagesByFriend.getOrPut(otherUuid) { Collections.synchronizedList(mutableListOf()) }.add(msg)
            }

            MessageType.CHAT_FRIEND_ACCEPT_OK -> {
                val requestId = payload.get("requestId")?.asString ?: return
                pendingAccept.remove(requestId)?.invoke()
            }

            MessageType.CHAT_FRIEND_ACCEPT_ERROR -> {
                val requestId = payload.get("requestId")?.asString ?: return
                pendingAccept.remove(requestId)?.invoke()
            }

            MessageType.CHAT_FRIEND_REMOVE_OK -> {
                val requestId = payload.get("requestId")?.asString ?: return
                pendingRemove.remove(requestId)?.invoke()
            }

            MessageType.CHAT_FRIEND_REMOVE_ERROR -> {
                val requestId = payload.get("requestId")?.asString ?: return
                pendingRemove.remove(requestId)?.invoke()
            }

            MessageType.CHAT_MESSAGE_SEND_OK -> {
                val requestId = payload.get("requestId")?.asString ?: return
                pendingSendMessage.remove(requestId)?.invoke(MessageSendResult.Success)
            }

            MessageType.CHAT_MESSAGE_ERROR -> {
                val requestId = payload.get("requestId")?.asString ?: return
                pendingSendMessage.remove(requestId)?.invoke(MessageSendResult.Error(null))
            }

            MessageType.CHAT_FRIEND_REQUEST_OK, MessageType.CHAT_FRIEND_REQUEST_ERROR -> {
                val requestId = payload.get("requestId")?.asString ?: return
                pendingRequestFriend.remove(requestId)?.invoke()
            }

            else -> {}
        }
    }

    fun acceptFriendRequest(uuid: String, callback: () -> Unit) {
        val ws = instance.shindoAPI.ws ?: run { callback(); return }
        if (!ws.isOpen()) {
            callback(); return
        }
        val requestId = UUID.randomUUID().toString()
        pendingAccept[requestId] = callback
        val payload = JsonObject()
        payload.addProperty("requestId", requestId)
        payload.addProperty("uuid", uuid)
        ws.send(MessageType.CHAT_FRIEND_ACCEPT, payload)
    }

    fun removeFriend(uuid: String, callback: () -> Unit) {
        val ws = instance.shindoAPI.ws ?: run { callback(); return }
        if (!ws.isOpen()) {
            callback(); return
        }
        val requestId = UUID.randomUUID().toString()
        pendingRemove[requestId] = callback
        val payload = JsonObject()
        payload.addProperty("requestId", requestId)
        payload.addProperty("friendUuid", uuid)
        ws.send(MessageType.CHAT_FRIEND_REMOVE, payload)
    }

    fun requestFriend(username: String, callback: () -> Unit) {
        val ws = instance.shindoAPI.ws ?: run { callback(); return }
        if (!ws.isOpen()) {
            callback(); return
        }
        val requestId = UUID.randomUUID().toString()
        pendingRequestFriend[requestId] = callback
        val payload = JsonObject()
        payload.addProperty("requestId", requestId)
        payload.addProperty("name", username.trim())
        payload.addProperty("uuid", instance.shindoAPI.getEffectiveUuid().toString())
        ws.send(MessageType.CHAT_FRIEND_REQUEST, payload)
    }

    fun sendMessage(friendUuid: String, text: String, callback: (MessageSendResult) -> Unit) {
        val ws = instance.shindoAPI.ws ?: run { callback(MessageSendResult.Error(null)); return }
        if (!ws.isOpen()) {
            callback(MessageSendResult.Error(null)); return
        }
        val requestId = UUID.randomUUID().toString()
        pendingSendMessage[requestId] = callback
        val payload = JsonObject()
        payload.addProperty("requestId", requestId)
        payload.addProperty("toUuid", friendUuid)
        payload.addProperty("message", text.trim())
        ws.send(MessageType.CHAT_MESSAGE_SEND, payload)
    }

    sealed class MessageSendResult {
        object Success : MessageSendResult()
        data class Error(val reason: String?) : MessageSendResult()
    }
}
