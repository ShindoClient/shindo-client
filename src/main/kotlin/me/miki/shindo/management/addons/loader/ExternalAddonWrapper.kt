package me.miki.shindo.management.addons.loader

import me.miki.shindo.Shindo
import me.miki.client_api.AddonMetadata
import me.miki.client_api.AddonType as ApiAddonType
import me.miki.client_api.ShindoAddon
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.Addon
import me.miki.shindo.management.addons.AddonManager
import me.miki.shindo.management.addons.AddonType

/**
 * Adapta um ShindoAddon (API) para o Addon interno do client.
 */
class ExternalAddonWrapper(
    private val apiAddon: ShindoAddon,
    private val metadata: AddonMetadata
) : Addon(
    metadata.name,
    metadata.description,
    null,
    metadata.icon,
    mapType(metadata.type)
) {

    override val isBuiltIn: Boolean get() = false

    override val showToggle: Boolean get() = metadata.showToggle

    override fun onEnable() {
        Shindo.getInstance().eventManager.register(apiAddon)
        apiAddon.onEnable()
        ShindoLogger.info("[ADDON] ${metadata.name} foi habilitado")
    }

    override fun onDisable() {
        Shindo.getInstance().eventManager.unregister(apiAddon)
        apiAddon.onDisable()
        ShindoLogger.info("[ADDON] ${metadata.name} foi desabilitado")
    }

    fun getMetadata(): AddonMetadata = metadata

    companion object {
        private fun mapType(apiType: ApiAddonType): AddonType = when (apiType) {
            ApiAddonType.RENDER -> AddonType.RENDER
            ApiAddonType.QOL -> AddonType.QOL
            ApiAddonType.OTHER -> AddonType.OTHER
        }
    }
}
