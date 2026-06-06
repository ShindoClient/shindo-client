package com.shindoclient.shindo.management.addons

import com.google.gson.Gson
import com.shindoclient.addon.api.ShindoAddon
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.addons.data.AddonJson
import com.shindoclient.shindo.management.addons.data.MinecraftVersionJson
import com.shindoclient.shindo.management.addons.data.VersionCheckResult
import com.shindoclient.shindo.management.file.FileManager
import java.io.File
import java.io.InputStreamReader
import java.net.URLClassLoader
import java.util.jar.JarFile

object AddonLoader {
    private val gson = Gson()
    private val loaders = mutableMapOf<String, URLClassLoader>()

    fun loadExternalAddons(
        fileManager: FileManager,
        addonManager: AddonManager,
    ) {
        val addonsDir = fileManager.addonsDir
        if (!addonsDir.exists()) {
            addonsDir.mkdirs()
            ShindoLogger.info("Created addons directory at ${addonsDir.absolutePath}")
            return
        }

        val jarFiles = addonsDir.listFiles { f -> f.extension.equals("jar", ignoreCase = true) } ?: emptyArray()
        if (jarFiles.isEmpty()) {
            ShindoLogger.info("No external addon JARs found")
            return
        }

        ShindoLogger.info("Found ${jarFiles.size} external addon JAR(s)")

        for (jarFile in jarFiles.sortedBy { it.name }) {
            loadAddonJar(jarFile, addonManager)
        }
    }

    private fun loadAddonJar(
        jarFile: File,
        addonManager: AddonManager,
    ) {
        val jarFileName = jarFile.name
        ShindoLogger.info("Loading addon JAR: $jarFileName")

        try {
            val jar = JarFile(jarFile)
            val jsonEntry =
                jar.getJarEntry("addon.json")
                    ?: run {
                        jar.close()
                        addonManager.registerFailedAddon(jarFileName, "Missing addon.json in JAR")
                        return
                    }

            val manifest: AddonJson
            try {
                manifest = gson.fromJson(InputStreamReader(jar.getInputStream(jsonEntry)), AddonJson::class.java)
            } catch (e: Exception) {
                jar.close()
                addonManager.registerFailedAddon(jarFileName, "Invalid addon.json: ${e.message}")
                return
            }
            jar.close()

            if (manifest.main.isBlank()) {
                addonManager.registerFailedAddon(jarFileName, "addon.json is missing 'main' field")
                return
            }

            when (val versionCheck = checkMinecraftVersion(manifest.minecraft)) {
                is VersionCheckResult.Incompatible -> {
                    addonManager.registerFailedAddon(
                        jarFileName,
                        "Incompatible Minecraft version: ${versionCheck.reason}",
                    )
                    return
                }

                is VersionCheckResult.Compatible -> {}
            }

            // Use the client's classloader as parent so AddonAPI types resolve correctly
            val classLoader =
                URLClassLoader(
                    arrayOf(jarFile.toURI().toURL()),
                    ExternalAddon::class.java.classLoader,
                )
            loaders[jarFileName] = classLoader

            val mainClass = classLoader.loadClass(manifest.main)
            val constructor = mainClass.getDeclaredConstructor()
            constructor.isAccessible = true
            val shindoAddon = constructor.newInstance() as ShindoAddon

            val externalAddon =
                ExternalAddon(
                    name = manifest.name.ifBlank { shindoAddon.info.name },
                    description = manifest.description.ifBlank { shindoAddon.info.description },
                    icon = manifest.icon.ifBlank { shindoAddon.info.icon },
                    typeName = manifest.type.ifBlank { shindoAddon.info.type },
                    shindoAddon = shindoAddon,
                    classLoader = classLoader,
                )

            addonManager.registerAddon(externalAddon)
            externalAddon.initAddon()
            ShindoLogger.info("Loaded addon: ${externalAddon.name} v${manifest.version}")
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load addon JAR: $jarFileName", e)
            addonManager.registerFailedAddon(jarFileName, e.message ?: "Unknown error")
        }
    }

    fun unloadAddon(jarFileName: String) {
        loaders.remove(jarFileName)?.close()
    }
}

private val CURRENT_MINECRAFT_VERSION = "1.8.9"

private fun checkMinecraftVersion(mc: MinecraftVersionJson?): VersionCheckResult {
    if (mc == null) return VersionCheckResult.Compatible
    val current = CURRENT_MINECRAFT_VERSION

    if (mc.version.isNotBlank() && mc.version != current) {
        return VersionCheckResult.Incompatible(
            "requires exact version ${mc.version}, current is $current",
        )
    }

    if (mc.minVersion.isNotBlank() && compareVersions(current, mc.minVersion) < 0) {
        return VersionCheckResult.Incompatible(
            "requires Minecraft >= ${mc.minVersion}, current is $current",
        )
    }

    if (mc.maxVersion.isNotBlank() && compareVersions(current, mc.maxVersion) > 0) {
        return VersionCheckResult.Incompatible(
            "requires Minecraft <= ${mc.maxVersion}, current is $current",
        )
    }

    return VersionCheckResult.Compatible
}

private fun compareVersions(
    a: String,
    b: String,
): Int {
    val partsA = a.split(".").map { it.toIntOrNull() ?: 0 }
    val partsB = b.split(".").map { it.toIntOrNull() ?: 0 }
    val maxLen = maxOf(partsA.size, partsB.size)
    for (i in 0 until maxLen) {
        val va = partsA.getOrElse(i) { 0 }
        val vb = partsB.getOrElse(i) { 0 }
        if (va != vb) return va.compareTo(vb)
    }
    return 0
}
