package me.miki.shindo

import me.miki.extensions.ExtensionLibrary
import me.miki.extensions.core.ExtensionManager
import me.miki.shindo.api.broadcast.BroadcastManager
import me.miki.shindo.api.chat.ChatManager
import me.miki.shindo.injection.mixin.ShindoTweaker
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.AddonManager
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.command.CommandManager
import me.miki.shindo.management.cosmetic.bandana.BandanaManager
import me.miki.shindo.management.cosmetic.cape.CapeManager
import me.miki.shindo.management.cosmetic.wing.WingManager
import me.miki.shindo.management.event.EventManager
import me.miki.shindo.management.file.FileManager
import me.miki.shindo.management.language.LanguageManager
import me.miki.shindo.management.mods.ModManager
import me.miki.shindo.management.mods.RestrictedMod
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.music.MusicManager
import me.miki.shindo.management.music.RomanizationManager
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.network.NetworkManager
import me.miki.shindo.management.notification.NotificationManager
import me.miki.shindo.management.profile.ProfileManager
import me.miki.shindo.management.profile.ProfileShareManager
import me.miki.shindo.management.quickplay.QuickPlayManager
import me.miki.shindo.management.remote.blacklists.BlacklistManager
import me.miki.shindo.management.remote.changelog.ChangelogManager
import me.miki.shindo.management.remote.discord.DiscordStats
import me.miki.shindo.management.remote.download.DownloadManager
import me.miki.shindo.management.remote.news.NewsManager
import me.miki.shindo.management.remote.update.Update
import me.miki.shindo.management.screenshot.ScreenshotManager
import me.miki.shindo.management.security.SecurityFeatureManager
import me.miki.shindo.management.shader.ShaderManager
import me.miki.shindo.management.skin.SkinManager
import me.miki.shindo.management.sound.Sound
import me.miki.shindo.management.sound.Sounds
import me.miki.shindo.management.waypoint.WaypointManager
import me.miki.shindo.ui.ClickEffects
import me.miki.shindo.ui.layout.UILayoutManager
import me.miki.shindo.utils.BuildInfo
import me.miki.shindo.utils.OptifineUtils
import me.miki.shindo.utils.render.EntityProjection
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding
import org.apache.commons.lang3.ArrayUtils

@Suppress("UNUSED")
class Shindo private constructor() {

    private val mc: Minecraft = Minecraft.getMinecraft()
    private val buildInfo: BuildInfo = BuildInfo.DEFAULT

    private val name: String = "Shindo"
    private val version: String = buildInfo.semver
    private val author: String = "MikiDevAHM"
    private val verIdentifier: Int = buildInfo.build

    private var started: Boolean = false
    private var updateNeeded: Boolean = false

    private lateinit var fileManager: FileManager
    private lateinit var languageManager: LanguageManager
    private lateinit var eventManager: EventManager
    private lateinit var networkManager: NetworkManager
    private lateinit var downloadManager: DownloadManager
    private lateinit var extensionManager: ExtensionManager

    private lateinit var shindoAPI: ShindoAPI
    private lateinit var discordStats: DiscordStats

    private lateinit var profileManager: ProfileManager
    private lateinit var profileShareManager: ProfileShareManager
    private lateinit var skinManager: SkinManager
    private lateinit var capeManager: CapeManager
    private lateinit var wingManager: WingManager
    private lateinit var bandanaManager: BandanaManager
    private lateinit var colorManager: ColorManager

    private lateinit var modManager: ModManager
    private lateinit var addonManager: AddonManager
    private lateinit var shaderManager: ShaderManager
    private lateinit var restrictedMod: RestrictedMod
    private lateinit var blacklistManager: BlacklistManager

    private lateinit var chatManager: ChatManager
    private lateinit var broadcastManager: BroadcastManager
    private lateinit var notificationManager: NotificationManager

    private lateinit var waypointManager: WaypointManager
    private lateinit var quickPlayManager: QuickPlayManager
    private lateinit var clickEffects: ClickEffects
    private lateinit var securityFeatureManager: SecurityFeatureManager
    private lateinit var romanizationManager: RomanizationManager

    private lateinit var uiLayoutManager: UILayoutManager
    private lateinit var musicManager: MusicManager
    private lateinit var screenshotManager: ScreenshotManager

    private lateinit var commandManager: CommandManager
    private lateinit var changelogManager: ChangelogManager
    private lateinit var newsManager: NewsManager
    private lateinit var update: Update

    lateinit var nanoVGManager: NanoVGManager

    fun start() {
        ShindoLogger.info("Starting Shindo")
        try {
            OptifineUtils.disableFastRender()
            removeOptifineZoom()
        } catch (e: Exception) {
            ShindoLogger.error("Optifine Load Error", e)
        }
        blacklistManager = BlacklistManager()
        restrictedMod = RestrictedMod()
        try {
            restrictedMod.shouldCheck = !System.getProperty("me.miki.shindo.blacklistchecks", "true").equals("false", ignoreCase = true)
        } catch (e: Exception) {
            ShindoLogger.error("Restriction System load Error", e)
        }

        fileManager = FileManager()
        languageManager = LanguageManager()
        eventManager = EventManager()
        extensionManager = ExtensionManager().also {
            ExtensionLibrary.bootstrap(it)
        }

        downloadManager = DownloadManager()
        modManager = ModManager()
        addonManager = AddonManager()

        modManager.init()
        addonManager.init()

        notificationManager = NotificationManager()
        capeManager = CapeManager()
        wingManager = WingManager()
        bandanaManager = BandanaManager()
        colorManager = ColorManager()
        uiLayoutManager = UILayoutManager()
        profileManager = ProfileManager()
        profileShareManager = ProfileShareManager()
        chatManager = ChatManager()
        broadcastManager = BroadcastManager()
        musicManager = MusicManager(fileManager)
        romanizationManager = RomanizationManager()
        skinManager = SkinManager()

        networkManager = NetworkManager()
        networkManager.init()

        shindoAPI = ShindoAPI()
        shindoAPI.init()

        commandManager = CommandManager()
        screenshotManager = ScreenshotManager()

        securityFeatureManager = SecurityFeatureManager()
        quickPlayManager = QuickPlayManager()
        changelogManager = ChangelogManager()
        waypointManager = WaypointManager()
        newsManager = NewsManager()
        discordStats = DiscordStats().also { it.check() }
        update = Update().also { it.check() }

        eventManager.register(EntityProjection.getInstance())
        eventManager.register(ShindoHandler())

        InternalSettingsMod.instance.setToggled(true)
        InternalSettingsMod.instance.applyBorderlessOnStartup()

        clickEffects = ClickEffects()
        shaderManager = ShaderManager().also { it.init() }
        started = true
        mc.updateDisplay()

        shindoAPI.start()
    }

    fun stop() {
        ShindoLogger.info("Stopping Shindo")

        started = false
        nanoVGManager.destroy()
        profileManager.save()
        shindoAPI.stop()

        shaderManager.cleanup()
        Sound.play(Sounds.SHINDO_AUDIO_CLOSE, true)

        romanizationManager.shutdown()
    }

    private fun removeOptifineZoom() {
        if (ShindoTweaker.hasOptifine) {
            try {
                unregisterKeybind(GameSettings::class.java.getField("ofKeyBindZoom").get(mc.gameSettings) as KeyBinding)
            } catch (e: Exception) {
                ShindoLogger.error("Failed to unregister zoom key", e)
            }
        }
    }

    private fun unregisterKeybind(key: KeyBinding) {
        if (listOf(*mc.gameSettings.keyBindings).contains(key)) {
            mc.gameSettings.keyBindings =
                ArrayUtils.remove(mc.gameSettings.keyBindings, listOf(*mc.gameSettings.keyBindings).indexOf(key))
            key.keyCode = 0
        }
    }

    companion object {
        private val instance: Shindo = Shindo()

        @JvmStatic
        fun getInstance(): Shindo = instance
    }

    fun getFileManager(): FileManager = fileManager
    fun getLanguageManager(): LanguageManager = languageManager
    fun getEventManager(): EventManager = eventManager
    fun getNetworkManager(): NetworkManager = networkManager
    fun getDownloadManager(): DownloadManager = downloadManager
    fun getExtensionManager(): ExtensionManager = extensionManager

    fun getShindoAPI(): ShindoAPI = shindoAPI
    fun getDiscordStats(): DiscordStats = discordStats

    fun getProfileManager(): ProfileManager = profileManager
    fun getProfileShareManager(): ProfileShareManager = profileShareManager
    fun getSkinManager(): SkinManager = skinManager
    fun getCapeManager(): CapeManager = capeManager
    fun getWingManager(): WingManager = wingManager
    fun getBandanaManager(): BandanaManager = bandanaManager
    fun getColorManager(): ColorManager = colorManager

    fun getModManager(): ModManager = modManager
    fun getAddonManager(): AddonManager = addonManager
    fun getShaderManager(): ShaderManager = shaderManager
    fun getRestrictedMod(): RestrictedMod = restrictedMod
    fun getBlacklistManager(): BlacklistManager = blacklistManager

    fun getChatManager(): ChatManager = chatManager
    fun getBroadcastManager(): BroadcastManager = broadcastManager
    fun getNotificationManager(): NotificationManager = notificationManager

    fun getWaypointManager(): WaypointManager = waypointManager
    fun getQuickPlayManager(): QuickPlayManager = quickPlayManager
    fun getClickEffects(): ClickEffects = clickEffects
    fun getSecurityFeatureManager(): SecurityFeatureManager = securityFeatureManager
    fun getRomanizationManager(): RomanizationManager = romanizationManager

    fun getUILayoutManager(): UILayoutManager = uiLayoutManager
    fun getMusicManager(): MusicManager = musicManager
    fun getScreenshotManager(): ScreenshotManager = screenshotManager

    fun getCommandManager(): CommandManager = commandManager
    fun getChangelogManager(): ChangelogManager = changelogManager
    fun getNewsManager(): NewsManager = newsManager
    fun getUpdate(): Update = update

    fun getName(): String = name
    fun getVersion(): String = version
    fun getAuthor(): String = author
    fun getVerIdentifier(): Int = verIdentifier

    fun getBuildInfo(): BuildInfo = buildInfo
    fun isUpdateNeeded(): Boolean = updateNeeded
    fun setUpdateNeeded(updateNeeded: Boolean) {
        this.updateNeeded = updateNeeded
    }

    fun hasStarted(): Boolean = started
}
