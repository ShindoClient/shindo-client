package me.miki.shindo.management.addons.resourcify.core

import me.miki.shindo.management.addons.resourcify.model.ResourcifyCategory
import me.miki.shindo.management.addons.resourcify.model.ResourcifyConfig
import me.miki.shindo.management.addons.resourcify.model.ResourcifyDownloadResult
import me.miki.shindo.management.addons.resourcify.model.ResourcifyEntry
import me.miki.shindo.management.addons.resourcify.model.ResourcifyFilters
import me.miki.shindo.management.addons.resourcify.model.ResourcifyResourceType
import me.miki.shindo.management.addons.resourcify.model.ResourcifySearchPage
import me.miki.shindo.management.addons.resourcify.model.ResourcifyServiceType
import me.miki.shindo.management.addons.resourcify.model.ResourcifyUpdate
import me.miki.shindo.management.addons.resourcify.model.ResourcifyVersion
import me.miki.shindo.management.addons.resourcify.service.CurseForgeService
import me.miki.shindo.management.addons.resourcify.service.ModrinthService
import me.miki.shindo.management.addons.resourcify.service.ResourcifyService

import com.google.gson.GsonBuilder
import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.network.HttpUtils
import me.miki.shindo.utils.network.UserAgents
import net.minecraft.client.Minecraft
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Locale

class ResourcifyManager(private val configFile: File) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val lock = Any()
    private val services: List<ResourcifyService> = listOf(ModrinthService(), CurseForgeService())
    private val categoryCache = HashMap<String, List<ResourcifyCategory>>()
    private val versionCache = HashMap<ResourcifyServiceType, List<String>>()

    var config: ResourcifyConfig = ResourcifyConfig()
        private set

    init {
        load()
    }

    fun load() {
        synchronized(lock) {
            if (!configFile.exists()) {
                save()
                return
            }
            try {
                FileReader(configFile).use { reader ->
                    val loaded = gson.fromJson(reader, ResourcifyConfig::class.java)
                    if (loaded != null) {
                        config = loaded
                    }
                }
            } catch (e: Exception) {
                ShindoLogger.error("Failed to load Resourcify config", e)
            }
        }
    }

    fun save() {
        synchronized(lock) {
            try {
                configFile.parentFile?.mkdirs()
                FileWriter(configFile).use { writer ->
                    gson.toJson(config, writer)
                }
            } catch (e: Exception) {
                ShindoLogger.error("Failed to save Resourcify config", e)
            }
        }
    }

    fun getService(type: ResourcifyServiceType): ResourcifyService? {
        return services.find { it.serviceType == type }
    }

    fun getAvailableServices(): List<ResourcifyService> {
        return services.filter { it.isEnabled(config) }
    }

    fun updateCurseForgeApiKey(key: String?) {
        val trimmed = key?.trim()
        val next = if (trimmed.isNullOrEmpty()) null else trimmed
        synchronized(lock) {
            if (config.curseForgeApiKey == next) return
            config.curseForgeApiKey = next
            categoryCache.clear()
            versionCache.remove(ResourcifyServiceType.CURSEFORGE)
            save()
        }
    }

    fun search(
        serviceType: ResourcifyServiceType,
        query: String,
        type: ResourcifyResourceType,
        offset: Int,
        filters: ResourcifyFilters
    ): ResourcifySearchPage? {
        val service = getService(serviceType) ?: return null
        if (!service.isEnabled(config)) return null
        return service.search(config, query, type, offset, filters)
    }

    fun getLatestVersion(
        serviceType: ResourcifyServiceType,
        projectId: String,
        type: ResourcifyResourceType,
        version: String?
    ): ResourcifyVersion? {
        val service = getService(serviceType) ?: return null
        if (!service.isEnabled(config)) return null
        return service.getLatestVersion(config, projectId, type, version)
    }

    fun download(version: ResourcifyVersion, type: ResourcifyResourceType): ResourcifyDownloadResult {
        val folder = getResourceFolder(type)
        if (!folder.exists() && !folder.mkdirs()) {
            return ResourcifyDownloadResult(null, "Failed to create folder")
        }

        val existingEntry = findEntry(version.service, version.projectId, type)
        val targetFile = resolveTargetFile(folder, version.fileName, existingEntry)
        val ok = HttpUtils.downloadFile(version.downloadUrl, targetFile, UserAgents.MOZILLA)
        if (!ok) {
            return ResourcifyDownloadResult(null, "Download failed")
        }

        val entry = ResourcifyEntry(
            version.service,
            version.projectId,
            version.versionId,
            targetFile.name,
            targetFile.absolutePath,
            type,
            System.currentTimeMillis()
        )

        synchronized(lock) {
            config.entries.removeIf {
                it.service == version.service && it.projectId == version.projectId && it.type == type
            }
            config.entries.add(entry)
            save()
        }

        refreshResourcePacks(type)
        return ResourcifyDownloadResult(targetFile, null)
    }

    fun getEntries(type: ResourcifyResourceType? = null): List<ResourcifyEntry> {
        return if (type == null) config.entries.toList() else config.entries.filter { it.type == type }
    }

    fun checkUpdates(type: ResourcifyResourceType, versionFilter: String?, serviceType: ResourcifyServiceType? = null): List<ResourcifyUpdate> {
        val updates = ArrayList<ResourcifyUpdate>()
        pruneMissingEntries()
        val entries = getEntries(type)
        for (entry in entries) {
            if (serviceType != null && entry.service != serviceType) {
                continue
            }
            val version = getLatestVersion(entry.service, entry.projectId, type, versionFilter ?: DEFAULT_MC_VERSION)
            if (version == null || version.versionId == entry.versionId) {
                updates.add(ResourcifyUpdate(entry, null))
            } else {
                updates.add(ResourcifyUpdate(entry, version))
            }
        }
        return updates
    }

    fun getCategories(serviceType: ResourcifyServiceType, type: ResourcifyResourceType): List<ResourcifyCategory> {
        val key = serviceType.name + ":" + type.name
        val cached = categoryCache[key]
        if (cached != null) {
            return cached
        }
        val service = getService(serviceType) ?: return emptyList()
        if (!service.isEnabled(config)) return emptyList()
        val categories = service.getCategories(config, type)
        categoryCache[key] = categories
        return categories
    }

    fun getVersions(serviceType: ResourcifyServiceType): List<String> {
        val cached = versionCache[serviceType]
        if (cached != null) {
            return cached
        }
        val service = getService(serviceType) ?: return listOf(DEFAULT_MC_VERSION)
        if (!service.isEnabled(config)) return listOf(DEFAULT_MC_VERSION)
        val versions = service.getMinecraftVersions(config).toMutableList()
        if (!versions.contains(DEFAULT_MC_VERSION)) {
            versions.add(0, DEFAULT_MC_VERSION)
        }
        versionCache[serviceType] = versions
        return versions
    }

    private fun resolveTargetFile(folder: File, fileName: String, existingEntry: ResourcifyEntry?): File {
        if (existingEntry != null) {
            val existingFile = File(existingEntry.filePath)
            if (existingFile.parentFile != null && existingFile.parentFile.exists()) {
                return existingFile
            }
        }

        var sanitized = fileName
        if (!sanitized.endsWith(".zip", true) && !sanitized.endsWith(".jar", true)) {
            sanitized = "$sanitized.zip"
        }
        var target = File(folder, sanitized)
        if (!target.exists()) {
            return target
        }

        val base = sanitized.substringBeforeLast(".")
        val ext = sanitized.substringAfterLast(".", "")
        var index = 1
        while (target.exists()) {
            val name = String.format(Locale.ROOT, "%s-%d.%s", base, index, ext)
            target = File(folder, name)
            index++
        }
        return target
    }

    private fun getResourceFolder(type: ResourcifyResourceType): File {
        val mcDir = Shindo.getInstance().fileManager.shindoDir.parentFile
        val base = mcDir ?: Shindo.getInstance().fileManager.shindoDir
        return File(base, type.folderName)
    }

    private fun findEntry(service: ResourcifyServiceType, projectId: String, type: ResourcifyResourceType): ResourcifyEntry? {
        return config.entries.find {
            it.service == service && it.projectId == projectId && it.type == type
        }
    }

    private fun pruneMissingEntries() {
        synchronized(lock) {
            val before = config.entries.size
            config.entries.removeIf { !File(it.filePath).exists() }
            if (config.entries.size != before) {
                save()
            }
        }
    }

    private fun refreshResourcePacks(type: ResourcifyResourceType) {
        if (type != ResourcifyResourceType.RESOURCE_PACK) return
        try {
            Minecraft.getMinecraft().resourcePackRepository.updateRepositoryEntriesAll()
        } catch (e: Exception) {
            ShindoLogger.error("Failed to refresh resource pack list", e)
        }
    }

    companion object {
        const val DEFAULT_MC_VERSION = "1.8.9"
    }
}
