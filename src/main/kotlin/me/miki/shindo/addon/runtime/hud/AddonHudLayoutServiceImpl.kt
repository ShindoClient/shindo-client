package me.miki.shindo.addon.runtime.hud

import me.miki.shindo.addon.api.hud.AddonHudElement
import me.miki.shindo.addon.api.hud.HudLayoutService
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Implementação de [HudLayoutService] usada pelo Shindo Client.
 */
class AddonHudLayoutServiceImpl : HudLayoutService {

    private val elements = CopyOnWriteArrayList<AddonHudElement>()

    override fun register(hud: AddonHudElement) {
        val owner = hud.ownerAddonId()
        if (elements.any { it.id == hud.id && it.ownerAddonId() == owner }) {
            // Evita duplicatas com mesmo id.
            return
        }
        elements.add(hud)
    }

    override fun unregister(hud: AddonHudElement) {
        val owner = hud.ownerAddonId()
        elements.removeIf { it.id == hud.id && it.ownerAddonId() == owner }
    }

    override fun getAll(): List<AddonHudElement> = elements.toList()

    override fun getById(id: String): AddonHudElement? =
        elements.firstOrNull { it.id == id }

    override fun unregisterById(id: String) {
        elements.removeIf { it.id == id }
    }

    override fun unregisterByOwner(ownerAddonId: String) {
        if (ownerAddonId.isBlank()) return
        elements.removeIf { it.ownerAddonId() == ownerAddonId }
    }
}
