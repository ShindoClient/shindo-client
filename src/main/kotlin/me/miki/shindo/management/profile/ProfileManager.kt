package me.miki.shindo.management.profile

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.AddonManager
import me.miki.shindo.management.addons.config.AddonConfigRegistry
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.Theme
import me.miki.shindo.management.file.FileManager
import me.miki.shindo.management.language.Language
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.mods.ModManager
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

    companion object {
        private const val SENTINEL_ID = 999
        private const val DEFAULT_ID = -1
    }

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
        loadProfiles(true)
    }

    @JvmOverloads
    fun loadProfiles(loadDefaultProfile: Boolean = true) {
        profiles.clear()
        val fileManager = instance.fileManager
        val profileDir = fileManager.profileDir

        ShindoLogger.info("=== LOADING PROFILES ===")
        ShindoLogger.info("Profile directory: ${profileDir.absolutePath}")
        ShindoLogger.info("Directory exists: ${profileDir.exists()}")

        try {
            if (!profileDir.exists()) {
                fileManager.createDir(profileDir)
                ShindoLogger.info("Created profile directory")
            }

            val defaultFile = File(profileDir, "Default.json")
            ShindoLogger.info("Default profile: ${defaultFile.absolutePath}")
            ShindoLogger.info("Default profile exists: ${defaultFile.exists()}")

            if (!defaultFile.exists()) {
                ShindoLogger.info("Creating default profile")
                save(defaultFile, "", ProfileType.ALL, ProfileIcon.GRASS, null)
            } else if (loadDefaultProfile) {
                ShindoLogger.info("Loading default profile")
                load(defaultFile)
            }

            val builtDefault = buildProfileFromFile(defaultFile, DEFAULT_ID)
            if (builtDefault != null) {
                defaultProfile = builtDefault
                profiles.add(builtDefault)
                ShindoLogger.info("Default profile added to list: ${builtDefault.name}")
            } else {
                ShindoLogger.warn("Failed to build default profile, falling back to placeholder entry")
                val fallback = Profile(DEFAULT_ID, "", defaultFile, ProfileIcon.GRASS, null, ProfileType.ALL, null)
                defaultProfile = fallback
                profiles.add(fallback)
                ShindoLogger.info("Default profile placeholder added to list: ${fallback.name}")
            }

            var id = 0
            val files = profileDir.listFiles()
            ShindoLogger.info("Found ${files?.size ?: 0} files in profile directory")

            val defaultCanonicalPath = defaultFile.canonicalPath

            val profileFiles = files?.filter { file ->
                file.canonicalPath != defaultCanonicalPath &&
                        "json".equals(FileUtils.getExtension(file), ignoreCase = true)
            } ?: emptyList()

            val profileFutures = profileFiles.mapIndexed { index, file ->
                val profileId = id + index
                TaskExecutor.runAsync(ThreadPoolType.IO) {
                    try {
                        ShindoLogger.info("Loading profile (parallel): ${file.name}")

                        FileReader(file).use { reader ->
                            gson.fromJson(reader, JsonObject::class.java) ?: JsonObject()
                        }
                    } catch (e: Exception) {
                        ShindoLogger.error("Failed to read profile file: ${file.name}", e)
                        null
                    }
                }.thenApply { json ->

                    if (json != null) {
                        buildProfileFromJson(json, file, profileId)
                    } else {
                        null
                    }
                }
            }

            profileFutures.forEach { future ->
                try {
                    future.get()?.let {
                        profiles.add(it)
                        ShindoLogger.info("Profile added: ${it.name}")
                    } ?: run {
                        ShindoLogger.error("Failed to build profile")
                    }
                } catch (e: Exception) {
                    ShindoLogger.error("Failed to load profile", e)
                }
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load profile metadata", e)
        }

        profiles.add(Profile(SENTINEL_ID, "", null, null, null))
        ShindoLogger.info("Total profiles loaded: ${profiles.size - 1}")
        synchronizeActiveProfile(loadDefaultProfile)
        allowBorderlessProfileLoad = false
    }

    @JvmOverloads
    fun load(file: File?, disableModsBefore: Boolean = false): Boolean {
        if (file == null) return false

        ShindoLogger.info("=== LOADING PROFILE ===")
        ShindoLogger.info("Profile file: ${file.absolutePath}")
        ShindoLogger.info("File exists: ${file.exists()}")

        if (!file.exists()) {
            ShindoLogger.error("Profile file does not exist!")
            return false
        }

        val modManager: ModManager = instance.modManager
        if (disableModsBefore) {
            modManager.disableAll()
        }

        try {
            FileReader(file).use { reader ->
                val root = gson.fromJson(reader, JsonObject::class.java) ?: JsonObject()
                ShindoLogger.info("JSON root loaded successfully")
                ShindoLogger.info("Root keys: ${root.keySet()}")

                val appJson = JsonUtils.getObjectProperty(root, "Appearance") ?: JsonObject()
                val modJson = JsonUtils.getObjectProperty(root, "Mods") ?: JsonObject()
                val addonJson = JsonUtils.getObjectProperty(root, "Addons") ?: JsonObject()
                val networkJson = JsonUtils.getObjectProperty(root, "Network") ?: JsonObject()

                ShindoLogger.info("Appearance keys: ${appJson.keySet()}")
                ShindoLogger.info("Mods keys: ${modJson.keySet()}")
                ShindoLogger.info("Addons keys: ${addonJson.keySet()}")
                ShindoLogger.info("Network keys: ${networkJson.keySet()}")

                val colorManager: ColorManager = instance.colorManager
                val accentColorName = JsonUtils.getStringProperty(appJson, "Accent Color", "Teal Love")
                val themeId = JsonUtils.getIntProperty(appJson, "Theme", Theme.LIGHT.getId())
                val backgroundId = JsonUtils.getIntProperty(appJson, "Background", 0)
                val languageId = JsonUtils.getStringProperty(appJson, "Language", Language.ENGLISH.getId())

                ShindoLogger.info("Loading appearance - Accent: $accentColorName, Theme: $themeId, Background: $backgroundId, Language: $languageId")

                colorManager.setCurrentColor(colorManager.getColorByName(accentColorName!!))
                colorManager.setTheme(Theme.getThemeById(themeId))
                backgroundManager.setCurrentBackground(backgroundManager.getBackgroundById(backgroundId))
                instance.languageManager.setCurrentLanguage(Language.getLanguageById(languageId!!))

                var modsLoaded = 0
                for (mod in modManager.getMods()) {
                    val modObject = JsonUtils.getObjectProperty(modJson, mod.getNameKey()) ?: continue
                    mod.setToggled(JsonUtils.getBooleanProperty(modObject, "Toggle", false))

                    if (mod.isToggled()) {
                        modsLoaded++
                    }

                    if (mod is HUDMod) {
                        mod.setX(JsonUtils.getIntProperty(modObject, "X", 100))
                        mod.setY(JsonUtils.getIntProperty(modObject, "Y", 100))
                        mod.setWidth(JsonUtils.getIntProperty(modObject, "Width", 100))
                        mod.setHeight(JsonUtils.getIntProperty(modObject, "Height", 100))
                        mod.setScale(JsonUtils.getFloatProperty(modObject, "Scale", 1f))
                    }

                    modManager.getSettingsByMod(mod)?.let { applySettings(instance.fileManager, modObject, it) }
                }

                ShindoLogger.info("Loaded $modsLoaded enabled mods")

                val addonManager: AddonManager = instance.addonManager
                var addonsLoaded = 0
                for (addon in addonManager.addons) {
                    val addonKey = addon.getConfigId()
                    // Fallback: só processa addons que existem no JSON; chaves órfãs (addons removidos) são ignoradas
                    val addonObject = JsonUtils.getObjectProperty(addonJson, addonKey)
                        ?: JsonUtils.getObjectProperty(addonJson, addon.name)
                        ?: continue

                    addon.setToggled(JsonUtils.getBooleanProperty(addonObject, "Toggle", addon.isToggled()), false)
                    if (addon.isToggled()) {
                        addonsLoaded++
                    }

                    addonManager.getSettingByAddon(addon)?.let { applySettings(instance.fileManager, addonObject, it) }

                    // Carrega IAddonConfigStorage (key-value) se existir
                    JsonUtils.getObjectProperty(addonObject, "Config")?.let { configJson ->
                        AddonConfigRegistry.get(addonKey)?.fromJson(configJson)
                    }
                }

                ShindoLogger.info("Loaded $addonsLoaded enabled addons")

                // Carrega configurações de rede
                loadNetworkSettings(networkJson)

                ShindoLogger.info("Profile loaded successfully!")
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load profile", e)
            return false
        }

        pendingActiveFile = file
        getProfileByFile(file)?.let {
            setActiveProfile(it)
            pendingActiveFile = null
            ShindoLogger.info("Active profile set to: ${it.name}")
            return true
        } ?: run {
            ShindoLogger.error("Failed to find profile in list after loading!")
            return false
        }
    }

    fun delete(profile: Profile?) {
        if (profile == null) return
        profiles.remove(profile)

        profile.jsonFile?.takeIf { it.exists() }?.delete()
        profile.customIcon?.takeIf { it.exists() }?.delete()

        if (profile == activeProfile) {
            defaultProfile?.jsonFile?.let { load(it) } ?: run {
                activeProfile = null
            }
        }
    }

    fun save(
        file: File,
        serverIp: String?,
        type: ProfileType?,
        icon: ProfileIcon?,
        customIcon: File?,
        shareCode: String? = null
    ) {
        ShindoLogger.info("=== SAVING PROFILE ===")
        ShindoLogger.info("File: ${file.absolutePath}")
        ShindoLogger.info("Server: $serverIp, Type: ${type?.id}, Icon: ${icon?.id}")

        val resolvedShareCode = shareCode ?: readShareCode(file)
        val snapshot = buildProfileSnapshot(serverIp, type, icon, customIcon, resolvedShareCode)
        writeProfile(file, snapshot)

        activeProfile?.takeIf { it.jsonFile?.canonicalPath == file.canonicalPath }?.let {
            it.serverIp = serverIp ?: ""
            it.type = type ?: ProfileType.ALL
            it.customIcon = customIcon
            it.shareCode = resolvedShareCode
            ShindoLogger.info("Updated active profile metadata")
        }
    }

    fun save() = saveActiveProfile()

    private fun saveActiveProfile() {
        val target = activeProfile ?: defaultProfile ?: return
        val file = target.jsonFile ?: return

        ShindoLogger.info("=== SAVING ACTIVE PROFILE ===")
        ShindoLogger.info("Profile: ${target.name}")
        ShindoLogger.info("File: ${file.absolutePath}")

        val snapshot =
            buildProfileSnapshot(target.serverIp, target.type, target.icon, target.customIcon, target.shareCode)
        writeProfile(file, snapshot)
    }

    private fun getProfileByFile(file: File?): Profile? {
        if (file == null) {
            ShindoLogger.warn("getProfileByFile called with null file")
            return null
        }

        val targetPath = file.canonicalPath
        ShindoLogger.info("Looking for profile with file: $targetPath")
        ShindoLogger.info("Profiles in list: ${profiles.size}")

        profiles.firstOrNull { it.jsonFile?.canonicalPath == targetPath }?.let {
            ShindoLogger.info("Found profile: ${it.name}")
            return it
        }

        if (defaultProfile?.jsonFile?.canonicalPath == targetPath) {
            ShindoLogger.info("Found as default profile")
            return defaultProfile
        }

        ShindoLogger.error("Profile not found! Available profiles:")
        profiles.forEach {
            ShindoLogger.info("  - ${it.name} (${it.jsonFile?.canonicalPath})")
        }

        return null
    }

    private fun synchronizeActiveProfile(forceDefault: Boolean) {
        ShindoLogger.info("=== SYNCHRONIZING ACTIVE PROFILE ===")
        ShindoLogger.info("Force default: $forceDefault")
        ShindoLogger.info("Pending active file: ${pendingActiveFile?.absolutePath}")
        ShindoLogger.info("Current active profile: ${activeProfile?.name}")

        pendingActiveFile?.let { pending ->
            ShindoLogger.info("Checking pending file: ${pending.name}")
            getProfileByFile(pending)?.let {
                ShindoLogger.info("Found pending profile in list: ${it.name}")
                setActiveProfile(it)
                pendingActiveFile = null
                return
            } ?: run {
                ShindoLogger.error("Pending profile not found in list!")
            }
        }

        if ((forceDefault || activeProfile == null) && defaultProfile != null) {
            ShindoLogger.info("Setting default profile as active")
            setActiveProfile(defaultProfile)
            return
        }

        activeProfile?.jsonFile?.let { currentFile ->
            ShindoLogger.info("Synchronizing current active profile: ${currentFile.name}")
            getProfileByFile(currentFile)?.let {
                setActiveProfile(it)
                ShindoLogger.info("Active profile synchronized: ${it.name}")
            } ?: run {
                ShindoLogger.error("Current active profile not found in list!")
            }
        }
    }

    private fun setActiveProfile(profile: Profile?) {
        if (profile?.jsonFile == null) {
            ShindoLogger.error("Cannot set active profile: profile or jsonFile is null")
            return
        }
        activeProfile = profile
        ShindoLogger.info("Active profile set: ${profile.name}")
    }

    private fun buildProfileFromFile(file: File, id: Int): Profile? {
        if (!file.exists()) {
            ShindoLogger.error("Profile file does not exist: ${file.absolutePath}")
            return null
        }

        return try {
            FileReader(file).use { reader ->
                val root = gson.fromJson(reader, JsonObject::class.java) ?: JsonObject()
                buildProfileFromJson(root, file, id)
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load profile from ${file.name}", e)
            null
        }
    }

    private fun buildProfileFromJson(root: JsonObject, file: File, id: Int): Profile {
        ShindoLogger.info("Building profile from: ${file.name}")
        ShindoLogger.info("Root keys: ${root.keySet()}")

        val profileData = JsonUtils.getObjectProperty(root, "Profile Data") ?: JsonObject()
        ShindoLogger.info("Profile Data keys: ${profileData.keySet()}")

        val serverIp = JsonUtils.getStringProperty(profileData, "Server", "") ?: ""
        val icon = ProfileIcon.getIconById(JsonUtils.getIntProperty(profileData, "Icon", ProfileIcon.GRASS.id))
        val type = ProfileType.getTypeById(JsonUtils.getIntProperty(profileData, "Type", ProfileType.ALL.id))

        ShindoLogger.info("Profile metadata - Server: '$serverIp', Icon: ${icon.id}, Type: ${type.id}")

        val customIconName = (JsonUtils.getStringProperty(profileData, "CustomIcon", "") ?: "")
            .trim()
            .takeIf { it.isNotEmpty() && !it.equals("null", true) }

        val customIcon = customIconName?.let { name ->
            val candidate = File(instance.fileManager.profileIconDir, name)
            if (candidate.exists()) candidate else null
        }

        if (customIconName != null) {
            if (customIcon != null) {
                ShindoLogger.info("Custom icon found: $customIconName")
            } else {
                ShindoLogger.warn("Custom icon not found: $customIconName")
            }
        }

        val shareCode = (JsonUtils.getStringProperty(profileData, "ShareCode", "") ?: "")
            .trim()
            .takeIf { it.isNotEmpty() }

        val profile = Profile(id, serverIp, file, icon, customIcon, type, shareCode)
        ShindoLogger.info("Profile built successfully: ${profile.name}")
        return profile
    }

    private fun buildProfileSnapshot(
        serverIp: String?,
        type: ProfileType?,
        icon: ProfileIcon?,
        customIcon: File?,
        shareCode: String?
    ): JsonObject {
        val modManager: ModManager = instance.modManager
        val addonManager: AddonManager = instance.addonManager
        val colorManager: ColorManager = instance.colorManager

        val jsonObject = JsonObject()
        val appJsonObject = JsonObject()
        val modJsonObject = JsonObject()
        val addonJsonObject = JsonObject()
        val profileData = JsonObject()

        val resolvedIcon = icon ?: ProfileIcon.GRASS
        val resolvedType = type ?: ProfileType.ALL

        profileData.addProperty("Icon", resolvedIcon.id)
        profileData.addProperty("Type", resolvedType.id)
        profileData.addProperty("Server", serverIp ?: "")
        profileData.addProperty("CustomIcon", customIcon?.name ?: "")
        if (!shareCode.isNullOrBlank()) {
            profileData.addProperty("ShareCode", shareCode)
        }

        jsonObject.add("Profile Data", profileData)

        appJsonObject.addProperty("Accent Color", colorManager.getCurrentColor().getName())
        appJsonObject.addProperty("Theme", colorManager.getTheme().getId())
        appJsonObject.addProperty("Background", backgroundManager.getCurrentBackground()!!.getId())
        appJsonObject.addProperty("Language", instance.languageManager.getCurrentLanguage().getId())

        jsonObject.add("Appearance", appJsonObject)

        // Salva configurações de rede
        val networkJsonObject = saveNetworkSettings()
        jsonObject.add("Network", networkJsonObject)

        for (mod in modManager.getMods()) {
            val modObject = JsonObject()
            modObject.addProperty("Toggle", mod.isToggled())

            if (mod is HUDMod) {
                modObject.addProperty("Toggle", mod.isToggled())
                modObject.addProperty("X", mod.getX())
                modObject.addProperty("Y", mod.getY())
                modObject.addProperty("Width", mod.getWidth())
                modObject.addProperty("Height", mod.getHeight())
                modObject.addProperty("Scale", mod.getScale())
            }

            modManager.getSettingsByMod(mod)?.let { settings ->
                val settingsObject = JsonObject()
                for (setting in settings) {
                    when (setting) {
                        is ColorSetting -> settingsObject.addProperty(setting.getNameKey(), setting.getColor().rgb)
                        is BooleanSetting -> settingsObject.addProperty(setting.getNameKey(), setting.isToggled())
                        is ComboSetting -> settingsObject.addProperty(
                            setting.getNameKey(),
                            setting.getOption()!!.nameKey
                        )

                        is NumberSetting -> settingsObject.addProperty(setting.getNameKey(), setting.getValue())
                        is TextSetting -> settingsObject.addProperty(setting.getNameKey(), setting.getText())
                        is KeybindSetting -> settingsObject.addProperty(setting.getNameKey(), setting.getKeyCode())
                        is ImageSetting -> settingsObject.addProperty(
                            setting.getNameKey(),
                            setting.getImage()?.name ?: "null"
                        )

                        is SoundSetting -> settingsObject.addProperty(
                            setting.getNameKey(),
                            setting.getSound()?.name ?: "null"
                        )

                        is CellGridSetting -> {
                            val outerArray = JsonArray()
                            setting.getCells()?.forEach { row ->
                                val inner = JsonArray()
                                row.forEach { cell -> inner.add(cell) }
                                outerArray.add(inner)
                            }
                            settingsObject.add(setting.getNameKey(), outerArray)
                        }
                    }
                }
                if (settingsObject.size() > 0) {
                    modObject.add("Settings", settingsObject)
                }
            }

            modJsonObject.add(mod.getNameKey(), modObject)
        }

        jsonObject.add("Mods", modJsonObject)

        for (addon in addonManager.addons) {
            val addonObject = JsonObject()
            addonObject.addProperty("Toggle", addon.isToggled())

            // Salva IAddonConfigStorage (key-value) se o addon tiver
            AddonConfigRegistry.get(addon.getConfigId())?.let { storage ->
                val configJson = storage.toJson()
                if (configJson.size() > 0) {
                    addonObject.add("Config", configJson)
                }
            }

            addonManager.getSettingByAddon(addon)?.let { settings ->
                val settingsObject = JsonObject()
                for (setting in settings) {
                    when (setting) {
                        is ColorSetting -> settingsObject.addProperty(setting.getNameKey(), setting.getColor().rgb)
                        is BooleanSetting -> settingsObject.addProperty(setting.getNameKey(), setting.isToggled())
                        is ComboSetting -> settingsObject.addProperty(
                            setting.getNameKey(),
                            setting.getOption()!!.nameKey
                        )

                        is NumberSetting -> settingsObject.addProperty(setting.getNameKey(), setting.getValue())
                        is TextSetting -> settingsObject.addProperty(setting.getNameKey(), setting.getText())
                        is KeybindSetting -> settingsObject.addProperty(setting.getNameKey(), setting.getKeyCode())
                        is ImageSetting -> settingsObject.addProperty(
                            setting.getNameKey(),
                            setting.getImage()?.name ?: "null"
                        )

                        is SoundSetting -> settingsObject.addProperty(
                            setting.getNameKey(),
                            setting.getSound()?.name ?: "null"
                        )

                        is CellGridSetting -> {
                            val outerArray = JsonArray()
                            setting.getCells()?.forEach { row ->
                                val inner = JsonArray()
                                row.forEach { cell -> inner.add(cell) }
                                outerArray.add(inner)
                            }
                            settingsObject.add(setting.getNameKey(), outerArray)
                        }
                    }
                }
                if (settingsObject.size() > 0) {
                    addonObject.add("Settings", settingsObject)
                }
            }

            addonJsonObject.add(addon.getConfigId(), addonObject)
        }

        jsonObject.add("Addons", addonJsonObject)

        return jsonObject
    }

    private fun applySettings(fileManager: FileManager, modJson: JsonObject, settings: List<Setting>) {
        val settingsJson = JsonUtils.getObjectProperty(modJson, "Settings") ?: return

        settingsLoop@ for (setting in settings) {
            when (setting) {
                is ColorSetting -> setting.setColor(
                    ColorUtils.getColorByInt(
                        JsonUtils.getIntProperty(
                            settingsJson,
                            setting.getNameKey(),
                            Color.RED.rgb
                        )
                    )
                )

                is BooleanSetting -> {
                    if (setting.getNameKey() == "borderlessFullscreenSetting" && !allowBorderlessProfileLoad) {
                        continue@settingsLoop
                    }
                    setting.setToggled(JsonUtils.getBooleanProperty(settingsJson, setting.getNameKey(), false))
                }

                is ComboSetting -> setting.setOption(
                    setting.getOptionByNameKey(
                        JsonUtils.getStringProperty(
                            settingsJson,
                            setting.getNameKey(),
                            setting.getDefaultOption()?.nameKey
                        ).toString()
                    )
                )

                is NumberSetting -> setting.setValue(
                    JsonUtils.getDoubleProperty(
                        settingsJson,
                        setting.getNameKey(),
                        setting.getDefaultValue()
                    )
                )

                is TextSetting -> setting.setText(
                    JsonUtils.getStringProperty(
                        settingsJson,
                        setting.getNameKey(),
                        setting.getDefaultText()
                    ) ?: ""
                )

                is KeybindSetting -> setting.setKeyCode(
                    JsonUtils.getIntProperty(
                        settingsJson,
                        setting.getNameKey(),
                        setting.getDefaultKeyCode()
                    )
                )

                is ImageSetting -> {
                    val cacheDir = File(fileManager.cacheDir, "custom-image")
                    if (cacheDir.exists()) {
                        JsonUtils.getStringProperty(settingsJson, setting.getNameKey(), null)?.let { name ->
                            val image = File(cacheDir, name)
                            if (image.exists()) {
                                setting.setImage(image)
                            }
                        }
                    }
                }

                is SoundSetting -> {
                    val cacheDir = File(fileManager.cacheDir, "custom-sound")
                    if (cacheDir.exists()) {
                        JsonUtils.getStringProperty(settingsJson, setting.getNameKey(), null)?.let { name ->
                            val sound = File(cacheDir, name)
                            if (sound.exists()) {
                                setting.setSound(sound)
                            }
                        }
                    }
                }

                is CellGridSetting -> {
                    settingsJson.getAsJsonArray(setting.getNameKey())?.let { outer ->
                        val cells = Array(outer.size()) { i ->
                            val inner = outer.get(i).asJsonArray
                            BooleanArray(inner.size()) { j -> inner.get(j).asBoolean }
                        }
                        setting.setCells(cells)
                    }
                }
            }
        }
    }

    private fun writeProfile(file: File, jsonObject: JsonObject) {
        try {
            ShindoLogger.info("Writing profile to: ${file.absolutePath}")
            file.parentFile?.takeIf { !it.exists() }?.let { instance.fileManager.createDir(it) }
            FileWriter(file).use { writer ->
                prettyGson.toJson(jsonObject, writer)
            }
            ShindoLogger.info("Profile written successfully, size: ${file.length()} bytes")
        } catch (e: Exception) {
            ShindoLogger.error("Failed to save profile", e)
        }
    }

    fun readProfileJson(file: File?): JsonObject? {
        if (file == null || !file.exists()) {
            return null
        }
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
        if (profile?.jsonFile == null) {
            return
        }
        val root = readProfileJson(profile.jsonFile) ?: JsonObject()
        val profileData = JsonUtils.getObjectProperty(root, "Profile Data") ?: JsonObject()
        profileData.addProperty("ShareCode", code)
        root.add("Profile Data", profileData)
        writeProfile(profile.jsonFile, root)
        profile.shareCode = code
    }

    fun importProfileFromShare(name: String?, code: String?, json: JsonObject): File {
        val baseName = sanitizeProfileName(name ?: "Shared Profile")
        val target = createUniqueProfileFile(baseName)
        if (!code.isNullOrBlank()) {
            val profileData = JsonUtils.getObjectProperty(json, "Profile Data") ?: JsonObject()
            profileData.addProperty("ShareCode", code)
            json.add("Profile Data", profileData)
        }
        writeProfile(target, json)
        loadProfiles(false)
        return target
    }

    private fun readShareCode(file: File): String? {
        val root = readProfileJson(file) ?: return null
        val profileData = JsonUtils.getObjectProperty(root, "Profile Data") ?: return null
        val raw = JsonUtils.getStringProperty(profileData, "ShareCode", "") ?: ""
        return raw.trim().ifEmpty { null }
    }

    private fun createUniqueProfileFile(baseName: String): File {
        val profileDir = instance.fileManager.profileDir
        var sanitized = sanitizeProfileName(baseName)
        if (sanitized.isEmpty()) {
            sanitized = "Shared Profile"
        }
        var candidate = File(profileDir, "$sanitized.json")
        var suffix = 1
        while (candidate.exists()) {
            candidate = File(profileDir, "$sanitized ($suffix).json")
            suffix++
        }
        return candidate
    }

    private fun sanitizeProfileName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return "Profile"
        }
        val cleaned = trimmed.replace(Regex("[^A-Za-z0-9 _-]"), "")
        return cleaned.take(48).ifEmpty { "Profile" }
    }

    /**
     * Salva as configurações de rede no JSON
     */
    private fun saveNetworkSettings(): JsonObject {
        val networkManager = instance.networkManager
        val networkJson = JsonObject()

        // Salva o tipo de proxy ativo
        val proxyType = networkManager.getActiveProxyType()
        networkJson.addProperty("ProxyType", proxyType.name)

        // Salva o ID do proxy customizado ativo (se houver)
        networkManager.getActiveCustomProxyId()?.let {
            networkJson.addProperty("ActiveCustomProxyId", it)
        }

        // Salva todos os proxies customizados
        val customProxiesArray = JsonArray()
        networkManager.proxyManager.getCustomProxies().forEach { proxy ->
            val proxyJson = JsonObject()
            proxyJson.addProperty("Id", proxy.id)
            proxyJson.addProperty("Name", proxy.name)
            proxyJson.addProperty("PrimaryDNS", proxy.primaryDNS)
            proxy.secondaryDNS?.let {
                proxyJson.addProperty("SecondaryDNS", it)
            }
            customProxiesArray.add(proxyJson)
        }
        networkJson.add("CustomProxies", customProxiesArray)

        return networkJson
    }


    private fun loadNetworkSettings(networkJson: JsonObject) {
        val networkManager = instance.networkManager

        try {
            val customProxiesArray = JsonUtils.getArrayProperty(networkJson, "CustomProxies")
            for (i in 0 until customProxiesArray.size()) {
                val proxyJson = customProxiesArray.get(i).asJsonObject
                val id = JsonUtils.getStringProperty(proxyJson, "Id", "null") ?: continue
                val name = JsonUtils.getStringProperty(proxyJson, "Name", "null") ?: continue
                val primaryDNS = JsonUtils.getStringProperty(proxyJson, "PrimaryDNS", "null") ?: continue
                val secondaryDNS = JsonUtils.getStringProperty(proxyJson, "SecondaryDNS", "null")

                val proxy = CustomProxy(
                    id = id,
                    name = name,
                    primaryDNS = primaryDNS,
                    secondaryDNS = secondaryDNS
                )

                if (proxy.isValid()) {
                    networkManager.proxyManager.addProxy(proxy)
                }
            }

            val proxyTypeStr = JsonUtils.getStringProperty(networkJson, "ProxyType", "SYSTEM_DEFAULT")
            val proxyType = try {
                NetworkManager.ProxyType.valueOf(proxyTypeStr!!)
            } catch (e: Exception) {
                NetworkManager.ProxyType.SYSTEM_DEFAULT
            }

            when (proxyType) {
                NetworkManager.ProxyType.CLOUDFLARE -> {
                    networkManager.enableCloudflareProxy()
                }
                NetworkManager.ProxyType.CUSTOM -> {
                    val activeProxyId = JsonUtils.getStringProperty(networkJson, "ActiveCustomProxyId", "null")
                    if (activeProxyId != null) {
                        networkManager.enableCustomProxy(activeProxyId)
                    }
                }
                NetworkManager.ProxyType.SYSTEM_DEFAULT -> {
                    networkManager.disableAllProxies()
                }
            }

            ShindoLogger.info("Network settings loaded - ProxyType: $proxyType")
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load network settings", e)
        }
    }
}
