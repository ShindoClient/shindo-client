package com.shindoclient.shindo.management.addons.config

import java.util.concurrent.ConcurrentHashMap

object AddonConfigRegistry {
    private val storages = ConcurrentHashMap<String, AddonConfigStorageImpl>()

    fun getOrCreate(addonId: String): AddonConfigStorageImpl =
        storages.getOrPut(addonId) {
            AddonConfigStorageImpl(addonId)
        }

    fun get(addonId: String): AddonConfigStorageImpl? = storages[addonId]

    fun getAll(): Map<String, AddonConfigStorageImpl> = HashMap(storages)

    fun remove(addonId: String) {
        storages.remove(addonId)
    }
}
