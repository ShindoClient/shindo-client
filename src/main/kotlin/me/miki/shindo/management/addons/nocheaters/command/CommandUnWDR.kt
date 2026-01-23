package me.miki.shindo.management.addons.nocheaters.command

import me.miki.shindo.management.addons.nocheaters.NoCheatersAddon
import me.miki.shindo.management.addons.nocheaters.data.WdrData
import me.miki.shindo.management.addons.nocheaters.queue.ReportQueue
import me.miki.shindo.management.addons.nocheaters.warning.WarningMessages
import me.miki.shindo.utils.Multithreading
import me.miki.shindo.utils.concurrent.TaskExecutor
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import java.util.*

/**
 * Comando /unwdr (Un-WatchDog Report)
 * 
 * Funcionalidades:
 * - Remove jogador da lista de reportados
 * - Para de mostrar avisos para o jogador
 * 
 * Extensível para:
 * - Confirmação antes de remover
 * - Histórico de remoções
 */
class CommandUnWDR : ICommand {

    override fun getCommandName(): String = "unwdr"

    override fun getCommandUsage(sender: ICommandSender?): String = "/unwdr <playername>"

    override fun getCommandAliases(): List<String> = emptyList()

    private val mc = Minecraft.getMinecraft()
    
    private fun sendChatMessage(component: net.minecraft.util.IChatComponent) {
        mc.ingameGUI?.chatGUI?.addToSentMessages(component.toString())
    }
    
    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty() || args.size > 2) {
            sendChatMessage(
                ChatComponentText("${EnumChatFormatting.RED}Usage: ${getCommandUsage(sender)}")
            )
            return
        }

        if (args.size == 1) {
            // /unwdr <playername>
            unwdrPlayer(args[0])
        } else {
            // /unwdr <uuid> <playername> (quando clica na mensagem)
            unwdr(args[0], args[1])
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos?
    ): List<String> {
        if (args.size == 1) {
            // Sugere jogadores reportados
            val allWDRs = WdrData.getAllWDRs()
            return allWDRs.keys
                .mapNotNull { key ->
                    when (key) {
                        is UUID -> {
                            // Tenta obter nome do UUID (pode ser expandido)
                            null
                        }
                        is String -> key
                        else -> null
                    }
                }
                .filter { it.startsWith(args[0], ignoreCase = true) }
                .sorted()
        }
        return emptyList()
    }

    override fun isUsernameIndex(args: Array<String>, index: Int): Boolean = index == 0

    override fun compareTo(other: ICommand?): Int {
        return getCommandName().compareTo(other?.getCommandName() ?: "")
    }

    private fun unwdrPlayer(playername: String) {
        // Tenta encontrar UUID primeiro
        val netHandler = mc.netHandler

        var foundOnline = false
        if (netHandler != null) {
            for (netInfo in netHandler.getPlayerInfoMap()) {
                if (netInfo.gameProfile.name.equals(playername, ignoreCase = true)) {
                    unwdr(netInfo.gameProfile.id.toString(), playername)
                    foundOnline = true
                    return
                }
            }
        }

        // Se não encontrou, tenta buscar UUID via API
        if (!foundOnline) {
            Multithreading.runAsync {
                try {
                    val nameUuidData = me.miki.shindo.libs.mojang.MojangApi.nameToUUID(playername)
                    TaskExecutor.runOnMainThread {
                        unwdr(nameUuidData.uuid.toString(), nameUuidData.name)
                    }
                } catch (e: me.miki.shindo.libs.hypixel.exceptions.MojangApiException) {
                    // Se não encontrou na API, remove apenas por nickname
                    TaskExecutor.runOnMainThread {
                        unwdr(null, playername)
                    }
                } catch (e: Exception) {
                    me.miki.shindo.logger.ShindoLogger.error(
                        "[NoCheaters] Failed to get UUID for $playername",
                        e
                    )
                    TaskExecutor.runOnMainThread {
                        unwdr(null, playername)
                    }
                }
            }
        }
    }

    private fun unwdr(uuidStr: String?, playername: String) {
        // Remove da fila de reportes
        ReportQueue.INSTANCE.removePlayerFromReportQueue(playername)

        // Tenta parsear UUID
        val uuid = try {
            uuidStr?.let { UUID.fromString(it) }
        } catch (e: IllegalArgumentException) {
            null
        }

        // Remove dos dados
        val wdr = WdrData.remove(uuid, playername)
        if (wdr == null) {
            sendChatMessage(
                ChatComponentText("${WarningMessages.getTagNoCheaters()}${EnumChatFormatting.RED}Player not found in your report list.")
            )
            return
        }

        // Salva dados
        NoCheatersAddon.instance.data.saveReportedPlayers()

        // Mostra mensagem de confirmação
        sendChatMessage(
            ChatComponentText("${WarningMessages.getTagNoCheaters()}${EnumChatFormatting.GREEN}You will no longer receive warnings for ${EnumChatFormatting.RED}$playername${EnumChatFormatting.GREEN}.")
        )
    }
}
