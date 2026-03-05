package me.miki.shindo.api.websocket

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

internal class WsClient(
    serverUri: URI,
    private val ssl: Boolean
) : WebSocketClient(serverUri) {

    private val listeners: MutableList<WsClientListener> = CopyOnWriteArrayList()
    private val open = AtomicBoolean(false)
    private val outbox: ConcurrentLinkedQueue<JsonObject> = ConcurrentLinkedQueue()

    init {
        if (ssl && serverUri.toString().startsWith("wss://")) {
            try {
                val context = SSLContext.getInstance("TLS")
                context.init(null, null, null)
                val factory: SSLSocketFactory = context.socketFactory
                setSocketFactory(factory)
            } catch (ignored: Exception) {
            }
        }
        connectionLostTimeout = 0
    }

    fun addListener(l: WsClientListener?) {
        if (l != null) listeners.add(l)
    }

    fun isOpenAtomic(): Boolean = open.get()

    fun sendJson(json: JsonObject) {
        if (isOpen) {
            super.send(json.toString())
        } else {
            outbox.offer(json)
        }
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        open.set(true)
        while (!outbox.isEmpty()) {
            val o = outbox.poll()
            if (o != null) super.send(o.toString())
        }
        for (l in listeners) {
            try {
                l.onOpen()
            } catch (ignored: Exception) {
            }
        }
    }

    override fun onMessage(message: String?) {
        if (message == null) return
        try {
            val obj = JsonParser.parseString(message).asJsonObject
            val type = if (obj.has("type")) obj.get("type").asString else "unknown"
            for (l in listeners) {
                try {
                    l.onMessage(type, obj)
                } catch (ignored: Exception) {
                }
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onMessage(bytes: ByteBuffer?) {

    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        open.set(false)
        for (l in listeners) {
            try {
                l.onClose(code, reason ?: "", remote)
            } catch (ignored: Exception) {
            }
        }
    }

    override fun onError(ex: Exception) {
        for (l in listeners) {
            try {
                l.onError(ex)
            } catch (ignored: Exception) {
            }
        }
    }

    internal interface WsClientListener {
        fun onOpen()
        fun onMessage(type: String, payload: JsonObject)
        fun onClose(code: Int, reason: String, remote: Boolean)
        fun onError(ex: Exception)
    }
}
