package me.miki.shindo

import me.miki.shindo.injection.mixin.ShindoTweaker
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.AddonManager
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.command.CommandManager
import me.miki.shindo.management.cosmetic.cape.CapeManager
import me.miki.shindo.management.event.EventManager
import me.miki.shindo.management.file.FileManager
import me.miki.shindo.management.language.LanguageManager
import me.miki.shindo.management.layout.UILayoutManager
import me.miki.shindo.management.mods.ModManager
import me.miki.shindo.management.mods.RestrictedMod
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.music.MusicManager
import me.miki.shindo.management.music.RomanizationManager
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.network.NetworkManager
import me.miki.shindo.management.network.proxy.WarpProxyManager
import me.miki.shindo.management.notification.NotificationManager
import me.miki.shindo.management.profile.ProfileManager
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
import me.miki.shindo.management.waypoint.WaypointManager
import me.miki.shindo.ui.ClickEffects
import me.miki.shindo.utils.OptifineUtils
import me.miki.shindo.utils.Sound
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding
import org.apache.commons.lang3.ArrayUtils
import java.util.Arrays

class Shindo private constructor() {

    private val mc: Minecraft = Minecraft.getMinecraft()

    val name: String = "Shindo"
    val version: String = "5.1.10"
    val author: String = "MikiDevAHM"
    val verIdentifier: Int = 5110

    var nanoVGManager: NanoVGManager? = null
    private var started: Boolean = false

    var updateNeeded: Boolean = false

    lateinit var fileManager: FileManager
        private set
    lateinit var languageManager: LanguageManager
        private set
    lateinit var eventManager: EventManager
        private set
    lateinit var downloadManager: DownloadManager
        private set
    lateinit var modManager: ModManager
        private set
    lateinit var addonManager: AddonManager
        private set
    lateinit var capeManager: CapeManager
        private set
    lateinit var colorManager: ColorManager
        private set
    lateinit var profileManager: ProfileManager
        private set
    lateinit var commandManager: CommandManager
        private set
    lateinit var screenshotManager: ScreenshotManager
        private set
    lateinit var notificationManager: NotificationManager
        private set
    lateinit var securityFeatureManager: SecurityFeatureManager
        private set
    lateinit var uiLayoutManager: UILayoutManager
        private set
    lateinit var musicManager: MusicManager
        private set
    lateinit var quickPlayManager: QuickPlayManager
        private set
    lateinit var changelogManager: ChangelogManager
        private set
    lateinit var newsManager: NewsManager
        private set
    lateinit var discordStats: DiscordStats
        private set
    lateinit var waypointManager: WaypointManager
        private set
    lateinit var warpProxyManager: WarpProxyManager
        private set
    lateinit var connectionTweakerManager: NetworkManager
        private set
    lateinit var update: Update
        private set
    lateinit var clickEffects: ClickEffects
        private set
    lateinit var blacklistManager: BlacklistManager
        private set
    lateinit var restrictedMod: RestrictedMod
        private set
    lateinit var shaderManager: ShaderManager
        private set
    lateinit var romanizationManager: RomanizationManager
        private set
    lateinit var skinManager: SkinManager
        private set
    lateinit var shindoAPI: ShindoAPI
        private set

    fun hasStarted(): Boolean = started

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
            restrictedMod.shouldCheck =
                !System.getProperty("me.miki.shindo.blacklistchecks", "true").equals("false", ignoreCase = true)
        } catch (e: Exception) {
            ShindoLogger.error("Restriction System load Error", e)
        }

        fileManager = FileManager()
        languageManager = LanguageManager()
        eventManager = EventManager()
        downloadManager = DownloadManager()
        modManager = ModManager()
        addonManager = AddonManager()

        modManager.init()
        addonManager.init()

        warpProxyManager = WarpProxyManager()
        connectionTweakerManager = NetworkManager()

        capeManager = CapeManager()
        colorManager = ColorManager()
        uiLayoutManager = UILayoutManager()
        profileManager = ProfileManager()
        musicManager = MusicManager(fileManager)
        romanizationManager = RomanizationManager()
        skinManager = SkinManager()

        shindoAPI = ShindoAPI()
        shindoAPI.init()

        commandManager = CommandManager()
        screenshotManager = ScreenshotManager()
        notificationManager = NotificationManager()
        securityFeatureManager = SecurityFeatureManager()
        quickPlayManager = QuickPlayManager()
        changelogManager = ChangelogManager()
        waypointManager = WaypointManager()
        newsManager = NewsManager()
        discordStats = DiscordStats().also { it.check() }
        update = Update().also { it.check() }

        eventManager.register(ShindoHandler())

        InternalSettingsMod.getInstance().setToggled(true)
        InternalSettingsMod.getInstance().applyBorderlessOnStartup()
        clickEffects = ClickEffects()
        shaderManager = ShaderManager().also { it.init() }
        started = true
        mc.updateDisplay()

        shindoAPI.start()
    }

    fun stop() {
        ShindoLogger.info("Stopping Shindo")

        started = false

        profileManager.save()
        shindoAPI.stop()

        shaderManager.cleanup()

        Sound.play("shindo/audio/close.wav", true)

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
        if (Arrays.asList(*mc.gameSettings.keyBindings).contains(key)) {
            mc.gameSettings.keyBindings =
                ArrayUtils.remove(mc.gameSettings.keyBindings, Arrays.asList(*mc.gameSettings.keyBindings).indexOf(key))
            key.keyCode = 0
        }
    }

    companion object {
        private val instance: Shindo = Shindo()

        @JvmStatic
        fun getInstance(): Shindo = instance
    }
}
