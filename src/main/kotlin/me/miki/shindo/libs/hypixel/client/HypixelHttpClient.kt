package me.miki.shindo.libs.hypixel.client

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.miki.shindo.libs.hypixel.exceptions.HypixelApiException
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.network.HttpUtils
import me.miki.shindo.utils.network.UserAgents
import java.net.HttpURLConnection

/**
 * Cliente HTTP para a API do Hypixel
 * 
 * Funcionalidades:
 * - Requisições GET para API do Hypixel
 * - Gerenciamento de API key
 * - Rate limiting básico
 * - Cache de requisições
 * 
 * Extensível para:
 * - Rate limiting avançado
 * - Retry automático
 * - Pool de conexões
 * - Métricas e monitoramento
 */
object HypixelHttpClient {

    private const val BASE_URL = "https://api.hypixel.net"
    private const val TIMEOUT = 10000 // 10 segundos
    private val gson = Gson()
    
    private var apiKey: String? = null
    private var lastRequestTime = 0L
    private val minRequestInterval = 100L // 100ms entre requisições

    /**
     * Define a API key do Hypixel
     */
    fun setApiKey(key: String?) {
        apiKey = key
    }

    /**
     * Verifica se a API key está configurada
     */
    fun hasApiKey(): Boolean {
        return !apiKey.isNullOrBlank()
    }

    /**
     * Faz uma requisição GET para a API do Hypixel
     */
    fun get(endpoint: String, requireApiKey: Boolean = true): JsonObject? {
        if (requireApiKey && !hasApiKey()) {
            throw HypixelApiException(HypixelApiException.INVALID_API_KEY)
        }

        // Rate limiting básico
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
            val connection = HttpUtils.setupConnection(url, UserAgents.MOZILLA, TIMEOUT, false)
                ?: throw HypixelApiException("Failed to create connection")

            connection.connect()
            val statusCode = connection.responseCode

            if (statusCode != HttpURLConnection.HTTP_OK) {
                when (statusCode) {
                    403 -> throw HypixelApiException(HypixelApiException.INVALID_API_KEY)
                    429 -> throw HypixelApiException(HypixelApiException.RATE_LIMITED)
                    else -> throw HypixelApiException("HTTP $statusCode: ${connection.responseMessage}")
                }
            }

            val response = HttpUtils.readResponse(connection)
            
            if (response.isBlank()) {
                throw HypixelApiException("Empty response from API")
            }

            val json = gson.fromJson(response, JsonObject::class.java)
                ?: throw HypixelApiException("Failed to parse JSON response")
            
            // Verifica se há erro na resposta
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

    /**
     * Obtém dados de um jogador pelo UUID
     */
    fun getPlayerData(uuid: String): JsonObject? {
        return get("/player?uuid=$uuid")
    }

    /**
     * Obtém dados de um jogador pelo UUID (com cache)
     */
    fun getPlayerDataCached(uuid: String): JsonObject? {
        return HypixelCache.getPlayerData(uuid) ?: run {
            val data = getPlayerData(uuid)
            data?.let { HypixelCache.cachePlayerData(uuid, it) }
            data
        }
    }
}
