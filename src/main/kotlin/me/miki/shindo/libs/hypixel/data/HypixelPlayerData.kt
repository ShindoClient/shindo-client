package me.miki.shindo.libs.hypixel.data

import com.google.gson.JsonObject
import me.miki.shindo.libs.hypixel.client.HypixelHttpClient
import me.miki.shindo.libs.hypixel.exceptions.HypixelApiException
import java.util.UUID

/**
 * Dados de um jogador do Hypixel
 * 
 * Funcionalidades:
 * - Acessa dados do jogador via API
 * - Parsing de informações básicas
 * 
 * Extensível para:
 * - Parsing de estatísticas de jogos
 * - Parsing de achievements
 * - Parsing de social media
 * - Parsing de guild
 */
class HypixelPlayerData(
    uuid: UUID,
    useCache: Boolean = true
) {
    
    private val playerData: JsonObject
    val uuid: String = uuid.toString().replace("-", "")

    init {
        val data = if (useCache) {
            HypixelHttpClient.getPlayerDataCached(this.uuid)
        } else {
            HypixelHttpClient.getPlayerData(this.uuid)
        }
        
        if (data == null) {
            throw HypixelApiException(HypixelApiException.PLAYER_NEVER_JOINED)
        }
        
        val playerObj = data.getAsJsonObject("player")
        if (playerObj == null) {
            throw HypixelApiException(HypixelApiException.PLAYER_NEVER_JOINED)
        }
        
        this.playerData = playerObj
    }

    constructor(uuid: String, useCache: Boolean = true) : this(
        UUID.fromString(uuid.replace("-", "").let {
            // Adiciona hífens se necessário
            if (it.length == 32) {
                "${it.substring(0, 8)}-${it.substring(8, 12)}-${it.substring(12, 16)}-${it.substring(16, 20)}-${it.substring(20, 32)}"
            } else {
                it
            }
        }),
        useCache
    )

    /**
     * Obtém os dados brutos do jogador
     */
    fun getPlayerData(): JsonObject = playerData

    /**
     * Obtém o display name do jogador
     */
    fun getDisplayName(): String? {
        return playerData.get("displayname")?.asString
    }

    /**
     * Obtém o nome do jogador
     */
    fun getPlayerName(): String? {
        return playerData.get("playername")?.asString ?: getDisplayName()
    }

    /**
     * Verifica se o jogador está online
     */
    fun isOnline(): Boolean {
        return playerData.get("lastLogin")?.asLong?.let { lastLogin ->
            val lastLogout = playerData.get("lastLogout")?.asLong ?: 0L
            lastLogin > lastLogout
        } ?: false
    }

    /**
     * Obtém timestamp do último login
     */
    fun getLastLogin(): Long {
        return playerData.get("lastLogin")?.asLong ?: 0L
    }

    /**
     * Obtém timestamp do último logout
     */
    fun getLastLogout(): Long {
        return playerData.get("lastLogout")?.asLong ?: 0L
    }

    /**
     * Verifica se o jogador está escondendo informações da API
     */
    fun isHidingFromAPI(): Boolean {
        return playerData.get("settings")?.asJsonObject?.get("apiDisabled")?.asBoolean ?: false
    }
}
