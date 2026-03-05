@file:JvmName("JavaWebSocketExtensions")

package me.miki.extensions.network.websocket

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

/**
 * Sends a JSON object payload when socket is open.
 */
fun WebSocketClient.sendJsonObject(payload: JsonObject): Boolean {
    if (!isOpen) return false
    return try {
        send(payload.toString())
        true
    } catch (ignored: Throwable) {
        false
    }
}

/**
 * Serializes payload with Gson and sends it when socket is open.
 */
fun WebSocketClient.sendJson(gson: Gson, payload: Any): Boolean {
    if (!isOpen) return false
    return try {
        send(gson.toJson(payload))
        true
    } catch (ignored: Throwable) {
        false
    }
}

/**
 * Closes websocket without propagating errors.
 */
fun WebSocketClient.closeQuietly(code: Int = 1000, reason: String = "normal"): Unit {
    try {
        close(code, reason)
    } catch (ignored: Throwable) {
    }
}

fun ServerHandshake?.statusCodeOr(defaultValue: Int = -1): Int {
    return this?.httpStatus?.toInt() ?: defaultValue
}
