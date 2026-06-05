package me.miki.shindo.management.addons.config

import com.google.gson.JsonObject
import java.util.concurrent.ConcurrentHashMap

class AddonConfigStorageImpl(
    private val addonId: String,
) {
    private val data = ConcurrentHashMap<String, String>()

    fun getAddonId(): String = addonId

    fun getString(
        key: String,
        default: String,
    ): String = data[key] ?: default

    fun setString(
        key: String,
        value: String,
    ) {
        data[key] = value
    }

    fun getInt(
        key: String,
        default: Int,
    ): Int = data[key]?.toIntOrNull() ?: default

    fun setInt(
        key: String,
        value: Int,
    ) {
        data[key] = value.toString()
    }

    fun getLong(
        key: String,
        default: Long,
    ): Long = data[key]?.toLongOrNull() ?: default

    fun setLong(
        key: String,
        value: Long,
    ) {
        data[key] = value.toString()
    }

    fun getFloat(
        key: String,
        default: Float,
    ): Float = data[key]?.toFloatOrNull() ?: default

    fun setFloat(
        key: String,
        value: Float,
    ) {
        data[key] = value.toString()
    }

    fun getDouble(
        key: String,
        default: Double,
    ): Double = data[key]?.toDoubleOrNull() ?: default

    fun setDouble(
        key: String,
        value: Double,
    ) {
        data[key] = value.toString()
    }

    fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean = data[key]?.let { it.equals("true", true) } ?: default

    fun setBoolean(
        key: String,
        value: Boolean,
    ) {
        data[key] = value.toString()
    }

    fun getColor(
        key: String,
        default: Int,
    ): Int = data[key]?.toIntOrNull(16) ?: default

    fun setColor(
        key: String,
        value: Int,
    ) {
        data[key] = value.toString(16)
    }

    fun save() {
        // Persistência é feita pelo ProfileManager ao salvar perfil
        // Addons podem chamar save() para sinalizar - por ora é no-op
    }

    fun toJson(): JsonObject {
        val obj = JsonObject()
        data.forEach { (k, v) -> obj.addProperty(k, v) }
        return obj
    }

    fun fromJson(obj: JsonObject) {
        obj.entrySet().forEach { (k, v) ->
            if (!v.isJsonNull) {
                data[k] =
                    when {
                        v.isJsonPrimitive -> v.asJsonPrimitive.asString
                        else -> v.toString()
                    }
            }
        }
    }
}
