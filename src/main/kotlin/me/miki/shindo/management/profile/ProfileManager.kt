package me.miki.shindo.management.profile

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.config.AddonConfigRegistry
import me.miki.shindo.management.color.Theme
import me.miki.shindo.management.file.FileManager
import me.miki.shindo.management.language.Language
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.network.NetworkManager
import me.miki.shindo.management.network.proxy.CustomProxy
import me.miki.shindo.management.profile.mainmenu.BackgroundManager
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.impl.*
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.file.FileUtils
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.CopyOnWriteArrayList

class ProfileManager {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    companion object {
        private const val SENTINEL_ID = 999
        private const val DEFAULT_ID = -1
        private val INVALID_CUSTOM_ICON_VALUES = setOf("", "null")
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private val instance = Shindo.getInstance()
    private val gson = Gson()
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    val profiles: CopyOnWriteArrayList<Profile> = CopyOnWriteArrayList()
    val backgroundManager: BackgroundManager = BackgroundManager()

    var activeProfile: Profile? = null
        private set
    var defaultProfile: Profile? = null
        private set

    private var pendingActiveFile: File? = null
    private var allowBorderlessProfileLoad: Boolean = true

    init {
        loadProfiles(loadDefaultProfile = true)
    }

    // =========================================================================
    // Public API
    // =========================================================================

    @JvmOverloads
    fun loadProfiles(loadDefaultProfile: Boolean = true) {
        profiles.clear()

        val profileDir = instance.fileManager.profileDir.also { dir ->
            if (!dir.exists()) instance.fileManager.createDir(dir)
        }

        val defaultFile = File(profileDir, "Default.json")
        initDefaultFile(defaultFile, loadDefaultProfile)

        val builtDefault = buildProfileFromFile(defaultFile, DEFAULT_ID)
        defaultProfile = builtDefault
            ?: Profile(DEFAULT_ID, "", defaultFile, ProfileIcon.GRASS, null, ProfileType.ALL, null)
        profiles.add(defaultProfile!!)

        loadNonDefaultProfiles(profileDir, defaultFile)

        profiles.add(Profile(SENTINEL_ID, "", null, null, null))
        ShindoLogger.info("Total profiles loaded: ${profiles.size - 1}")

        synchronizeActiveProfile(loadDefaultProfile)
        allowBorderlessProfileLoad = false
    }

    @JvmOverloads
    fun load(file: File?, disableModsBefore: Boolean = false): Boolean {
        if (file == null || !file.exists()) {
            ShindoLogger.error("Profile file is null or missing: ${file?.absolutePath}")
            return false
        }

        if (disableModsBefore) instance.modManager.disableAll()

        return try {
            FileReader(file).use { reader ->
                val root = gson.fromJson(reader, JsonObject::class.java) ?: JsonObject()
                applyProfileJson(root)
            }
            pendingActiveFile = file
            resolveAndSetActiveProfile(file)
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load profile: ${file.name}", e)
            false
        }
    }

    fun delete(profile: Profile?) {
        profile ?: return
        profiles.remove(profile)
        profile.jsonFile?.takeIf { it.exists() }?.delete()
        profile.customIcon?.takeIf { it.exists() }?.delete()

        if (profile == activeProfile) {
            defaultProfile?.jsonFile?.let { load(it) } ?: run { activeProfile = null }
        }
    }

    @JvmOverloads
    fun save(
        file: File,
        serverIp: String?,
        type: ProfileType?,
        icon: ProfileIcon?,
        customIcon: File?,
        shareCode: String? = null
    ) {
        val resolvedShareCode = shareCode ?: readShareCode(file)
        writeProfile(file, buildProfileSnapshot(serverIp, type, icon, customIcon, resolvedShareCode))

        activeProfile?.takeIf { it.jsonFile?.canonicalPath == file.canonicalPath }?.apply {
            this.serverIp = serverIp ?: ""
            this.type = type ?: ProfileType.ALL
            this.customIcon = customIcon
            this.shareCode = resolvedShareCode
        }
    }

    fun save() {
        val target = activeProfile ?: defaultProfile ?: return
        val file = target.jsonFile ?: return
        writeProfile(file, buildProfileSnapshot(target.serverIp, target.type, target.icon, target.customIcon, target.shareCode))
    }

    fun readProfileJson(file: File?): JsonObject? {
        if (file == null || !file.exists()) return null
        return try {
            FileReader(file).use { reader ->
                gson.fromJson(reader, JsonObject::class.java) ?: JsonObject()
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to read profile JSON", e)
            null
        }
    }

    fun updateShareCode(profile: Profile?, code: String) {
        val file = profile?.jsonFile ?: return
        val root = readProfileJson(file) ?: JsonObject()
        val profileData = JsonUtils.getObjectProperty(root, "Profile Data") ?: JsonObject()
        profileData.addProperty("ShareCode", code)
        root.add("Profile Data", profileData)
        writeProfile(file, root)
        profile.shareCode = code
    }

    fun importProfileFromShare(name: String?, code: String?, json: JsonObject): File {
        val target = createUniqueProfileFile(name ?: "Shared Profile")
        if (!code.isNullOrBlank()) {
            val profileData = JsonUtils.getObjectProperty(json, "Profile Data") ?: JsonObject()
            profileData.addProperty("ShareCode", code)
            json.add("Profile Data", profileData)
        }
        writeProfile(target, json)
        loadProfiles(loadDefaultProfile = false)
        return target
    }

    // =========================================================================
    // Profile Loading Helpers
    // =========================================================================

    private fun initDefaultFile(defaultFile: File, loadDefault: Boolean) {
        if (!defaultFile.exists()) {
            save(defaultFile, "", ProfileType.ALL, ProfileIcon.GRASS, null)
        } else if (loadDefault) {
            load(defaultFile)
        }
    }

    private fun loadNonDefaultProfiles(profileDir: File, defaultFile: File) {
        val defaultPath = defaultFile.canonicalPath
        val files = profileDir.listFiles()
            ?.filter { it.canonicalPath != defaultPath && "json".equals(FileUtils.getExtension(it), ignoreCase = true) }
            ?: return

        val futures = files.mapIndexed { index, file ->
            TaskExecutor.runAsync(ThreadPoolType.IO) {
                try {
                    FileReader(file).use { gson.fromJson(it, JsonObject::class.java) ?: JsonObject() }
                } catch (e: Exception) {
                    ShindoLogger.error("Failed to read profile: ${file.name}", e)
                    null
                }
            }.thenApply { json -> json?.let { buildProfileFromJson(it, file, index) } }
        }

        futures.forEach { future ->
            try {
                future.get()?.let { profiles.add(it) }
                    ?: ShindoLogger.error("Failed to build a profile entry")
            } catch (e: Exception) {
                ShindoLogger.error("Failed to load profile", e)
            }
        }
    }

    private fun applyProfileJson(root: JsonObject) {
        val appJson    = JsonUtils.getObjectProperty(root, "Appearance") ?: JsonObject()
        val modJson    = JsonUtils.getObjectProperty(root, "Mods")       ?: JsonObject()
        val addonJson  = JsonUtils.getObjectProperty(root, "Addons")     ?: JsonObject()

        applyAppearance(appJson)
        applyMods(modJson)
        applyAddons(addonJson)
    }

    private fun applyAppearance(appJson: JsonObject) {
        val colorManager = instance.colorManager
        val accentName   = JsonUtils.getStringProperty(appJson, "Accent Color", "Teal Love")!!
        val themeId      = JsonUtils.getIntProperty(appJson, "Theme", Theme.LIGHT.getId())
        val bgId         = JsonUtils.getIntProperty(appJson, "Background", 0)
        val langId       = JsonUtils.getStringProperty(appJson, "Language", Language.ENGLISH.getId())!!

        colorManager.setCurrentColor(colorManager.getColorByName(accentName))
        colorManager.setTheme(Theme.getThemeById(themeId))
        backgroundManager.setCurrentBackground(backgroundManager.getBackgroundById(bgId))
        instance.languageManager.setCurrentLanguage(Language.getLanguageById(langId))
    }

    private fun applyMods(modJson: JsonObject) {
        for (mod in instance.modManager.getMods()) {
            val modObject = JsonUtils.getObjectProperty(modJson, mod.getNameKey()) ?: continue
            mod.setToggled(JsonUtils.getBooleanProperty(modObject, "Toggle", false))

            if (mod is HUDMod) {
                mod.setX(JsonUtils.getIntProperty(modObject, "X", 100))
                mod.setY(JsonUtils.getIntProperty(modObject, "Y", 100))
                mod.setWidth(JsonUtils.getIntProperty(modObject, "Width", 100))
                mod.setHeight(JsonUtils.getIntProperty(modObject, "Height", 100))
                mod.setScale(JsonUtils.getFloatProperty(modObject, "Scale", 1f))
            }

            instance.modManager.getSettingsByMod(mod)
                ?.let { applySettings(instance.fileManager, modObject, it) }
        }
    }

    private fun applyAddons(addonJson: JsonObject) {
        for (addon in instance.addonManager.addons) {
            val addonKey = addon.getConfigId()
            val addonObject = JsonUtils.getObjectProperty(addonJson, addonKey)
                ?: JsonUtils.getObjectProperty(addonJson, addon.name)
                ?: continue

            addon.setToggled(JsonUtils.getBooleanProperty(addonObject, "Toggle", addon.isToggled()), false)

            instance.addonManager.getSettingByAddon(addon)
                ?.let { applySettings(instance.fileManager, addonObject, it) }

            JsonUtils.getObjectProperty(addonObject, "Config")
                ?.let { AddonConfigRegistry.get(addonKey)?.fromJson(it) }
        }
    }

    // =========================================================================
    // Profile Snapshot (Save)
    // =========================================================================

    private fun buildProfileSnapshot(
        serverIp: String?,
        type: ProfileType?,
        icon: ProfileIcon?,
        customIcon: File?,
        shareCode: String?
    ): JsonObject {
        val root = JsonObject()

        root.add("Profile Data", buildProfileDataJson(serverIp, type, icon, customIcon, shareCode))
        root.add("Appearance", buildAppearanceJson())
        //root.add("Network", buildNetworkJson())
        root.add("Mods", buildModsJson())
        root.add("Addons", buildAddonsJson())

        return root
    }

    private fun buildProfileDataJson(
        serverIp: String?,
        type: ProfileType?,
        icon: ProfileIcon?,
        customIcon: File?,
        shareCode: String?
    ): JsonObject = JsonObject().apply {
        addProperty("Icon", (icon ?: ProfileIcon.GRASS).id)
        addProperty("Type", (type ?: ProfileType.ALL).id)
        addProperty("Server", serverIp ?: "")
        addProperty("CustomIcon", customIcon?.name ?: "")
        if (!shareCode.isNullOrBlank()) addProperty("ShareCode", shareCode)
    }

    private fun buildAppearanceJson(): JsonObject {
        val colorManager = instance.colorManager
        return JsonObject().apply {
            addProperty("Accent Color", colorManager.getCurrentColor().getName())
            addProperty("Theme", colorManager.getTheme().getId())
            addProperty("Background", backgroundManager.getCurrentBackground()!!.getId())
            addProperty("Language", instance.languageManager.getCurrentLanguage().getId())
        }
    }

    private fun buildModsJson(): JsonObject {
        val modJsonObject = JsonObject()
        for (mod in instance.modManager.getMods()) {
            val modObject = JsonObject().apply {
                addProperty("Toggle", mod.isToggled())
                if (mod is HUDMod) {
                    addProperty("X", mod.getX())
                    addProperty("Y", mod.getY())
                    addProperty("Width", mod.getWidth())
                    addProperty("Height", mod.getHeight())
                    addProperty("Scale", mod.getScale())
                }
            }
            instance.modManager.getSettingsByMod(mod)
                ?.let { buildSettingsJson(it) }
                ?.takeIf { it.size() > 0 }
                ?.let { modObject.add("Settings", it) }
            modJsonObject.add(mod.getNameKey(), modObject)
        }
        return modJsonObject
    }

    private fun buildAddonsJson(): JsonObject {
        val addonJsonObject = JsonObject()
        for (addon in instance.addonManager.addons) {
            val addonObject = JsonObject().apply {
                addProperty("Toggle", addon.isToggled())
            }
            AddonConfigRegistry.get(addon.getConfigId())
                ?.toJson()
                ?.takeIf { it.size() > 0 }
                ?.let { addonObject.add("Config", it) }
            instance.addonManager.getSettingByAddon(addon)
                ?.let { buildSettingsJson(it) }
                ?.takeIf { it.size() > 0 }
                ?.let { addonObject.add("Settings", it) }
            addonJsonObject.add(addon.getConfigId(), addonObject)
        }
        return addonJsonObject
    }

    private fun buildSettingsJson(settings: List<Setting>): JsonObject {
        val obj = JsonObject()
        for (setting in settings) {
            val key = setting.getNameKey()
            when (setting) {
                is ColorSetting    -> obj.addProperty(key, setting.getColor().rgb)
                is BooleanSetting  -> obj.addProperty(key, setting.isToggled())
                is ComboSetting    -> obj.addProperty(key, setting.getOption()!!.nameKey)
                is NumberSetting   -> obj.addProperty(key, setting.getValue())
                is TextSetting     -> obj.addProperty(key, setting.getText())
                is KeybindSetting  -> obj.addProperty(key, setting.getKeyCode())
                is ImageSetting    -> obj.addProperty(key, setting.getImage()?.name ?: "null")
                is SoundSetting    -> obj.addProperty(key, setting.getSound()?.name ?: "null")
                is CellGridSetting -> obj.add(key, setting.getCells()?.toCellGridJson() ?: JsonArray())
            }
        }
        return obj
    }

    // =========================================================================
    // Settings Application
    // =========================================================================

    private fun applySettings(fileManager: FileManager, modJson: JsonObject, settings: List<Setting>) {
        val settingsJson = JsonUtils.getObjectProperty(modJson, "Settings") ?: return

        settingsLoop@ for (setting in settings) {
            val key = setting.getNameKey()
            when (setting) {
                is ColorSetting   -> setting.setColor(ColorUtils.getColorByInt(JsonUtils.getIntProperty(settingsJson, key, Color.RED.rgb)))

                is BooleanSetting -> {
                    if (key == "borderlessFullscreenSetting" && !allowBorderlessProfileLoad) continue@settingsLoop
                    setting.setToggled(JsonUtils.getBooleanProperty(settingsJson, key, false))
                }

                is ComboSetting   -> setting.setOption(
                    setting.getOptionByNameKey(
                        JsonUtils.getStringProperty(settingsJson, key, setting.getDefaultOption()?.nameKey).toString()
                    )
                )

                is NumberSetting  -> setting.setValue(JsonUtils.getDoubleProperty(settingsJson, key, setting.getDefaultValue()))
                is TextSetting    -> setting.setText(JsonUtils.getStringProperty(settingsJson, key, setting.getDefaultText()) ?: "")
                is KeybindSetting -> setting.setKeyCode(JsonUtils.getIntProperty(settingsJson, key, setting.getDefaultKeyCode()))

                is ImageSetting   -> resolveFileFromCache(fileManager, "custom-image", settingsJson, key)
                    ?.let { setting.setImage(it) }

                is SoundSetting   -> resolveFileFromCache(fileManager, "custom-sound", settingsJson, key)
                    ?.let { setting.setSound(it) }

                is CellGridSetting -> settingsJson.getAsJsonArray(key)?.let { outer ->
                    setting.setCells(Array(outer.size()) { i ->
                        outer[i].asJsonArray.let { inner -> BooleanArray(inner.size()) { j -> inner[j].asBoolean } }
                    })
                }
            }
        }
    }

    // =========================================================================
    // Network Save / Load
    // =========================================================================

    private fun buildNetworkJson(): JsonObject {
        val networkManager = instance.networkManager
        return JsonObject().apply {
            addProperty("ProxyType", networkManager.getActiveProxyType().name)
            networkManager.getActiveCustomProxyId()?.let { addProperty("ActiveCustomProxyId", it) }
            add("CustomProxies", JsonArray().also { arr ->
                networkManager.proxyManager.getCustomProxies().forEach { proxy ->
                    arr.add(JsonObject().apply {
                        addProperty("Id", proxy.id)
                        addProperty("Name", proxy.name)
                        addProperty("PrimaryDNS", proxy.primaryDNS)
                        proxy.secondaryDNS?.let { addProperty("SecondaryDNS", it) }
                    })
                }
            })
        }
    }

    private fun loadNetworkSettings(networkJson: JsonObject) {
        val networkManager = instance.networkManager
        try {
            JsonUtils.getArrayProperty(networkJson, "CustomProxies")?.let { arr ->
                for (i in 0 until arr.size()) {
                    val obj = arr[i].asJsonObject
                    val proxy = CustomProxy(
                        id         = JsonUtils.getStringProperty(obj, "Id", null) ?: continue,
                        name       = JsonUtils.getStringProperty(obj, "Name", null) ?: continue,
                        primaryDNS = JsonUtils.getStringProperty(obj, "PrimaryDNS", null) ?: continue,
                        secondaryDNS = JsonUtils.getStringProperty(obj, "SecondaryDNS", null)
                    )
                    if (proxy.isValid()) networkManager.proxyManager.addProxy(proxy)
                }
            }

            val proxyType = try {
                NetworkManager.ProxyType.valueOf(
                    JsonUtils.getStringProperty(networkJson, "ProxyType", "SYSTEM_DEFAULT")!!
                )
            } catch (e: Exception) {
                NetworkManager.ProxyType.SYSTEM_DEFAULT
            }

            when (proxyType) {
                NetworkManager.ProxyType.CLOUDFLARE    -> networkManager.enableCloudflareProxy()
                NetworkManager.ProxyType.SYSTEM_DEFAULT -> networkManager.disableAllProxies()
                NetworkManager.ProxyType.CUSTOM -> {
                    JsonUtils.getStringProperty(networkJson, "ActiveCustomProxyId", null)
                        ?.let { networkManager.enableCustomProxy(it) }
                }
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load network settings", e)
        }
    }

    // =========================================================================
    // Profile Resolution Helpers
    // =========================================================================

    private fun synchronizeActiveProfile(forceDefault: Boolean) {
        pendingActiveFile?.let { pending ->
            getProfileByFile(pending)?.let {
                setActiveProfile(it)
                pendingActiveFile = null
                return
            }
        }

        if ((forceDefault || activeProfile == null) && defaultProfile != null) {
            setActiveProfile(defaultProfile)
            return
        }

        activeProfile?.jsonFile?.let { file ->
            getProfileByFile(file)?.let { setActiveProfile(it) }
        }
    }

    private fun resolveAndSetActiveProfile(file: File): Boolean {
        pendingActiveFile = file
        return getProfileByFile(file)?.let {
            setActiveProfile(it)
            pendingActiveFile = null
            true
        } ?: false
    }

    private fun setActiveProfile(profile: Profile?) {
        if (profile?.jsonFile == null) return
        activeProfile = profile
    }

    private fun getProfileByFile(file: File?): Profile? {
        file ?: return null
        val targetPath = file.canonicalPath
        return profiles.firstOrNull { it.jsonFile?.canonicalPath == targetPath }
            ?: defaultProfile?.takeIf { it.jsonFile?.canonicalPath == targetPath }
    }

    // =========================================================================
    // Profile Building
    // =========================================================================

    private fun buildProfileFromFile(file: File, id: Int): Profile? {
        if (!file.exists()) return null
        return try {
            FileReader(file).use { buildProfileFromJson(gson.fromJson(it, JsonObject::class.java) ?: JsonObject(), file, id) }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to build profile from: ${file.name}", e)
            null
        }
    }

    private fun buildProfileFromJson(root: JsonObject, file: File, id: Int): Profile {
        val profileData = JsonUtils.getObjectProperty(root, "Profile Data") ?: JsonObject()

        val serverIp = JsonUtils.getStringProperty(profileData, "Server", "") ?: ""
        val icon     = ProfileIcon.getIconById(JsonUtils.getIntProperty(profileData, "Icon", ProfileIcon.GRASS.id))
        val type     = ProfileType.getTypeById(JsonUtils.getIntProperty(profileData, "Type", ProfileType.ALL.id))

        val customIcon = (JsonUtils.getStringProperty(profileData, "CustomIcon", "") ?: "")
            .trim()
            .takeIf { it !in INVALID_CUSTOM_ICON_VALUES }
            ?.let { name -> File(instance.fileManager.profileIconDir, name).takeIf { it.exists() } }

        val shareCode = (JsonUtils.getStringProperty(profileData, "ShareCode", "") ?: "")
            .trim()
            .ifEmpty { null }

        return Profile(id, serverIp, file, icon, customIcon, type, shareCode)
    }

    // =========================================================================
    // File I/O Utilities
    // =========================================================================

    private fun writeProfile(file: File, jsonObject: JsonObject) {
        try {
            file.parentFile?.takeIf { !it.exists() }?.let { instance.fileManager.createDir(it) }
            FileWriter(file).use { prettyGson.toJson(jsonObject, it) }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to write profile: ${file.name}", e)
        }
    }

    private fun readShareCode(file: File): String? {
        val root = readProfileJson(file) ?: return null
        val profileData = JsonUtils.getObjectProperty(root, "Profile Data") ?: return null
        return (JsonUtils.getStringProperty(profileData, "ShareCode", "") ?: "").trim().ifEmpty { null }
    }

    private fun resolveFileFromCache(fileManager: FileManager, subDir: String, settingsJson: JsonObject, key: String): File? {
        val cacheDir = File(fileManager.cacheDir, subDir).takeIf { it.exists() } ?: return null
        val name = JsonUtils.getStringProperty(settingsJson, key, null) ?: return null
        return File(cacheDir, name).takeIf { it.exists() }
    }

    private fun createUniqueProfileFile(baseName: String): File {
        val profileDir = instance.fileManager.profileDir
        val sanitized  = sanitizeProfileName(baseName).ifEmpty { "Shared Profile" }
        var candidate  = File(profileDir, "$sanitized.json")
        var suffix     = 1
        while (candidate.exists()) {
            candidate = File(profileDir, "$sanitized ($suffix).json")
            suffix++
        }
        return candidate
    }

    private fun sanitizeProfileName(name: String): String {
        val cleaned = name.trim().replace(Regex("[^A-Za-z0-9 _-]"), "")
        return cleaned.take(48).ifEmpty { "Profile" }
    }

    // =========================================================================
    // Extension Utilities
    // =========================================================================

    private fun Array<BooleanArray>.toCellGridJson(): JsonArray = JsonArray().also { outer ->
        forEach { row ->
            outer.add(JsonArray().also { inner -> row.forEach { inner.add(it) } })
        }
    }
}