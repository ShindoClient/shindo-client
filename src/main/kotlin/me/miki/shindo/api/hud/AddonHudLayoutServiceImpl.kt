package me.miki.shindo.api.hud

import me.miki.client_api.hud.AddonHudElement
import me.miki.client_api.hud.HudLayoutService
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Implementação de [HudLayoutService] usada pelo Shindo Client.
 */
class AddonHudLayoutServiceImpl : HudLayoutService {

    private val elements = CopyOnWriteArrayList<AddonHudElement>()

    override fun register(hud: AddonHudElement) {
        if (elements.any { it.id == hud.id }) {
            // Evita duplicatas com mesmo id.
            return
        }
        elements.add(hud)
    }

    override fun unregister(hud: AddonHudElement) {
        elements.removeIf { it.id == hud.id }
    }

    override fun getAll(): List<AddonHudElement> = elements.toList()
}

