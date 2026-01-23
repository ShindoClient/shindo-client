package me.miki.shindo.management.addons.hackerdetector

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.Addon
import me.miki.shindo.management.addons.AddonType
import me.miki.shindo.management.addons.hackerdetector.checks.ICheck
import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.addons.hackerdetector.checks.*
import me.miki.shindo.management.addons.hackerdetector.data.BrokenBlock
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.data.TickingBlockMap
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.util.*

/**
 * HackerDetectorAddon - Sistema de detecção automática de cheats
 * 
 * Baseado no HackerDetector do MWE, este addon:
 * - Detecta vários tipos de cheats (KillAura, Fastbreak, Autoblock, etc.)
 * - Integra com NoCheaters para adicionar jogadores à lista de WDR
 * - Usa sistema de ViolationLevelTracker para evitar falsos positivos
 * - Rastreia dados dos jogadores através de PlayerDataSamples
 * 
 * Checks implementados:
 * - Autoblock: Detecta se jogador ataca enquanto bloqueia
 * - Fastbreak: Detecta quebra de blocos muito rápida
 * - Ghosthand: Detecta interação com blocos fora do alcance
 * - KeepSprint: Detecta se jogador mantém sprint após atacar
 * - KillAura: Detecta ataques em múltiplos alvos ou através de blocos
 * - NoSlowdown: Detecta se jogador não é desacelerado ao usar itens
 * - Scaffold: Detecta colocação de blocos muito rápida
 */
class HackerDetectorAddon : Addon(
    "HackerDetector",
    "Sistema de detecção automática de cheats",
    TranslateText.ADDON_HACKERDETECTOR_DESCRIPTION,
    LegacyIcon.SHIELD,
    AddonType.OTHER
) {
    
    companion object {
        @JvmStatic
        lateinit var instance: HackerDetectorAddon
            private set
        
        @JvmStatic
        fun log(message: String) {
            ShindoLogger.info("[HackerDetector] $message")
        }
        
        @JvmStatic
        fun isValidPlayer(uuid: UUID): Boolean {
            val v = uuid.version()
            // Versão 1 = online mode, versão 4 = UUID v4
            return v == 1 || v == 4
        }
    }

    @Property(type = PropertyType.BOOLEAN, name = "Show Flag Messages", category = "Messages", current = 1.0)
    @JvmField
    var showFlagMessagesSetting = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Show Flag Message Type", category = "Messages", current = 1.0)
    @JvmField
    var showFlagMessageTypeSetting = true
    
    @Property(type = PropertyType.BOOLEAN, name = "One Flag Message Per Game", category = "Messages", current = 0.0)
    @JvmField
    var oneFlagMessagePerGameSetting = false
    
    @Property(type = PropertyType.BOOLEAN, name = "Show Report Button on Flags", category = "Messages", current = 1.0)
    @JvmField
    var showReportButtonOnFlagsSetting = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Sound When Flagging", category = "General", current = 1.0)
    @JvmField
    var soundWhenFlaggingSetting = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Add Detected to Report List", category = "Auto Report", current = 1.0)
    @JvmField
    var addDetectedToReportListSetting = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Auto Report Flagged Players", category = "Auto Report", current = 0.0)
    @JvmField
    var autoReportFlaggedPlayersSetting = false
    
    @Property(type = PropertyType.BOOLEAN, name = "Debug Logging", category = "Debug", current = 0.0)
    @JvmField
    var debugLoggingSetting = false
    
    // Check Settings
    @Property(type = PropertyType.BOOLEAN, name = "Enable Autoblock Check", category = "Checks", current = 1.0)
    @JvmField
    var enableAutoblockCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable Fastbreak Check", category = "Checks", current = 1.0)
    @JvmField
    var enableFastbreakCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable Ghosthand Check", category = "Checks", current = 1.0)
    @JvmField
    var enableGhosthandCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable KeepSprint Check", category = "Checks", current = 1.0)
    @JvmField
    var enableKeepSprintCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable KillAura Check", category = "Checks", current = 1.0)
    @JvmField
    var enableKillAuraCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable NoSlowdown Check", category = "Checks", current = 1.0)
    @JvmField
    var enableNoSlowdownCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable Scaffold Check", category = "Checks", current = 1.0)
    @JvmField
    var enableScaffoldCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable Reach Check", category = "Checks", current = 1.0)
    @JvmField
    var enableReachCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable Fly Check", category = "Checks", current = 1.0)
    @JvmField
    var enableFlyCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable Speed Check", category = "Checks", current = 1.0)
    @JvmField
    var enableSpeedCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable Jesus Check", category = "Checks", current = 1.0)
    @JvmField
    var enableJesusCheck = true
    
    @Property(type = PropertyType.BOOLEAN, name = "Enable NoFall Check", category = "Checks", current = 1.0)
    @JvmField
    var enableNoFallCheck = true
    
    private val checkList: MutableList<ICheck> = mutableListOf()
    private val brokenBlocksList: MutableList<BrokenBlock> = mutableListOf()
    private val recentPlacedBlocks = TickingBlockMap()
    private val scheduledTasks: Queue<Runnable> = ArrayDeque()
    
    private var timeElapsedTemp = 0L
    private var timeElapsed = 0L
    private var playersChecked = 0
    private var playersCheckedTemp = 0
    
    private lateinit var fastbreakCheck: FastbreakCheck
    
    init {
        // Define instance ANTES do setup() ser chamado pelo init do Addon
        instance = this
        ShindoLogger.info("[HackerDetector] Initializing addon...")
        try {
            // Inicializa checks
            checkList.add(AutoblockCheck())
            checkList.add(FastbreakCheck(brokenBlocksList).also { fastbreakCheck = it })
            checkList.add(GhosthandCheck())
            checkList.add(KeepSprintACheck())
            checkList.add(KeepSprintBCheck())
            checkList.add(KillAuraACheck(recentPlacedBlocks))
            checkList.add(KillAuraBCheck())
            checkList.add(NoSlowdownCheck())
            checkList.add(ScaffoldCheck())
            checkList.add(ReachCheck())
            checkList.add(FlyCheck())
            checkList.add(SpeedCheck())
            checkList.add(JesusCheck())
            checkList.add(NoFallCheck())

            ShindoLogger.info("[HackerDetector] Addon initialized with ${checkList.size} checks")
        } catch (e: Exception) {
            ShindoLogger.error("[HackerDetector] Error initializing addon", e)
            throw e
        }
    }

    override fun onEnable() {
        super.onEnable()
        ShindoLogger.info("[HackerDetector] Addon enabled")
    }
    
    override fun onDisable() {
        super.onDisable()
        ShindoLogger.info("[HackerDetector] Addon disabled")
    }
    
    @EventTarget
    fun onTick(event: EventTick) {
        val mc = Minecraft.getMinecraft()
        if (mc.theWorld == null || mc.thePlayer == null || !mc.theWorld.isRemote) {
            synchronized(scheduledTasks) {
                scheduledTasks.clear()
            }
            return
        }
        
        // Verifica se fastbreakCheck foi inicializado
        if (!::fastbreakCheck.isInitialized) return
        
        // Processa tarefas agendadas
        synchronized(scheduledTasks) {
            while (scheduledTasks.isNotEmpty()) {
                scheduledTasks.poll().run()
            }
        }
        
        // Coleta lista de jogadores válidos
        val playerList = mutableListOf<EntityPlayer>()
        for (player in mc.theWorld.playerEntities) {
            if (player.ticksExisted >= 20 && !player.isDead && isValidPlayer(player.uniqueID)) {
                playerList.add(player)
                if (player is IMixinEntityPlayer) {
                    player.getPlayerDataSamples().onTickStart()
                }
            }
        }
        
        // Executa checks em cada jogador
        for (player in playerList) {
            performChecksOnPlayer(player)
        }
        
        // Finaliza tick
        fastbreakCheck.onTickEnd()
        brokenBlocksList.clear()
        recentPlacedBlocks.onTick()
        
        playersChecked = playersCheckedTemp
        playersCheckedTemp = 0
    }
    
    private fun performChecksOnPlayer(player: EntityPlayer) {
        val mc = Minecraft.getMinecraft()
        if (player == mc.thePlayer) {
            if (::fastbreakCheck.isInitialized) {
                fastbreakCheck.checkPlayerSP(player)
            }
            return
        }
        
        if (player !is IMixinEntityPlayer) return
        
        val data = player.getPlayerDataSamples()
        if (data.checkedThisTick) return
        
        data.onTick(player)
        for (check in checkList) {
            check.performCheck(player, data)
        }
        data.onPostChecks()
        playersCheckedTemp++
    }
    
    fun addScheduledTask(runnable: Runnable) {
        if (runnable == null) return
        synchronized(scheduledTasks) {
            scheduledTasks.add(runnable)
        }
    }
    
    fun addBrokenBlock(block: Block, blockPos: BlockPos, tool: String) {
        brokenBlocksList.add(BrokenBlock(block, blockPos, tool))
    }
    
    fun addPlacedBlock(pos: BlockPos, state: IBlockState) {
        val mc = Minecraft.getMinecraft()
        if (mc.thePlayer == null || mc.theWorld == null) return
        
        val xDiff = Math.abs(mc.thePlayer.posX - pos.x)
        val zDiff = Math.abs(mc.thePlayer.posZ - pos.z)
        if (xDiff > 70.0 || zDiff > 70.0) return
        
        if (!state.block.isFullBlock || !state.block.canCollideCheck(state, false)) return
        if (mc.theWorld.getBlockState(pos).block.material.isReplaceable) {
            recentPlacedBlocks.add(pos)
        }
    }
    
    fun onPlayerBlockPacket(pos: BlockPos, placedBlockDirectionIn: Int, block: Block) {
        if (block == null || !block.isFullBlock || !block.canCollideCheck(block.defaultState, false)) return
        
        val enumfacing = EnumFacing.getFront(placedBlockDirectionIn) ?: return
        recentPlacedBlocks.add(pos.add(enumfacing.directionVec))
    }
}
