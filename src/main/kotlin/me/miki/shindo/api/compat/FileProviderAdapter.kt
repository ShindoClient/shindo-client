package me.miki.shindo.api.compat

import me.miki.shindo.Shindo
import me.miki.client_api.file.IFileProvider
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

class FileProviderAdapter : IFileProvider {

    private val fileManager get() = Shindo.getInstance().fileManager

    override fun getAddonConfigDir(addonId: String): String {
        val dir = File(fileManager.addonConfigDir, sanitizeAddonId(addonId))
        if (!dir.exists()) dir.mkdirs()
        return dir.absolutePath
    }

    override fun readAddonFile(addonId: String, relativePath: String, charset: Charset): String? {
        return try {
            val file = getAddonFile(addonId, relativePath)
            if (file.exists()) String(Files.readAllBytes(file.toPath()), charset) else null
        } catch (_: Exception) {
            null
        }
    }

    override fun writeAddonFile(addonId: String, relativePath: String, content: String, charset: Charset): Boolean {
        return try {
            val file = getAddonFile(addonId, relativePath)
            file.parentFile?.mkdirs()
            Files.write(file.toPath(), content.toByteArray(charset))
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun addonFileExists(addonId: String, relativePath: String): Boolean {
        return getAddonFile(addonId, relativePath).exists()
    }

    override fun resolvePath(addonId: String, relativePath: String): String {
        return getAddonFile(addonId, relativePath).absolutePath
    }

    private fun getAddonFile(addonId: String, relativePath: String): File {
        val dir = File(fileManager.addonConfigDir, sanitizeAddonId(addonId))
        val file = File(dir, relativePath).normalize()
        return if (file.absolutePath.startsWith(dir.absolutePath)) file else File(dir, relativePath)
    }

    private fun sanitizeAddonId(id: String): String =
        id.replace(Regex("[^a-zA-Z0-9_-]"), "_").takeIf { it.isNotEmpty() } ?: "unknown"
}
