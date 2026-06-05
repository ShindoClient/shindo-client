package me.miki.shindo.management.addons.bridge

import me.miki.addon.api.hud.AddonHUD
import me.miki.addon.api.hud.HUDContext
import me.miki.shindo.Shindo
import me.miki.shindo.management.addons.bridge.render.AddonNanoVGBridge
import me.miki.shindo.management.mods.ModManager
import me.miki.shindo.management.nanovg.NanoVGManager

class AddonBridge(
    private val modManager: ModManager = Shindo.getInstance().getModManager(),
    private val nanoVGManager: NanoVGManager = Shindo.getInstance().nanoVGManager,
    private val clientEventManager: Any = Shindo.getInstance().getEventManager(),
) {
    companion object {
        const val API_VERSION = 1
        const val MIN_COMPATIBLE_VERSION = 1
    }

    fun wrapHUD(addonHUD: AddonHUD): AddonHUDMod = AddonHUDMod(addonHUD)

    fun registerHUD(hudMod: AddonHUDMod) {
        modManager.registerHudMod(hudMod)
    }

    fun unregisterHUD(hudMod: AddonHUDMod) {
        modManager.unregisterHudMod(hudMod)
    }

    fun createHUDContext(
        hudWidth: Int,
        hudHeight: Int,
        partialTicks: Float,
        nanoVG: me.miki.addon.api.graphics.NanoVG?,
    ): HUDContext =
        HUDContext(
            width = hudWidth.toFloat(),
            height = hudHeight.toFloat(),
            partialTicks = partialTicks,
            nanoVG = nanoVG,
        )

    fun createNanoVGBridge(): AddonNanoVGBridge = AddonNanoVGBridge(nanoVGManager)

    fun registerEventBridge(bridge: AddonEventBridge) {
        try {
            val em = clientEventManager as? me.miki.shindo.management.event.EventManager
            em?.register(bridge)
        } catch (_: Exception) {
        }
    }

    fun unregisterEventBridge(bridge: AddonEventBridge) {
        try {
            val em = clientEventManager as? me.miki.shindo.management.event.EventManager
            em?.unregister(bridge)
        } catch (_: Exception) {
        }
    }
}
