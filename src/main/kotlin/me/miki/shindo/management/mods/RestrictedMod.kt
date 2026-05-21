package me.miki.shindo.management.mods

import me.miki.shindo.Shindo
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.utils.ServerUtils

@Suppress("UNUSED")
class RestrictedMod {
    var shouldCheck: Boolean = true
    private var currentServerIP = ""
    private val blacklistManager = Shindo.getInstance().getBlacklistManager()

    fun checkAllowed(mod: Mod): Boolean {
        if (shouldCheck) {
            val servers = blacklistManager.getBlacklist()
            for (server in servers) {
                if (currentServerIP.contains(server.serverIp)) {
                    val blacklistedMods = server.mods
                    if (blacklistedMods.contains(mod.getNameKey())) {
                        mod.setAllowed(false)
                        return false
                    }
                }
            }
        }
        mod.setAllowed(true)
        return true
    }

    fun joinServer(ip: String) {
        blacklistManager.check()
    }

    fun joinWorld() {
        currentServerIP = ServerUtils.getServerIP()
        for (mod in Shindo.getInstance().getModManager().getMods()) {
            if (!checkAllowed(mod) && mod.isToggled()) {
                mod.setToggled(false)
                Shindo.getInstance().getNotificationManager().post(
                    mod.getName(),
                    "Disabled due to serverside blacklist",
                    NotificationType.INFO,
                )
            }
        }
    }
}
