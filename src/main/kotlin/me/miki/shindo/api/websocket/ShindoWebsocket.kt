package me.miki.shindo.api.websocket

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.miki.shindo.api.roles.RoleManager
import me.miki.shindo.api.websocket.message.MessageHandler
import me.miki.shindo.api.websocket.message.MessageType
import me.miki.shindo.api.websocket.presence.PresenceTracker
import me.miki.shindo.logger.FileLogWriter
import okhttp3.Handshake
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import kotlin.math.pow

class ShindoWebsocket(
    private val url: String,
    presenceTracker: PresenceTracker? = null
) {

    @Suppress("UNUSED_PARAMETER")
    constructor(
        uri: java.net.URI,
        ssl: Boolean,
        presenceTracker: PresenceTracker? = null
    ) : this(uri.toString(), presenceTracker)

    val messageHandler: MessageHandler = MessageHandler(presenceTracker)

    var provider: IdentityProvider? = null
    var presenceTracker: PresenceTracker? = presenceTracker
    var roleManager: RoleManager? = null

    private val listeners = CopyOnWriteArrayList<Listener>()
    private val clientRef = AtomicReference<WsClient?>(null)
    private val lastRolesSent = AtomicReference<List<String>>(Collections.emptyList())

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "shindo-ws").apply { isDaemon = true }
    }

    private val stopRequested = AtomicBoolean(false)
    private val reconnectAttempts = AtomicInteger(0)
    private val lastHeartbeatAck = AtomicLong(0L)
    private val heartbeatFuture = AtomicReference<ScheduledFuture<*>?>(null)
    private val reconnectFuture = AtomicReference<ScheduledFuture<*>?>(null)

    fun addListener(l: Listener?) {
        if (l != null) listeners.add(l)
    }

    fun connect() {
        stopRequested.set(false)
        reconnectAttempts.set(0)
        cancelReconnect()
        establishClient()
    }

    fun disconnect() {
        stopRequested.set(true)
        stopHeartbeat()
        cancelReconnect()
        lastRolesSent.set(Collections.emptyList())
        lastHeartbeatAck.set(0L)
        closeClient(clientRef.getAndSet(null))
    }

    fun isOpen(): Boolean {
        val c = clientRef.get()
        return c != null && c.isOpenAtomic()
    }

    fun send(type: MessageType?, payload: JsonObject?) {
        val c = clientRef.get()
        if (c == null || type == null) return
        val obj = payload ?: JsonObject()
        obj.addProperty("type", type.wireType)
        //FileLogWriter.websocket("send type=" + type.wireType)
        c.sendJson(obj)
    }

    fun reauthenticate() {
        if (stopRequested.get()) return
        if (!isOpen()) {
            scheduleReconnect("reauth_requested")
            return
        }
        authenticate()
    }

    fun pushRoles(roles: Array<String>?) {
        val normalized = normalizeRoles(roles)
        val normalizedList = listOf(*normalized)
        if (lastRolesSent.get() == normalizedList) return

        val payload = JsonObject()
        val array = JsonArray()
        for (role in normalized) array.add(role)
        payload.add("roles", array)
        send(MessageType.ROLES_UPDATE, payload)
        lastRolesSent.set(normalizedList)
    }

    private fun establishClient() {
        if (stopRequested.get()) return

        val existing = clientRef.get()
        if (existing != null && existing.isOpenAtomic()) return
        closeClient(existing)

        val c = WsClient(url, WsHttpClientProvider.instance)
        c.addListener(object : WsClient.WsClientListener {

            override fun onOpen() {
                lastHeartbeatAck.set(System.currentTimeMillis())
                reconnectAttempts.set(0)
                cancelReconnect()
                authenticate()
                startHeartbeat()
                //FileLogWriter.websocket("open url=$url")
                notifyListeners(Consumer { l -> l.onOpen(null) })
            }

            override fun onMessage(type: String, payload: JsonObject) {
                handleServerMessage(type, payload)
                //FileLogWriter.websocket("recv type=$type")
                notifyListeners(Consumer { l -> l.onMessage(type, payload) })
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                stopHeartbeat()
                clientRef.compareAndSet(c, null)
                if (!stopRequested.get()) scheduleReconnect("close:$code")
                //FileLogWriter.websocket("close code=$code reason=$reason remote=$remote")
                notifyListeners(Consumer { l -> l.onClose(code, reason, remote) })
            }

            override fun onError(ex: Exception) {
                FileLogWriter.websocket("error ${ex.javaClass.simpleName} ${ex.message}")
                notifyListeners(Consumer { l -> l.onError(ex) })
            }
        })

        clientRef.set(c)
        c.connect()
    }

    private fun authenticate() {
        if (stopRequested.get()) return
        val current = fetchCurrentPlayer() ?: return
        sendAuthPayload(current)
    }

    private fun fetchCurrentPlayer(): WsIdentity? {
        if (provider == null) return null
        val raw = provider?.player() ?: return null
        return sanitizeIdentity(raw)
    }

    private fun sendAuthPayload(info: WsIdentity) {
        val outgoingRoles = normalizeRoles(info.roles)
        val payload = JsonObject()
        payload.addProperty("uuid", info.uuid)
        payload.addProperty("name", info.name)
        payload.addProperty("accountType", info.accountType.getWireValue())
        val rolesArr = JsonArray()
        for (role in outgoingRoles) rolesArr.add(role)
        payload.add("roles", rolesArr)
        lastRolesSent.set(listOf(*outgoingRoles))
        send(MessageType.AUTH, payload)
    }

    private fun handleServerMessage(rawType: String, payload: JsonObject?) {
        val type = MessageType.fromWire(rawType)
        if (type != MessageType.UNKNOWN) recordHeartbeat()

        when (type) {
            MessageType.PONG -> return

            MessageType.SERVER_KEEPALIVE -> {
                send(MessageType.PING, JsonObject())
                return
            }

            MessageType.SERVER_VERIFY -> {
                send(MessageType.PING, JsonObject())
                return
            }

            else -> {}
        }

        if (type == MessageType.AUTH_OK &&
            payload != null &&
            payload.has("roles") &&
            payload.get("roles").isJsonArray
        ) {
            val arr = payload.getAsJsonArray("roles")
            val roles = Array(arr.size()) { idx -> arr[idx].asString }
            lastRolesSent.set(listOf(*normalizeRoles(roles)))
        }

        messageHandler.handle(rawType, payload)
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatFuture.set(
            scheduler.scheduleAtFixedRate({
                val client = clientRef.get()
                if (client == null || !client.isOpenAtomic() || stopRequested.get()) return@scheduleAtFixedRate
                val now = System.currentTimeMillis()
                val lastAck = lastHeartbeatAck.get()
                if (lastAck > 0 && now - lastAck > HEARTBEAT_TIMEOUT_MS) {
                    //FileLogWriter.websocket("heartbeat_timeout – forcing reconnect")
                    closeClient(client)
                    scheduleReconnect("heartbeat_timeout")
                    return@scheduleAtFixedRate
                }
                send(MessageType.PING, JsonObject())
            }, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS)
        )
    }

    private fun stopHeartbeat() {
        heartbeatFuture.getAndSet(null)?.cancel(true)
    }

    private fun recordHeartbeat() {
        lastHeartbeatAck.set(System.currentTimeMillis())
    }

    private fun scheduleReconnect(reason: String) {
        if (stopRequested.get()) return
        val existing = reconnectFuture.get()
        if (existing != null && !existing.isDone) return
        val attempt = reconnectAttempts.incrementAndGet().coerceAtLeast(1)
        val delay = RECONNECT_MAX_MS.coerceAtMost(
            (RECONNECT_BASE_MS * 2.0.pow((attempt - 1).toDouble())).toLong()
        )
        //FileLogWriter.websocket("reconnect scheduled reason=$reason attempt=$attempt delay=${delay}ms")
        reconnectFuture.set(
            scheduler.schedule({ establishClient() }, delay, TimeUnit.MILLISECONDS)
        )
    }

    private fun cancelReconnect() {
        reconnectFuture.getAndSet(null)?.cancel(true)
    }

    private fun sanitizeIdentity(info: WsIdentity): WsIdentity {
        val uuid = info.uuid.trim()
        val name = info.name.trim()
        return WsIdentity(uuid, name, normalizeRoles(info.roles), info.accountType)
    }

    private fun normalizeRoles(roles: Array<String>?): Array<String> {
        if (roles.isNullOrEmpty()) return arrayOf(DEFAULT_ROLE)
        val set = HashSet<String>()
        for (role in roles) {
            val normalized = role.trim().toUpperCase(Locale.ROOT)
            if (ALLOWED_ROLES.contains(normalized)) set.add(normalized)
        }
        if (set.isEmpty()) set.add(DEFAULT_ROLE)
        return set.toTypedArray()
    }

    private fun closeClient(client: WsClient?) {
        if (client == null) return
        try {
            client.close()
        } catch (ignored: Exception) {
        }
    }

    private fun notifyListeners(consumer: Consumer<Listener>) {
        for (listener in listeners) {
            try {
                consumer.accept(listener)
            } catch (ignored: Exception) {
            }
        }
    }

    interface Listener {
        fun onOpen(handshake: Handshake?) {}
        fun onClose(code: Int, reason: String, remote: Boolean) {}
        fun onError(ex: Exception) {}
        fun onMessage(type: String, payload: JsonObject) {}
    }

    interface IdentityProvider {
        fun player(): WsIdentity?
    }

    companion object {
        private val ALLOWED_ROLES: Set<String> = Collections.unmodifiableSet(
            HashSet(listOf("STAFF", "NETHERITE", "EMERALD", "DIAMOND", "GOLD", "MEMBER"))
        )
        private const val DEFAULT_ROLE = "MEMBER"
        private const val HEARTBEAT_INTERVAL_MS = 20_000L
        private const val HEARTBEAT_TIMEOUT_MS = 45_000L
        private const val RECONNECT_BASE_MS = 2_000L
        private const val RECONNECT_MAX_MS = 30_000L
    }
}
