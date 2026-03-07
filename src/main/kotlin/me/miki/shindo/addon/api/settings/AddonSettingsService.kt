package me.miki.shindo.addon.api.settings

/**
 * Serviço de registro e acesso a settings de addons.
 *
 * As settings registradas aqui são renderizadas no mesmo painel de settings
 * dos mods, reaproveitando o sistema interno de [Setting] do client.
 */
interface AddonSettingsService {

    /**
     * Registra uma setting booleana para o addon informado.
     */
    fun registerBoolean(
        addonId: String,
        key: String,
        displayName: String,
        category: String = "",
        description: String = "",
        defaultValue: Boolean = false
    )

    /**
     * Registra uma setting de keybind (tecla) para o addon informado.
     *
     * @param defaultKeyCode código da tecla (LWJGL) usado como padrão.
     */
    fun registerKeybind(
        addonId: String,
        key: String,
        displayName: String,
        category: String = "",
        description: String = "",
        defaultKeyCode: Int
    )

    /**
     * Obtém o valor atual de uma setting booleana.
     */
    fun getBoolean(addonId: String, key: String, defaultValue: Boolean = false): Boolean

    /**
     * Obtém o valor atual de uma setting inteira (ex.: keybind).
     */
    fun getInt(addonId: String, key: String, defaultValue: Int = 0): Int
}

