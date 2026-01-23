package me.miki.shindo.management.addons.nocheaters.listener

import me.miki.shindo.management.addons.nocheaters.NoCheatersAddon
import me.miki.shindo.management.addons.nocheaters.warning.WarningMessages
import me.miki.shindo.management.addons.nocheaters.data.WdrData
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventEntityJoinWorld
import me.miki.shindo.management.event.impl.EventLoadWorld
import me.miki.shindo.utils.Multithreading
import me.miki.shindo.utils.concurrent.TaskExecutor
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumChatFormatting

/**
 * Listener para detectar quando jogadores entram no mundo
 * 
 * Funcionalidades:
 * - Detecta quando jogadores reportados entram
 * - Mostra avisos no chat
 * - Atualiza informações de jogadores
 * 
 * Extensível para:
 * - Detecção de nicks
 * - Verificação de UUID
 * - Integração com scoreboard
 * - Cache de informações
 */
class PlayerJoinListener {

    private var lastDeathTime = 0L
    private val mc = Minecraft.getMinecraft()

    @EventTarget
    fun onWorldLoad(event: EventLoadWorld) {
        // Limpa avisos após morte (evita spam)
        if (System.currentTimeMillis() - lastDeathTime > 5000L) {
            WarningMessages.clearWarningMessagesPrinted()
        }
    }

    @EventTarget
    fun onEntityJoinWorld(event: EventEntityJoinWorld) {
        val entity = event.entity
        if (entity !is EntityPlayer) return
        if (!NoCheatersAddon.instance.isToggled()) return
        if (!NoCheatersAddon.instance.enableWarningsSetting) return

        try {
            val player = entity as EntityPlayer
            val uuid = player.uniqueID
            val playername = player.name

            if (player is EntityPlayerSP) {
                // Delay para self (campos podem estar null no join)
                Multithreading.runAsync {
                    Thread.sleep(1000) // 1 segundo de delay
                    TaskExecutor.runOnMainThread {
                        checkAndWarnPlayer(uuid, playername, player)
                    }
                }
            } else {
                // Para outros jogadores, verifica imediatamente
                checkAndWarnPlayer(uuid, playername, player)
            }
        } catch (e: Exception) {
            me.miki.shindo.logger.ShindoLogger.error(
                "[NoCheaters] Exception when checking player ${entity.name}",
                e
            )
        }
    }

    /**
     * Verifica se há tela de morte aberta (para limpar avisos)
     */
    fun checkDeathScreen() {
        if (mc.currentScreen is GuiGameOver) {
            lastDeathTime = System.currentTimeMillis()
        }
    }

    private fun checkAndWarnPlayer(uuid: java.util.UUID, playername: String, player: EntityPlayer) {
        val wdr = WdrData.getWDR(uuid, playername)
        if (wdr != null && NoCheatersAddon.instance.showWarningMessagesSetting) {
            val team = player.worldObj.getScoreboard()?.getPlayersTeam(playername)
            WarningMessages.printWarningMessage(uuid, team, playername, wdr)
        }
    }
}
