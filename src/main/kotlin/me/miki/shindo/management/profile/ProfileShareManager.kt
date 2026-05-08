package me.miki.shindo.management.profile

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.miki.shindo.Shindo
import me.miki.shindo.api.websocket.message.MessageType
import me.miki.shindo.logger.ShindoLogger
import net.minecraft.client.Minecraft
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ProfileShareManager {

    private val instance = Shindo.getInstance()
    private val pendingShare = ConcurrentHashMap<String, (ShareResult) -> Unit>()
    private val pendingFetch = ConcurrentHashMap<String, (FetchResult) -> Unit>()
    private val pendingUnshare = ConcurrentHashMap<String, (UnshareResult) -> Unit>()

    fun requestShare(profile: Profile, onResult: (ShareResult) -> Unit) {
        val ws = instance.getShindoAPI().ws
        if (ws == null || !ws.isOpen()) {
            onResult(ShareResult.Error("websocket_unavailable"))
            return
        }
        val file = profile.jsonFile
        val json = instance.getProfileManager().readProfileJson(file)
        if (json == null) {
            onResult(ShareResult.Error("profile_not_found"))
            return
        }
        val requestId = UUID.randomUUID().toString()
        pendingShare[requestId] = onResult

        val payload = JsonObject()
        payload.addProperty("requestId", requestId)
        payload.addProperty("profile", json.toString())
        if (profile.name.isNotEmpty()) {
            payload.addProperty("name", profile.name)
        }
        ws.send(MessageType.PROFILE_SHARE, payload)
    }

    fun requestFetch(code: String, onResult: (FetchResult) -> Unit) {
        val ws = instance.getShindoAPI().ws
        if (ws == null || !ws.isOpen()) {
            onResult(FetchResult.Error("websocket_unavailable"))
            return
        }
        val normalizedCode = code.trim().toUpperCase(Locale.ROOT)
        val requestId = UUID.randomUUID().toString()
        pendingFetch[requestId] = onResult

        val payload = JsonObject()
        payload.addProperty("requestId", requestId)
        payload.addProperty("code", normalizedCode)
        ws.send(MessageType.PROFILE_FETCH, payload)
    }

    fun requestUnshare(code: String, onResult: ((UnshareResult) -> Unit)? = null) {
        val ws = instance.getShindoAPI().ws
        if (ws == null || !ws.isOpen()) {
            onResult?.invoke(UnshareResult.Error("websocket_unavailable"))
            return
        }
        val normalizedCode = code.trim().toUpperCase(Locale.ROOT)
        if (normalizedCode.length != 12) {
            onResult?.invoke(UnshareResult.Error("invalid_code"))
            return
        }
        val requestId = UUID.randomUUID().toString()
        if (onResult != null) {
            pendingUnshare[requestId] = onResult
        }

        val payload = JsonObject()
        payload.addProperty("requestId", requestId)
        payload.addProperty("code", normalizedCode)
        ws.send(MessageType.PROFILE_UNSHARE, payload)
    }

    fun handleMessage(type: MessageType, payload: JsonObject?) {
        if (payload == null) {
            return
        }
        when (type) {
            MessageType.PROFILE_SHARE_OK -> handleShareOk(payload)
            MessageType.PROFILE_SHARE_ERROR -> handleShareError(payload)
            MessageType.PROFILE_FETCH_OK -> handleFetchOk(payload)
            MessageType.PROFILE_FETCH_ERROR -> handleFetchError(payload)
            MessageType.PROFILE_UNSHARE_OK -> handleUnshareOk(payload)
            MessageType.PROFILE_UNSHARE_ERROR -> handleUnshareError(payload)
            else -> {}
        }
    }

    private fun handleShareOk(payload: JsonObject) {
        val requestId = payload.get("requestId")?.asString ?: return
        val code = payload.get("code")?.asString ?: return
        dispatchShare(requestId, ShareResult.Success(code))
    }

    private fun handleShareError(payload: JsonObject) {
        val requestId = payload.get("requestId")?.asString ?: return
        val message = payload.get("message")?.asString
        dispatchShare(requestId, ShareResult.Error(message))
    }

    private fun handleFetchOk(payload: JsonObject) {
        val requestId = payload.get("requestId")?.asString ?: return
        val code = payload.get("code")?.asString
        val name = payload.get("name")?.asString
        val rawProfile = payload.get("profile")?.asString ?: return

        val profileJson = try {
            JsonParser.parseString(rawProfile).asJsonObject
        } catch (e: Exception) {
            ShindoLogger.error("Failed to parse shared profile JSON", e)
            dispatchFetch(requestId, FetchResult.Error("invalid_profile"))
            return
        }
        dispatchFetch(requestId, FetchResult.Success(code, name, profileJson))
    }

    private fun handleFetchError(payload: JsonObject) {
        val requestId = payload.get("requestId")?.asString ?: return
        val message = payload.get("message")?.asString
        dispatchFetch(requestId, FetchResult.Error(message))
    }

    private fun handleUnshareOk(payload: JsonObject) {
        val requestId = payload.get("requestId")?.asString ?: return
        val code = payload.get("code")?.asString ?: return
        dispatchUnshare(requestId, UnshareResult.Success(code))
    }

    private fun handleUnshareError(payload: JsonObject) {
        val requestId = payload.get("requestId")?.asString ?: return
        val message = payload.get("message")?.asString
        dispatchUnshare(requestId, UnshareResult.Error(message))
    }

    private fun dispatchShare(requestId: String, result: ShareResult) {
        val cb = pendingShare.remove(requestId) ?: return
        Minecraft.getMinecraft().addScheduledTask { cb(result) }
    }

    private fun dispatchFetch(requestId: String, result: FetchResult) {
        val cb = pendingFetch.remove(requestId) ?: return
        Minecraft.getMinecraft().addScheduledTask { cb(result) }
    }

    private fun dispatchUnshare(requestId: String, result: UnshareResult) {
        val cb = pendingUnshare.remove(requestId) ?: return
        Minecraft.getMinecraft().addScheduledTask { cb(result) }
    }

    sealed class ShareResult {
        data class Success(val code: String) : ShareResult()
        data class Error(val message: String?) : ShareResult()
    }

    sealed class FetchResult {
        data class Success(val code: String?, val name: String?, val json: JsonObject) : FetchResult()
        data class Error(val message: String?) : FetchResult()
    }

    sealed class UnshareResult {
        data class Success(val code: String) : UnshareResult()
        data class Error(val message: String?) : UnshareResult()
    }
}


