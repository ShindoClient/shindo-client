package me.miki.shindo.addon.api.config

/**
 * Armazenamento key-value de configurações do addon, persistido no perfil do client.
 * Os valores são salvos junto com o perfil (JSON) e restaurados ao carregar.
 *
 * **Fallback quando addon é removido:** Se o addon for desinstalado, as chaves
 * no JSON do perfil são ignoradas ao carregar (não causam erro). O client
 * só aplica configs de addons que estão carregados.
 *
 * Use o id do addon ([AddonMetadata.id]) ao obter o storage via context.
 */
interface IAddonConfigStorage {

    fun getString(key: String, default: String = ""): String
    fun setString(key: String, value: String)

    fun getInt(key: String, default: Int = 0): Int
    fun setInt(key: String, value: Int)

    fun getLong(key: String, default: Long = 0L): Long
    fun setLong(key: String, value: Long)

    fun getFloat(key: String, default: Float = 0f): Float
    fun setFloat(key: String, value: Float)

    fun getDouble(key: String, default: Double = 0.0): Double
    fun setDouble(key: String, value: Double)

    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)

    /**
     * Cor em formato RGBA (0xAARRGGBB).
     */
    fun getColor(key: String, default: Int = 0xFFFFFFFF.toInt()): Int
    fun setColor(key: String, value: Int)

    /**
     * Força a persistência imediata (opcional - o client pode fazer auto-save ao trocar perfil).
     */
    fun save()
}
