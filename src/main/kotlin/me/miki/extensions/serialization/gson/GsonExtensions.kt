@file:JvmName("GsonExtensions")

package me.miki.extensions.serialization.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Parses JSON using a reified type and returns null on any parse error.
 */
inline fun <reified T> Gson.fromJsonOrNull(json: String?): T? {
    if (json.isNullOrBlank()) return null
    return try {
        fromJson(json, T::class.java)
    } catch (ignored: Throwable) {
        null
    }
}

/**
 * Converts any object to JsonObject, or null when conversion is not an object.
 */
fun Gson.toJsonObjectOrNull(value: Any?): JsonObject? {
    if (value == null) return null
    return try {
        JsonParser().parse(toJson(value)).asJsonObject
    } catch (ignored: Throwable) {
        null
    }
}

/**
 * Creates a pretty-print Gson instance for files and debug logs.
 */
fun Gson.prettyCopy(): Gson {
    return GsonBuilder().setPrettyPrinting().create()
}
