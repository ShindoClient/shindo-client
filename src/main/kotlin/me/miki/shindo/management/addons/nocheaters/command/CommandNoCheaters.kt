package me.miki.shindo.management.addons.nocheaters.command

import me.miki.shindo.management.addons.nocheaters.NoCheatersAddon
import me.miki.shindo.management.addons.nocheaters.data.WDR
import me.miki.shindo.management.addons.nocheaters.data.WdrData
import me.miki.shindo.management.addons.nocheaters.warning.WarningMessages
import me.miki.shindo.utils.concurrent.TaskExecutor
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent
import java.util.*

/**
 * Comando /nocheaters
 * 
 * Funcionalidades:
 * - Lista jogadores reportados no mundo atual
 * - Mostra lista completa de reportes
 * - Ajuda e informações
 * 
 * Extensível para:
 * - Filtros e busca
 * - Exportação de dados
 * - Estatísticas
 */
class CommandNoCheaters : ICommand {

    override fun getCommandName(): String = "nocheaters"

    override fun getCommandUsage(sender: ICommandSender?): String = "/nocheaters [reportlist|help]"

    override fun getCommandAliases(): List<String> = emptyList()

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            WarningMessages.printReportMessagesForWorld(true)
            return
        }

        when (args[0].lowercase()) {
            "reportlist" -> {
                printReportList(args)
            }
            "help" -> {
                printCommandHelp(sender)
            }
            else -> {
                printCommandHelp(sender)
            }
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos?
    ): List<String> {
        if (args.size == 1) {
            return listOf("help", "reportlist").filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }

    override fun isUsernameIndex(args: Array<String>, index: Int): Boolean = false

    override fun compareTo(other: ICommand?): Int {
        return getCommandName().compareTo(other?.commandName ?: "")
    }

    private val mc = Minecraft.getMinecraft()
    
    private fun sendChatMessage(component: IChatComponent) {
        mc.ingameGUI?.chatGUI?.addToSentMessages(component.toString())
    }
    
    private fun printReportList(args: Array<String>) {
        val displayPage = if (args.size > 1) {
            try {
                args[1].toInt().coerceAtLeast(1)
            } catch (e: NumberFormatException) {
                sendChatMessage(ChatComponentText("${EnumChatFormatting.RED}Not a valid page number"))
                return
            }
        } else {
            1
        }

        val allWDRs = WdrData.getAllWDRs()
        if (allWDRs.isEmpty()) {
            sendChatMessage(ChatComponentText("${EnumChatFormatting.GREEN}You have no one reported!"))
            return
        }

        // Ordena por timestamp (mais recentes primeiro)
        val sortedWDRs = allWDRs.entries.toList().sortedByDescending { it.value.getTimestamp() }

        val itemsPerPage = 10
        val totalPages = (sortedWDRs.size + itemsPerPage - 1) / itemsPerPage

        if (displayPage > totalPages) {
            sendChatMessage(
                ChatComponentText(
                    "${EnumChatFormatting.RED}No reports to display, $totalPages page${if (totalPages == 1) "" else "s"} available."
                )
            )
            return
        }

        val startIndex = (displayPage - 1) * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, sortedWDRs.size)

        sendChatMessage(
            ChatComponentText("${EnumChatFormatting.GOLD}${EnumChatFormatting.BOLD}Report List (Page $displayPage/$totalPages)")
        )

        val hasHypixelApi = me.miki.shindo.libs.hypixel.HypixelApiKeyManager.hasApiKey()

        for (i in startIndex until endIndex) {
            val (key, wdr) = sortedWDRs[i]
            
            // Processa cada entrada (pode ser async se tiver API key)
            if (key is UUID && hasHypixelApi) {
                // Busca informações do Hypixel (async)
                me.miki.shindo.utils.Multithreading.runAsync {
                    try {
                        val playerData = me.miki.shindo.libs.hypixel.data.HypixelPlayerData(key)
                        val loginData = me.miki.shindo.libs.hypixel.parser.LoginData(playerData)
                        
                        TaskExecutor.runOnMainThread {
                            val timeSince = formatTimeSince(wdr.getTimestamp())
                            val message = ChatComponentText("${EnumChatFormatting.GRAY}- ")
                                .appendSibling(
                                    WarningMessages.getPlayernameWithHoverText(
                                        loginData.getFormattedName(),
                                        null,
                                        loginData.getDisplayName(),
                                        key.toString(),
                                        wdr
                                    )
                                )
                                .appendText("${EnumChatFormatting.GRAY} reported: ${EnumChatFormatting.YELLOW}$timeSince")
                                .appendSibling(wdr.getFormattedCheats())
                            
                            // Adiciona status online/offline
                            if (loginData.isOnline()) {
                                message.appendText(" ${EnumChatFormatting.GREEN}Online")
                            } else {
                                val lastLogout = loginData.getLastLogout()
                                val timeSinceLogout = formatTimeSince(lastLogout)
                                message.appendText(" ${EnumChatFormatting.GRAY}Last logout: ${EnumChatFormatting.YELLOW}$timeSinceLogout")
                            }
                            
                            sendChatMessage(message)
                        }
                    } catch (e: Exception) {
                        // Fallback para exibição simples
                        TaskExecutor.runOnMainThread {
                            printSimpleReportEntry(key, wdr)
                        }
                    }
                }
            } else {
                // Exibição simples (sem API ou nickname)
                printSimpleReportEntry(key, wdr)
            }
        }

        if (totalPages > 1) {
            sendChatMessage(
                ChatComponentText(
                    "${EnumChatFormatting.GRAY}Use ${EnumChatFormatting.YELLOW}/nocheaters reportlist ${if (displayPage < totalPages) displayPage + 1 else displayPage}${EnumChatFormatting.GRAY} for ${if (displayPage < totalPages) "next" else "this"} page"
                )
            )
        }
    }

    private fun printSimpleReportEntry(key: Any, wdr: WDR) {
        val mc = net.minecraft.client.Minecraft.getMinecraft()
        val name = when (key) {
            is UUID -> {
                try {
                    me.miki.shindo.libs.mojang.MojangApi.uuidToName(key)
                } catch (e: Exception) {
                    key.toString()
                }
            }
            is String -> "[Nick] $key"
            else -> key.toString()
        }
        val timeSince = formatTimeSince(wdr.getTimestamp())

        val message = ChatComponentText("${EnumChatFormatting.GRAY}- ${EnumChatFormatting.YELLOW}$name")
            .appendText("${EnumChatFormatting.GRAY} reported: ${EnumChatFormatting.YELLOW}$timeSince")
            .appendSibling(wdr.getFormattedCheats())

        sendChatMessage(message)
    }

    private fun printCommandHelp(sender: ICommandSender) {
        sendChatMessage(
            ChatComponentText("${EnumChatFormatting.RED}${getBar()}\n")
                .appendText("${EnumChatFormatting.GOLD}NoCheaters Help\n\n")
                .appendText("${EnumChatFormatting.YELLOW}${getCommandUsage(sender)}${EnumChatFormatting.GRAY} - ${EnumChatFormatting.AQUA}prints the list of reported players in your current world\n")
                .appendText("${EnumChatFormatting.YELLOW}${getCommandUsage(sender)} reportlist${EnumChatFormatting.GRAY} - ${EnumChatFormatting.AQUA}prints the list of reported players\n")
                .appendText("${EnumChatFormatting.RED}${getBar()}")
        )
    }

    private fun getBar(): String = "=================================================="

    private fun formatTimeSince(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val days = diff / (24 * 3600 * 1000)
        val hours = (diff % (24 * 3600 * 1000)) / (3600 * 1000)
        val minutes = (diff % (3600 * 1000)) / (60 * 1000)

        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    private val sender: ICommandSender
        get() = net.minecraft.client.Minecraft.getMinecraft().thePlayer
}
