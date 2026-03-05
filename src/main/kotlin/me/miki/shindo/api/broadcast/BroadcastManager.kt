package me.miki.shindo.api.broadcast

import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.api.roles.Role
import me.miki.shindo.api.roles.RoleManager
import me.miki.shindo.api.websocket.message.MessageType
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.utils.network.HttpUtils
import net.minecraft.client.Minecraft
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue

class BroadcastManager {

    private val instance = Shindo.getInstance()
    private val broadcasts = LinkedBlockingQueue<BroadcastNotification>()
    private val handler = BroadcastHandler(broadcasts)

    private val tokenLock = Any()
    private val pendingTokenCallbacks = CopyOnWriteArrayList<(String?) -> Unit>()

    @Volatile
    private var broadcastToken: String? = null

    @Volatile
    private var broadcastTokenExpiresAt = 0L

    @Volatile
    private var tokenRequestInFlight = false

    @Volatile
    private var tokenRequestedAt = 0L

    init {
        instance.eventManager.register(handler)
    }

    fun handleMessage(type: MessageType, payload: JsonObject?) {
        if (payload == null) return
        when (type) {
            MessageType.BROADCAST -> {
                val title = payload.get("title")?.asString ?: return
                val message = payload.get("message")?.asString ?: return
                val severity = payload.get("severity")?.asString ?: "info"
                enqueue(title, message, mapSeverity(severity))
            }
            MessageType.BROADCAST_TOKEN_OK -> {
                val token = payload.get("token")?.asString ?: return
                val expires = payload.get("expiresIn")?.asLong ?: 0L
                storeBroadcastToken(token, expires)
            }
            else -> { }
        }
    }

    fun sendBroadcast(
        title: String,
        message: String,
        severity: String = "info",
        onResult: ((Boolean) -> Unit)? = null
    ) {
        if (!isStaff()) {
            onResult?.invoke(false)
            return
        }
        val cleanTitle = title.trim()
        val cleanMessage = message.trim()
        if (cleanTitle.isEmpty() || cleanMessage.isEmpty()) {
            onResult?.invoke(false)
            return
        }
        val payload = JsonObject()
        payload.addProperty("title", cleanTitle)
        payload.addProperty("message", cleanMessage)
        payload.addProperty("severity", severity.toLowerCase(Locale.ROOT))
        postBroadcast(payload) { response ->
            onResult?.invoke(response?.get("success")?.asBoolean == true)
        }
    }

    private fun enqueue(title: String, message: String, type: NotificationType) {
        broadcasts.add(BroadcastNotification(title, message, type))
    }

    private fun postBroadcast(payload: JsonObject, onResponse: (JsonObject?) -> Unit) {
        withBroadcastToken { token ->
            if (token.isNullOrEmpty()) {
                dispatch { onResponse(null) }
                return@withBroadcastToken
            }
            Thread {
                val response = HttpUtils.postJson(
                    "$BROADCAST_API_BASE/v1/broadcast/message",
                    payload,
                    mapOf("Authorization" to "Bearer $token")
                )
                dispatch { onResponse(response) }
            }.apply {
                isDaemon = true
                start()
            }
        }
    }

    private fun withBroadcastToken(onToken: (String?) -> Unit) {
        val now = System.currentTimeMillis()
        val cached = broadcastToken
        if (cached != null && now < broadcastTokenExpiresAt - TOKEN_SAFETY_MS) {
            onToken(cached)
            return
        }
        val ws = instance.shindoAPI.ws
        if (ws == null || !ws.isOpen()) {
            onToken(null)
            return
        }
        synchronized(tokenLock) {
            val current = broadcastToken
            if (current != null && now < broadcastTokenExpiresAt - TOKEN_SAFETY_MS) {
                onToken(current)
                return
            }
            pendingTokenCallbacks.add(onToken)
            if (!tokenRequestInFlight || now - tokenRequestedAt > TOKEN_REQUEST_TIMEOUT_MS) {
                tokenRequestInFlight = true
                tokenRequestedAt = now
                ws.send(MessageType.BROADCAST_TOKEN, JsonObject())
            }
        }
    }

    private fun storeBroadcastToken(token: String, expiresInSeconds: Long) {
        val now = System.currentTimeMillis()
        val waiters = mutableListOf<(String?) -> Unit>()
        synchronized(tokenLock) {
            broadcastToken = token
            broadcastTokenExpiresAt = now + expiresInSeconds * 1000L
            tokenRequestInFlight = false
            tokenRequestedAt = 0L
            waiters.addAll(pendingTokenCallbacks)
            pendingTokenCallbacks.clear()
        }
        for (waiter in waiters) {
            waiter(token)
        }
    }

    private fun dispatch(task: () -> Unit) {
        Minecraft.getMinecraft().addScheduledTask { task() }
    }

    private fun isStaff(): Boolean {
        val uuid = instance.shindoAPI.getEffectiveUuid()
        return RoleManager.hasAtLeast(uuid, Role.STAFF)
    }

    private fun mapSeverity(raw: String): NotificationType {
        return when (raw.trim().toLowerCase(Locale.ROOT)) {
            "success" -> NotificationType.SUCCESS
            "error" -> NotificationType.ERROR
            "warning" -> NotificationType.WARNING
            else -> NotificationType.INFO
        }
    }

    companion object {
        private const val BROADCAST_API_BASE = "https://ws.shindoclient.com"
        private const val TOKEN_SAFETY_MS = 30_000L
        private const val TOKEN_REQUEST_TIMEOUT_MS = 10_000L
    }
}
