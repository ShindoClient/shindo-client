package me.miki.shindo.api.serialization

import com.google.gson.JsonElement
import kotlinx.serialization.json.*
import me.miki.shindo.logger.ShindoLogger
import kotlinx.serialization.json.JsonElement as KotlinJsonElement

object ApiJson {

    private val json = Json(JsonConfiguration.Stable)

    fun parseFromGson(element: JsonElement?): KotlinJsonElement? {
        if (element == null) return null
        return try {
            json.parse(JsonElementSerializer, element.toString())
        } catch (ex: Exception) {
            ShindoLogger.error("Falha ao converter JsonElement do websocket", ex)
            null
        }
    }

    fun parseFromString(source: String?): KotlinJsonElement? {
        if (source.isNullOrBlank()) return null
        return try {
            json.parse(JsonElementSerializer, source)
        } catch (ex: Exception) {
            ShindoLogger.error("Falha ao converter string JSON", ex)
            null
        }
    }

    fun KotlinJsonElement?.asString(): String? = (this as? JsonPrimitive)?.contentOrNull

    private fun KotlinJsonElement?.asLong(): Long? = (this as? JsonPrimitive)?.content?.toLongOrNull()

    private fun KotlinJsonElement?.asBoolean(): Boolean? {
        val primitive = this as? JsonPrimitive ?: return null
        return when (primitive.content.trim().toLowerCase()) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }

    fun KotlinJsonElement?.asArray(): List<KotlinJsonElement>? = (this as? JsonArray)?.toList()

    fun JsonObject?.getString(key: String): String? = this?.get(key)?.asString()
    fun JsonObject?.getLong(key: String): Long? = this?.get(key)?.asLong()
    fun JsonObject?.getBoolean(key: String): Boolean? = this?.get(key)?.asBoolean()
    fun JsonObject?.getObject(key: String): JsonObject? = this?.get(key) as? JsonObject
    fun JsonObject?.getArray(key: String): List<KotlinJsonElement>? = (this?.get(key) as? JsonArray)?.toList()
}
