@file:JvmName("JsonObjectExtensions")

package me.miki.extensions.serialization.gson

import com.google.gson.JsonObject

fun JsonObject.stringOrNull(key: String): String? {
    if (!has(key)) return null
    val value = get(key)
    return if (value != null && !value.isJsonNull) value.asString else null
}

fun JsonObject.booleanOrDefault(key: String, defaultValue: Boolean = false): Boolean {
    if (!has(key)) return defaultValue
    val value = get(key)
    return if (value != null && !value.isJsonNull) value.asBoolean else defaultValue
}

fun JsonObject.putIfNotBlank(key: String, value: String?): JsonObject {
    if (value != null && value.isNotBlank()) {
        addProperty(key, value)
    }
    return this
}
