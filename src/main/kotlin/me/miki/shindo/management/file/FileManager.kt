package me.miki.shindo.management.file

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import net.minecraft.client.Minecraft
import java.io.File
import java.io.IOException

class FileManager {

    val shindoDir: File
    val externalDir: File
    val cacheDir: File
    val logsDir: File

    val musicDir: File
    val profileDir: File
    val screenshotDir: File
    val addonsDir: File
    val addonConfigDir: File
    val gamesDir: File
    val skinsDir: File

    val serversDir: File

    val customCapeDir: File
    val capeCacheDir: File

    val profileIconDir: File
    val mainLogFile: File
    val coreLogFile: File
    val modsLogFile: File
    val chatLogFile: File
    val notificationLogFile: File
    val websocketLogFile: File
    val musicLogFile: File
    val discordLogFile: File
    val uiLogFile: File
    val securityLogFile: File
    val addonsLogFile: File
    val profileLogFile: File

    init {
        val mcDir = Minecraft.getMinecraft().mcDataDir
        val soarDir = File(mcDir, "soar")

        shindoDir = File(mcDir, "shindo")
        externalDir = File(shindoDir, "external")
        cacheDir = File(shindoDir, "cache")
        logsDir = File(cacheDir, "logs")

        musicDir = File(shindoDir, "music")
        profileDir = File(shindoDir, "profile")
        screenshotDir = File(shindoDir, "screenshots")
        addonsDir = File(shindoDir, "addons")
        addonConfigDir = File(addonsDir, "configs")
        gamesDir = File(shindoDir, "games")
        skinsDir = File(shindoDir, "skins")

        serversDir = File(shindoDir, "servers")

        customCapeDir = File(cacheDir, "custom-cape")
        capeCacheDir = File(cacheDir, "cape")
        profileIconDir = File(cacheDir, "profile-icon")
        mainLogFile = File(logsDir, "client.log")
        coreLogFile = File(logsDir, "core.log")
        modsLogFile = File(logsDir, "mods.log")
        chatLogFile = File(logsDir, "chat-friends.log")
        notificationLogFile = File(logsDir, "notifications.log")
        websocketLogFile = File(logsDir, "websocket.log")
        musicLogFile = File(logsDir, "music.log")
        discordLogFile = File(logsDir, "discord.log")
        uiLogFile = File(logsDir, "ui.log")
        securityLogFile = File(logsDir, "security.log")
        addonsLogFile = File(logsDir, "addons.log")
        profileLogFile = File(logsDir, "profile.log")

        try {
            if (!shindoDir.exists()) {
                if (soarDir.exists()) {
                    val migrated = soarDir.renameTo(shindoDir)
                    if (!migrated) createDir(shindoDir)
                } else {
                    createDir(shindoDir)
                }
            }

            createDir(externalDir)
            createDir(cacheDir)
            createDir(logsDir)

            createDir(musicDir)
            createDir(profileDir)
            createDir(screenshotDir)
            createDir(addonsDir)
            createDir(addonConfigDir)
            createDir(gamesDir)
            createDir(skinsDir)

            createDir(serversDir)

            createDir(customCapeDir)
            createDir(capeCacheDir)
            createDir(profileIconDir)
            createFile(mainLogFile)
            createFile(coreLogFile)
            createFile(modsLogFile)
            createFile(chatLogFile)
            createFile(notificationLogFile)
            createFile(websocketLogFile)
            createFile(musicLogFile)
            createFile(discordLogFile)
            createFile(uiLogFile)
            createFile(securityLogFile)
            createFile(addonsLogFile)
            createFile(profileLogFile)

            createVersionFile()
        } catch (e: Exception) {
            ShindoLogger.error("Failed to prepare shindo directory; this may cause crashes", e)
        }
    }

    private fun createVersionFile() {
        val versionDir = File(cacheDir, "version")
        createDir(versionDir)
        createFile(File(versionDir, "${Shindo.getInstance().getVerIdentifier()}.tmp"))
    }

    fun createDir(file: File?) {
        if (file != null && !file.exists()) {
            file.mkdirs()
        }
    }

    fun createFile(file: File) {
        try {
            file.createNewFile()
        } catch (e: IOException) {
            ShindoLogger.error("Failed to create file ${file.name}", e)
        }
    }
}
