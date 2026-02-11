package me.miki.shindo.management.addons.nocheaters

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.Addon
import me.miki.shindo.management.addons.AddonType
import me.miki.shindo.management.addons.nocheaters.data.NoCheatersData
import me.miki.shindo.management.addons.nocheaters.data.WDR
import me.miki.shindo.management.addons.nocheaters.data.WdrData
import me.miki.shindo.management.addons.nocheaters.listener.PlayerJoinListener
import me.miki.shindo.management.addons.nocheaters.queue.ReportQueue
import me.miki.shindo.management.addons.nocheaters.warning.WarningMessages
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import java.util.*

class NoCheatersAddon : Addon(
    "NoCheaters",
    "Sistema de detecção e aviso de jogadores reportados",
    TranslateText.ADDON_NOCHEATERS_DESCRIPTION,
    LegacyIcon.SHIELD,
    AddonType.OTHER
) {

    companion object {
        @JvmStatic
        lateinit var instance: NoCheatersAddon
            private set
    }

    @Property(type = PropertyType.BOOLEAN, name = "Enable Warnings", category = "General", current = 1.0)
    @JvmField
    var enableWarningsSetting = true

    @Property(type = PropertyType.BOOLEAN, name = "Show Warning Messages", category = "Chat", current = 1.0)
    @JvmField
    var showWarningMessagesSetting = true

    @Property(type = PropertyType.BOOLEAN, name = "Show Icons in Tab", category = "Icons", current = 1.0)
    @JvmField
    var showIconsInTabSetting = true

    @Property(type = PropertyType.BOOLEAN, name = "Show Icons in Name", category = "Icons", current = 1.0)
    @JvmField
    var showIconsInNameSetting = true

    @Property(type = PropertyType.BOOLEAN, name = "Auto Report Queue", category = "Auto Report", current = 0.0)
    @JvmField
    var autoReportQueueSetting = false

    @Property(
        type = PropertyType.BOOLEAN,
        name = "Add Detected to Report List",
        category = "Auto Report",
        current = 0.0
    )
    @JvmField
    var addDetectedToReportListSetting = false

    lateinit var data: NoCheatersData
    lateinit var reportQueue: ReportQueue
    lateinit var warningMessages: WarningMessages
    lateinit var playerJoinListener: PlayerJoinListener

    init {
        instance = this
        ShindoLogger.info("[NoCheaters] Initializing addon...")
    }

    override fun setup() {
        super.setup()
        setHide(true)
        
        val configFile = java.io.File(
            Shindo.getInstance().fileManager.addonConfigDir,
            "nocheaters.json"
        )

        data = NoCheatersData(configFile)
        WdrData.initialize(data)

        reportQueue = ReportQueue()
        ReportQueue.INSTANCE = reportQueue

        warningMessages = WarningMessages
        playerJoinListener = PlayerJoinListener()

        registerCommands()

        ShindoLogger.info("[NoCheaters] Addon initialized successfully")
    }

    override fun onEnable() {
        super.onEnable()

        val eventManager = Shindo.getInstance().eventManager
        eventManager.register(playerJoinListener)
        eventManager.register(reportQueue)
        eventManager.register(data)

        ShindoLogger.info("[NoCheaters] Addon enabled")
    }

    override fun onDisable() {
        data.saveReportedPlayers()

        val eventManager = Shindo.getInstance().eventManager
        eventManager.unregister(playerJoinListener)
        eventManager.unregister(reportQueue)
        eventManager.unregister(data)

        super.onDisable()
        ShindoLogger.info("[NoCheaters] Addon disabled")
    }

    private fun registerCommands() {
        ShindoLogger.info("[NoCheaters] Commands will be registered automatically by Minecraft")
    }

    fun isPlayerReported(uuid: UUID?, playername: String?): Boolean {
        return data.getWDR(uuid, playername) != null
    }

    fun getPlayerWDR(uuid: UUID?, playername: String?): WDR? {
        return data.getWDR(uuid, playername)
    }

    fun addReportedPlayer(uuid: UUID?, playername: String?, cheats: List<String>) {
        data.put(uuid, playername, WDR(cheats))
    }

    fun removeReportedPlayer(uuid: UUID?, playername: String?): Boolean {
        return data.remove(uuid, playername) != null
    }
}
