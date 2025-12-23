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

    init {
        val mcDir = Minecraft.getMinecraft().mcDataDir
        val soarDir = File(mcDir, "soar")

        shindoDir = File(mcDir, "shindo")
        externalDir = File(shindoDir, "external")
        cacheDir = File(shindoDir, "cache")

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

            createVersionFile()
        } catch (e: Exception) {
            ShindoLogger.error("Failed to prepare shindo directory; this may cause crashes", e)
        }
    }

    private fun createVersionFile() {
        val versionDir = File(cacheDir, "version")
        createDir(versionDir)
        createFile(File(versionDir, "${Shindo.getInstance().verIdentifier}.tmp"))
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
