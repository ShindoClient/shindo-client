package me.miki.shindo.management.addons.config

import com.google.gson.JsonObject
import me.miki.client_api.config.IAddonConfigStorage
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementação de IAddonConfigStorage. Os valores são guardados em memória
 * e serializados pelo ProfileManager no JSON do perfil (seção Addons -> {addonId} -> Config).
 * Ao carregar perfil, só aplicamos config de addons que existem (fallback para addons removidos).
 */
class AddonConfigStorageImpl(private val addonId: String) : IAddonConfigStorage {

    private val data = ConcurrentHashMap<String, String>()

    fun getAddonId(): String = addonId

    override fun getString(key: String, default: String): String = data[key] ?: default

    override fun setString(key: String, value: String) {
        data[key] = value
    }

    override fun getInt(key: String, default: Int): Int =
        data[key]?.toIntOrNull() ?: default

    override fun setInt(key: String, value: Int) {
        data[key] = value.toString()
    }

    override fun getLong(key: String, default: Long): Long =
        data[key]?.toLongOrNull() ?: default

    override fun setLong(key: String, value: Long) {
        data[key] = value.toString()
    }

    override fun getFloat(key: String, default: Float): Float =
        data[key]?.toFloatOrNull() ?: default

    override fun setFloat(key: String, value: Float) {
        data[key] = value.toString()
    }

    override fun getDouble(key: String, default: Double): Double =
        data[key]?.toDoubleOrNull() ?: default

    override fun setDouble(key: String, value: Double) {
        data[key] = value.toString()
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        data[key]?.let { it.equals("true", true) } ?: default

    override fun setBoolean(key: String, value: Boolean) {
        data[key] = value.toString()
    }

    override fun getColor(key: String, default: Int): Int =
        data[key]?.toIntOrNull(16) ?: default

    override fun setColor(key: String, value: Int) {
        data[key] = value.toString(16)
    }

    override fun save() {
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
                data[k] = when {
                    v.isJsonPrimitive -> v.asJsonPrimitive.asString
                    else -> v.toString()
                }
            }
        }
    }
}
