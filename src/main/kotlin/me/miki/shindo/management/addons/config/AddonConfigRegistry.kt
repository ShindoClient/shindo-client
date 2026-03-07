package me.miki.shindo.management.addons.config

import me.miki.shindo.addon.api.config.IAddonConfigStorage
import java.util.concurrent.ConcurrentHashMap

/**
 * Registro de IAddonConfigStorage por addon. Usado pelo ProfileManager
 * para salvar/carregar configs no perfil. Só processa addons que existem no AddonManager
 * (fallback: addons removidos não causam erro ao carregar).
 */
object AddonConfigRegistry {

    private val storages = ConcurrentHashMap<String, AddonConfigStorageImpl>()

    fun getOrCreate(addonId: String): AddonConfigStorageImpl {
        return storages.getOrPut(addonId) { AddonConfigStorageImpl(addonId) }
    }

    fun get(addonId: String): AddonConfigStorageImpl? = storages[addonId]

    fun getAll(): Map<String, AddonConfigStorageImpl> = HashMap(storages)

    fun remove(addonId: String) {
        storages.remove(addonId)
    }
}
