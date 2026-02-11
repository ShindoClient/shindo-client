package me.miki.shindo.libs.hypixel.client

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.miki.shindo.libs.hypixel.exceptions.HypixelApiException
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.network.HttpUtils
import me.miki.shindo.utils.network.UserAgents
object HypixelHttpClient {

    private const val BASE_URL = "https://api.hypixel.net"
    private const val TIMEOUT = 10000
    private val gson = Gson()

    private var apiKey: String? = null
    private var lastRequestTime = 0L
    private val minRequestInterval = 100L
    fun setApiKey(key: String?) {
        apiKey = key
    }
    fun hasApiKey(): Boolean {
        return !apiKey.isNullOrBlank()
    }
    fun get(endpoint: String, requireApiKey: Boolean = true): JsonObject? {
        if (requireApiKey && !hasApiKey()) {
            throw HypixelApiException(HypixelApiException.INVALID_API_KEY)
        }

        val currentTime = System.currentTimeMillis()
        val timeSinceLastRequest = currentTime - lastRequestTime
        if (timeSinceLastRequest < minRequestInterval) {
            Thread.sleep(minRequestInterval - timeSinceLastRequest)
        }
        lastRequestTime = System.currentTimeMillis()

        val url = if (endpoint.contains("?")) {
            "$BASE_URL$endpoint&key=${apiKey ?: ""}"
        } else {
            "$BASE_URL$endpoint?key=${apiKey ?: ""}"
        }

        return try {
            val response = HttpUtils.get(url, null, UserAgents.MOZILLA, TIMEOUT)
                ?: throw HypixelApiException("Failed to create request")
            val statusCode = response.code

            if (statusCode != 200) {
                when (statusCode) {
                    403 -> throw HypixelApiException(HypixelApiException.INVALID_API_KEY)
                    429 -> throw HypixelApiException(HypixelApiException.RATE_LIMITED)
                    else -> throw HypixelApiException("HTTP $statusCode")
                }
            }

            if (response.body.isBlank()) {
                throw HypixelApiException("Empty response from API")
            }

            val json = gson.fromJson(response.body, JsonObject::class.java)
                ?: throw HypixelApiException("Failed to parse JSON response")

            if (json.has("success") && !json.get("success").asBoolean) {
                val cause = json.get("cause")?.asString ?: "Unknown error"
                throw HypixelApiException(cause)
            }

            json
        } catch (e: HypixelApiException) {
            throw e
        } catch (e: Exception) {
            ShindoLogger.error("[HypixelAPI] Request failed for $endpoint", e)
            throw HypixelApiException("Request failed: ${e.message}", e)
        }
    }
    fun getPlayerData(uuid: String): JsonObject? {
        return get("/player?uuid=$uuid")
    }
    fun getPlayerDataCached(uuid: String): JsonObject? {
        return HypixelCache.getPlayerData(uuid) ?: run {
            val data = getPlayerData(uuid)
            data?.let { HypixelCache.cachePlayerData(uuid, it) }
            data
        }
    }
}
