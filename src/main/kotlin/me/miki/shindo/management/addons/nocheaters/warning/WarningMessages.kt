package me.miki.shindo.management.addons.nocheaters.warning

import me.miki.shindo.management.addons.nocheaters.data.WDR
import me.miki.shindo.management.addons.nocheaters.data.WdrData
import me.miki.shindo.utils.ColorUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.scoreboard.Team
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import net.minecraft.util.EnumChatFormatting
import java.util.*

/**
 * Gerencia mensagens de aviso do NoCheaters
 * 
 * Funcionalidades:
 * - Exibe avisos quando jogadores reportados entram
 * - Formata mensagens com informações de cheats
 * - Adiciona hover text com detalhes
 * 
 * Extensível para:
 * - Notificações customizadas
 * - Sons de alerta
 * - Overlay visual
 * - Integração com HUD
 */
object WarningMessages {

    private val warningMessagesPrinted = mutableSetOf<String>()

    /**
     * Limpa a lista de avisos já exibidos
     */
    fun clearWarningMessagesPrinted() {
        warningMessagesPrinted.clear()
    }

    /**
     * Imprime avisos para todos os jogadores reportados no mundo atual
     */
    fun printReportMessagesForWorld(callFromCommand: Boolean = false) {
        clearWarningMessagesPrinted()
        
        val mc = Minecraft.getMinecraft()
        val netHandler = mc.netHandler ?: return
        
        var foundReport = false
        
        // Itera sobre todos os jogadores online
        for (netInfo in netHandler.getPlayerInfoMap()) {
            val uuid = netInfo.gameProfile.id
            val playerName = netInfo.gameProfile.name
            val wdr = WdrData.getWDR(uuid, playerName)
            
            if (wdr == null) continue
            
            foundReport = true
            val team = netInfo.playerTeam
            printWarningMessage(uuid, team, playerName, wdr)
        }
        
        if (callFromCommand && !foundReport) {
            val message = ChatComponentText("${getTagNoCheaters()}${EnumChatFormatting.GREEN}No reported player here!")
            mc.ingameGUI?.chatGUI?.addToSentMessages(message.toString())
        }
    }

    /**
     * Imprime mensagem de aviso para um jogador específico
     */
    fun printWarningMessage(uuid: UUID, team: Team?, playername: String, wdr: WDR) {
        // Evita spam de mensagens
        val key = "$uuid-$playername"
        if (warningMessagesPrinted.contains(key)) return
        warningMessagesPrinted.add(key)

        val wdrmapKey = if (isRealPlayer(uuid)) uuid.toString() else playername
        
        val imsg = ChatComponentText("${EnumChatFormatting.RED}Warning: ")
            .appendSibling(
                getPlayernameWithHoverText(null, team, playername, wdrmapKey, wdr)
            )
            .appendText("${EnumChatFormatting.GRAY} joined, Cheats:")
            .appendSibling(wdr.getFormattedCheats())

        Minecraft.getMinecraft().ingameGUI?.chatGUI?.addToSentMessages(imsg.toString())
    }

    /**
     * Cria componente de texto com hover para o nome do jogador
     */
    fun getPlayernameWithHoverText(
        formattedName: String?,
        team: Team?,
        playername: String,
        wdrmapKey: String,
        wdr: WDR
    ): IChatComponent {
        val name = formattedName ?: getFormattedNameWithoutIcons(team, playername)
        
        return ChatComponentText(name).setChatStyle(
            ChatStyle()
                .setChatClickEvent(
                    ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unwdr $wdrmapKey $playername")
                )
                .setChatHoverEvent(getWDRHoverEvent(name, wdr))
        )
    }

    /**
     * Cria hover event com informações do WDR
     */
    private fun getWDRHoverEvent(formattedName: String, wdr: WDR): HoverEvent {
        val timeSince = formatTimeSince(wdr.getTimestamp())
        val localTime = formatLocalTime(wdr.getTimestamp())
        
        val hoverText = ChatComponentText(
            "$formattedName\n" +
            "${EnumChatFormatting.GREEN}Last reported: ${EnumChatFormatting.YELLOW}$timeSince ago, on $localTime\n" +
            "${EnumChatFormatting.GREEN}Reported for:${EnumChatFormatting.GOLD}${wdr.cheatsToString()}\n\n" +
            "${EnumChatFormatting.YELLOW}Click here to remove this player from your report list"
        )
        
        return HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)
    }

    /**
     * Obtém tag do NoCheaters
     */
    fun getTagNoCheaters(): String {
        return "${EnumChatFormatting.GOLD}[${EnumChatFormatting.DARK_GRAY}NoCheaters${EnumChatFormatting.GOLD}] ${EnumChatFormatting.RESET}"
    }


    private fun getFormattedNameWithoutIcons(team: Team?, playername: String): String {
        // Remove formatação de ícones (pode ser expandido)
        return team?.formatString(playername) ?: playername
    }

    private fun isRealPlayer(uuid: UUID): Boolean {
        return uuid.version() == 4
    }

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

    private fun formatLocalTime(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("MM/dd/yyyy HH:mm")
        return format.format(date)
    }
}
