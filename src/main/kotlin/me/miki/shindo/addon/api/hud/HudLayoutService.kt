package me.miki.shindo.addon.api.hud

/**
 * Serviço responsável por registrar elementos de HUD de addons para que o
 * HUD Editor do client possa manipulá-los (mover/redimensionar/escala).
 */
interface HudLayoutService {

    fun register(hud: AddonHudElement)

    fun unregister(hud: AddonHudElement)

    /**
     * Retorna todos os HUDs registrados pelos addons.
     */
    fun getAll(): List<AddonHudElement>

    fun getById(id: String): AddonHudElement? =
        getAll().firstOrNull { it.id == id }

    fun unregisterById(id: String) {
        getById(id)?.let { unregister(it) }
    }

    fun unregisterByOwner(ownerAddonId: String) {
        if (ownerAddonId.isBlank()) return
        getAll()
            .filter { it.ownerAddonId() == ownerAddonId }
            .forEach { unregister(it) }
    }
}
