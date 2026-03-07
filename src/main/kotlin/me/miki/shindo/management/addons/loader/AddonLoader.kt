package me.miki.shindo.management.addons.loader

import me.miki.shindo.addon.api.AddonMetadata
import me.miki.shindo.addon.api.AddonType as ApiAddonType
import me.miki.shindo.addon.api.ShindoAddon
import me.miki.shindo.Shindo
import me.miki.shindo.addon.runtime.bridge.ShindoAddonContextImpl
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.AddonManager
import me.miki.shindo.management.file.FileManager
import java.io.File
import java.net.URLClassLoader
import java.util.Properties
import java.util.jar.JarFile

/**
 * Carrega addons externos a partir de JARs na pasta shindo/addons/.
 * Cada JAR deve conter META-INF/addon.properties com addon.main apontando para a classe ShindoAddon.
 */
object AddonLoader {

    private const val ADDON_PROPERTIES = "META-INF/addon.properties"
    private const val PROP_MAIN = "addon.main"
    private const val PROP_ID = "addon.id"
    private const val PROP_VERSION = "addon.version"
    private const val PROP_NAME = "addon.name"
    private const val PROP_DESCRIPTION = "addon.description"
    private const val PROP_ICON = "addon.icon"
    private const val PROP_TYPE = "addon.type"
    private const val PROP_AUTHOR = "addon.author"
    private const val PROP_SHOW_TOGGLE = "addon.showToggle"

    fun loadExternalAddons(fileManager: FileManager, addonManager: AddonManager) {
        val addonsDir = fileManager.addonsDir
        if (!addonsDir.exists() || !addonsDir.isDirectory) {
            ShindoLogger.info("[ADDON] Pasta de addons não encontrada: ${addonsDir.absolutePath}")
            return
        }

        val jarFiles = addonsDir.listFiles { f -> f.isFile && f.name.endsWith(".jar") } ?: return

        val parentClassLoader = AddonLoader::class.java.classLoader

        for (jarFile in jarFiles) {
            try {
                loadAddonFromJar(jarFile, parentClassLoader, addonManager)
            } catch (e: Throwable) {
                e.printStackTrace()
                ShindoLogger.error("[ADDON] Nao foi possivel carregar ${jarFile.name}: ${e.message ?: e.toString()}", e)
                addonManager.registerFailedAddon(jarFile.name, e.message ?: e.toString())
            }
        }
    }

    private fun loadAddonFromJar(jarFile: File, parentClassLoader: ClassLoader, addonManager: AddonManager) {
        JarFile(jarFile).use { jar ->
            val entry = jar.getJarEntry(ADDON_PROPERTIES)
                ?: throw IllegalStateException("$ADDON_PROPERTIES não encontrado no JAR")

            jar.getInputStream(entry).use { stream ->
                val props = Properties()
                props.load(stream)

                val mainClass = props.getProperty(PROP_MAIN)
                    ?: throw IllegalStateException("$PROP_MAIN não definido em addon.properties")

                val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), parentClassLoader)
                val clazz = classLoader.loadClass(mainClass)

                if (!ShindoAddon::class.java.isAssignableFrom(clazz)) {
                    throw IllegalStateException("$mainClass não implementa ShindoAddon")
                }

                @Suppress("UNCHECKED_CAST")
                val addonClass = clazz as Class<ShindoAddon>
                val apiAddon = addonClass.getDeclaredConstructor().newInstance()

                val metadata = parseMetadata(props, apiAddon.getMetadata())
                val context = ShindoAddonContextImpl(metadata.id, Shindo.getInstance().serviceRegistry)
                val wrapper = ExternalAddonWrapper(apiAddon, metadata, context)
                addonManager.registerAddon(wrapper)

                apiAddon.onLoad(context)
                ShindoLogger.info("[ADDON] Carregado: ${metadata.name} v${metadata.version} (${metadata.id})")
            }
        }
    }

    private fun parseMetadata(props: Properties, fallback: AddonMetadata): AddonMetadata {
        val id = props.getProperty(PROP_ID, fallback.id)
        val version = props.getProperty(PROP_VERSION, fallback.version)
        val name = props.getProperty(PROP_NAME, fallback.name)
        val description = props.getProperty(PROP_DESCRIPTION, fallback.description)
        val icon = props.getProperty(PROP_ICON, fallback.icon)
        val typeStr = props.getProperty(PROP_TYPE, fallback.type.name)
        val type = try {
            ApiAddonType.valueOf(typeStr.toUpperCase())
        } catch (e: Exception) {
            ApiAddonType.OTHER
        }
        val author = props.getProperty(PROP_AUTHOR, fallback.author)
        val showToggle = props.getProperty(PROP_SHOW_TOGGLE, "true").toLowerCase() != "false"
        return AddonMetadata(id, version, name, description, icon, type, author, showToggle)
    }

}
