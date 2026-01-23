package me.miki.shindo.libs.hypixel

import me.miki.shindo.libs.hypixel.client.HypixelHttpClient
import me.miki.shindo.logger.ShindoLogger
import java.io.File
import java.util.Properties

/**
 * Gerenciador de API Key do Hypixel
 * 
 * Funcionalidades:
 * - Salva/carrega API key de arquivo
 * - Valida API key
 * - Integra com HypixelHttpClient
 * 
 * Extensível para:
 * - Múltiplas API keys
 * - Rotação de keys
 * - Validação avançada
 */
object HypixelApiKeyManager {

    private const val API_KEY_FILE = "hypixel_api_key.properties"
    private const val API_KEY_PROPERTY = "api_key"

    /**
     * Obtém a API key salva
     */
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

    /**
     * Define e salva a API key
     */
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

    /**
     * Verifica se a API key está configurada
     */
    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    /**
     * Inicializa a API key do arquivo
     */
    fun initialize() {
        val key = getApiKey()
        if (key != null) {
            HypixelHttpClient.setApiKey(key)
            ShindoLogger.info("[HypixelAPI] API key loaded from file")
        } else {
            ShindoLogger.info("[HypixelAPI] No API key found, some features may be limited")
        }
    }

    /**
     * Valida a API key fazendo uma requisição de teste
     */
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
