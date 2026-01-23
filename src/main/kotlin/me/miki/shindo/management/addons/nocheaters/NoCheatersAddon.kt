package me.miki.shindo.management.addons.nocheaters

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.Addon
import me.miki.shindo.management.addons.AddonType
import me.miki.shindo.management.addons.nocheaters.command.CommandNoCheaters
import me.miki.shindo.management.addons.nocheaters.command.CommandUnWDR
import me.miki.shindo.management.addons.nocheaters.command.CommandWDR
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
import me.miki.shindo.management.event.EventManager

/**
 * Addon NoCheaters - Sistema de detecção e aviso de jogadores reportados
 * 
 * Baseado no sistema NoCheaters do MWE, este addon:
 * - Salva jogadores reportados via /wdr
 * - Avisa quando jogadores reportados entram no mundo
 * - Mantém uma lista de jogadores reportados
 * - Suporta reportes automáticos via fila
 * 
 * Estrutura extensível para melhorias futuras:
 * - Integração com APIs externas
 * - Sistema de sincronização entre clientes
 * - Detecção automática de cheats
 * - Estatísticas e relatórios
 */
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

    // Settings
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

    @Property(type = PropertyType.BOOLEAN, name = "Add Detected to Report List", category = "Auto Report", current = 0.0)
    @JvmField
    var addDetectedToReportListSetting = false

    // Managers
    lateinit var data: NoCheatersData
    lateinit var reportQueue: ReportQueue
    lateinit var warningMessages: WarningMessages
    lateinit var playerJoinListener: PlayerJoinListener

    init {
        instance = this
        ShindoLogger.info("[NoCheaters] Initializing addon...")
    }

    override fun setup() {
        // Inicializa componentes
        val configFile = java.io.File(
            Shindo.getInstance().fileManager.addonConfigDir,
            "nocheaters.json"
        )
        
        data = NoCheatersData(configFile)
        WdrData.initialize(data) // Inicializa singleton
        
        reportQueue = ReportQueue()
        ReportQueue.INSTANCE = reportQueue // Define instância estática
        
        warningMessages = WarningMessages
        playerJoinListener = PlayerJoinListener()

        // Registra comandos
        registerCommands()

        ShindoLogger.info("[NoCheaters] Addon initialized successfully")
    }

    override fun onEnable() {
        super.onEnable()
        
        // Registra listeners no EventManager do Shindo
        val eventManager = Shindo.getInstance().eventManager
        eventManager.register(playerJoinListener)
        eventManager.register(reportQueue)
        eventManager.register(data)

        ShindoLogger.info("[NoCheaters] Addon enabled")
    }

    override fun onDisable() {
        // Salva dados antes de desabilitar
        data.saveReportedPlayers()
        
        // Remove listeners do EventManager
        val eventManager = Shindo.getInstance().eventManager
        eventManager.unregister(playerJoinListener)
        eventManager.unregister(reportQueue)
        eventManager.unregister(data)

        super.onDisable()
        ShindoLogger.info("[NoCheaters] Addon disabled")
    }

    private fun registerCommands() {
        // Comandos do Minecraft (ICommand) são registrados automaticamente
        // Não precisam de registro manual, o Minecraft os detecta automaticamente
        // quando implementam ICommand corretamente
        ShindoLogger.info("[NoCheaters] Commands will be registered automatically by Minecraft")
    }

    /**
     * Verifica se um jogador está na lista de reportados
     */
    fun isPlayerReported(uuid: java.util.UUID?, playername: String?): Boolean {
        return data.getWDR(uuid, playername) != null
    }

    /**
     * Obtém informações de WDR de um jogador
     */
    fun getPlayerWDR(uuid: java.util.UUID?, playername: String?): WDR? {
        return data.getWDR(uuid, playername)
    }

    /**
     * Adiciona um jogador à lista de reportados
     */
    fun addReportedPlayer(uuid: java.util.UUID?, playername: String?, cheats: List<String>) {
        data.put(uuid, playername, WDR(cheats))
    }

    /**
     * Remove um jogador da lista de reportados
     */
    fun removeReportedPlayer(uuid: java.util.UUID?, playername: String?): Boolean {
        return data.remove(uuid, playername) != null
    }
}
