package me.miki.shindo.api.websocket

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.miki.shindo.logger.ShindoLogger
import okhttp3.*
import okio.ByteString
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class WsClient(
    private val url: String,
    private val httpClient: OkHttpClient
) {

    private val listeners: MutableList<WsClientListener> = CopyOnWriteArrayList()
    private val open = AtomicBoolean(false)
    private val socketRef = AtomicReference<WebSocket?>(null)

    // Messages queued before the socket handshake completes.
    private val outbox: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()

    fun addListener(l: WsClientListener?) {
        if (l != null) listeners.add(l)
    }

    fun isOpenAtomic(): Boolean = open.get()

    /** Opens the WebSocket connection asynchronously. */
    fun connect() {
        val request = Request.Builder().url(url).build()
        httpClient.newWebSocket(request, InternalListener())
    }

    /**
     * Sends [json] immediately if the socket is open, or enqueues it for
     * delivery once the handshake completes.
     */
    fun sendJson(json: JsonObject) {
        val text = json.toString()
        val ws = socketRef.get()
        if (ws != null && open.get()) {
            ws.send(text)
        } else {
            outbox.offer(text)
        }
    }

    /**
     * Initiates a graceful close (code 1000).
     * Safe to call even if the socket is already closed.
     */
    fun close() {
        socketRef.get()?.close(NORMAL_CLOSE_CODE, "client_disconnect")
        open.set(false)
    }

    /** Cancels the socket immediately without a graceful handshake. */
    fun cancel() {
        socketRef.get()?.cancel()
        open.set(false)
    }

    private inner class InternalListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            socketRef.set(webSocket)
            open.set(true)

            // Drain any messages that arrived before the handshake completed.
            while (outbox.isNotEmpty()) {
                val queued = outbox.poll() ?: break
                webSocket.send(queued)
            }

            for (l in listeners) safeCall { l.onOpen() }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val obj = JsonParser.parseString(text).asJsonObject
                val type = if (obj.has("type")) obj.get("type").asString else "unknown"
                for (l in listeners) safeCall { l.onMessage(type, obj) }
            } catch (e: Exception) {
                ShindoLogger.error("[WEBSOCKET] Exception!", e)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            // Binary frames are not used by the Shindo protocol.
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            // Acknowledge the server-initiated close.
            webSocket.close(NORMAL_CLOSE_CODE, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            open.set(false)
            socketRef.compareAndSet(webSocket, null)
            for (l in listeners) safeCall { l.onClose(code, reason, true) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            open.set(false)
            socketRef.compareAndSet(webSocket, null)
            val ex = if (t is Exception) t else RuntimeException(t)
            for (l in listeners) safeCall { l.onError(ex) }
            // Treat failure as a close so ShindoWebsocket can trigger reconnect.
            for (l in listeners) safeCall { l.onClose(FAILURE_CLOSE_CODE, t.message ?: "failure", false) }
        }
    }

    private inline fun safeCall(block: () -> Unit) {
        try {
            block()
        } catch (ignored: Exception) {
        }
    }

    internal interface WsClientListener {
        fun onOpen()
        fun onMessage(type: String, payload: JsonObject)
        fun onClose(code: Int, reason: String, remote: Boolean)
        fun onError(ex: Exception)
    }

    companion object {
        private const val NORMAL_CLOSE_CODE = 1000
        private const val FAILURE_CLOSE_CODE = -1
    }
}
