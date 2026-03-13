package me.miki.shindo.management.addons.loader

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.AddonManager
import me.miki.shindo.management.file.FileManager

/**
 * Stub loader retained to keep the old addon flow intact.
 * External JAR addons via client-api are disabled.
 */
object AddonLoader {
    fun loadExternalAddons(fileManager: FileManager, addonManager: AddonManager) {
        ShindoLogger.info("[ADDON] External addon loading disabled (client-api reverted).")
    }
}
