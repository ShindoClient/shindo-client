package me.miki.shindo.libs.hypixel

import me.miki.shindo.libs.hypixel.client.HypixelHttpClient
import me.miki.shindo.logger.ShindoLogger
import java.io.File
import java.util.*
object HypixelApiKeyManager {

    private const val API_KEY_FILE = "hypixel_api_key.properties"
    private const val API_KEY_PROPERTY = "api_key"
    fun getApiKey(): String? {
        val file = getApiKeyFile()
        if (!file.exists()) return null

        return try {
            val props = Properties()
            file.inputStream().use { props.load(it) }
            props.getProperty(API_KEY_PROPERTY)?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            ShindoLogger.error("[HypixelAPI] Failed to load API key", e)
            null
        }
    }
    fun setApiKey(key: String?) {
        if (key.isNullOrBlank()) {
            HypixelHttpClient.setApiKey(null)
            deleteApiKeyFile()
            return
        }

        val file = getApiKeyFile()
        try {
            val props = Properties()
            props.setProperty(API_KEY_PROPERTY, key)
            file.parentFile?.mkdirs()
            file.outputStream().use { props.store(it, "Hypixel API Key") }

            HypixelHttpClient.setApiKey(key)
            ShindoLogger.info("[HypixelAPI] API key saved successfully")
        } catch (e: Exception) {
            ShindoLogger.error("[HypixelAPI] Failed to save API key", e)
        }
    }
    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }
    fun initialize() {
        val key = getApiKey()
        if (key != null) {
            HypixelHttpClient.setApiKey(key)
            ShindoLogger.info("[HypixelAPI] API key loaded from file")
        } else {
            ShindoLogger.info("[HypixelAPI] No API key found, some features may be limited")
        }
    }
    fun validateApiKey(key: String?): Boolean {
        if (key.isNullOrBlank()) return false

        return try {
            val oldKey = getApiKey()
            HypixelHttpClient.setApiKey(key)
            val result = HypixelHttpClient.get("/key", requireApiKey = true)
            HypixelHttpClient.setApiKey(oldKey)

            result?.get("success")?.asBoolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun getApiKeyFile(): File {
        val shindoDir = me.miki.shindo.Shindo.getInstance().fileManager.shindoDir
        return File(shindoDir, API_KEY_FILE)
    }

    private fun deleteApiKeyFile() {
        val file = getApiKeyFile()
        if (file.exists()) {
            file.delete()
        }
    }
}
