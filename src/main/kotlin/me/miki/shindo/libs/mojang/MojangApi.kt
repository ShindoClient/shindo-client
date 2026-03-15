package me.miki.shindo.libs.mojang

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.miki.shindo.libs.hypixel.exceptions.MojangApiException
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.network.HttpUtils
import me.miki.shindo.utils.network.UserAgents
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object MojangApi {

    private const val NAME_TO_UUID_URL = "https://api.mojang.com/users/profiles/minecraft/"
    private const val UUID_TO_NAME_URL = "https://sessionserver.mojang.com/session/minecraft/profile/"
    private const val TIMEOUT = 10000
    private val gson = Gson()

    private val nameToUuidCache = ConcurrentHashMap<String, NameUuidData>()
    private val uuidToNameCache = ConcurrentHashMap<UUID, String>()
    fun nameToUUID(playername: String): NameUuidData {
        val lowerCaseName = playername.toLowerCase(Locale.ROOT)

        if (!Pattern.matches("\\w{1,16}", lowerCaseName)) {
            throw MojangApiException("${MojangApiException.INVALID_NAME}: $playername")
        }

        nameToUuidCache[lowerCaseName]?.let { return it }

        try {
            val url = "$NAME_TO_UUID_URL$playername"
            val response = HttpUtils.get(url, null, UserAgents.MOZILLA, TIMEOUT)
                ?: throw MojangApiException("Failed to create request")
            if (response.code == 204) {
                throw MojangApiException("${MojangApiException.PLAYER_NOT_FOUND}: $playername")
            }

            val json = gson.fromJson(response.body, JsonObject::class.java)
            val name = json.get("name")?.asString
            val id = json.get("id")?.asString

            if (name == null || id == null) {
                throw MojangApiException("${MojangApiException.PLAYER_NOT_FOUND}: $playername")
            }

            val uuid = formatUUID(id)
            val data = NameUuidData(name, uuid)
            nameToUuidCache[lowerCaseName] = data

            return data
        } catch (e: MojangApiException) {
            throw e
        } catch (e: Exception) {
            ShindoLogger.error("[MojangAPI] Failed to get UUID for $playername", e)
            throw MojangApiException("Failed to get UUID: ${e.message}", e)
        }
    }

    fun uuidToName(uuid: UUID): String {

        uuidToNameCache[uuid]?.let { return it }

        try {
            val url = "$UUID_TO_NAME_URL$uuid"
            val response = HttpUtils.get(url, null, UserAgents.MOZILLA, TIMEOUT)
                ?: throw MojangApiException("Failed to create request")
            if (response.code != 200) {
                throw MojangApiException("${MojangApiException.INVALID_UUID}: $uuid")
            }

            val json = gson.fromJson(response.body, JsonObject::class.java)
            val name = json.get("name")?.asString

            if (name == null) {
                throw MojangApiException("${MojangApiException.INVALID_UUID}: $uuid")
            }

            uuidToNameCache[uuid] = name
            return name
        } catch (e: MojangApiException) {
            throw e
        } catch (e: Exception) {
            ShindoLogger.error("[MojangAPI] Failed to get name for $uuid", e)
            throw MojangApiException("Failed to get name: ${e.message}", e)
        }
    }

    private fun formatUUID(uuidWithoutDashes: String): UUID {
        return if (uuidWithoutDashes.length == 32) {
            UUID.fromString(
                "${uuidWithoutDashes.substring(0, 8)}-" +
                        "${uuidWithoutDashes.substring(8, 12)}-" +
                        "${uuidWithoutDashes.substring(12, 16)}-" +
                        "${uuidWithoutDashes.substring(16, 20)}-" +
                        "${uuidWithoutDashes.substring(20, 32)}"
            )
        } else {
            UUID.fromString(uuidWithoutDashes)
        }
    }

    fun clearCache() {
        nameToUuidCache.clear()
        uuidToNameCache.clear()
    }

    data class NameUuidData(
        val name: String,
        val uuid: UUID
    ) {
        constructor(name: String, uuidString: String) : this(
            name,
            if (uuidString.length == 32) {
                UUID.fromString(
                    "${uuidString.substring(0, 8)}-" +
                            "${uuidString.substring(8, 12)}-" +
                            "${uuidString.substring(12, 16)}-" +
                            "${uuidString.substring(16, 20)}-" +
                            "${uuidString.substring(20, 32)}"
                )
            } else {
                UUID.fromString(uuidString)
            }
        )
    }
}


