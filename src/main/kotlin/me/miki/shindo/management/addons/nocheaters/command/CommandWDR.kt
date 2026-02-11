package me.miki.shindo.management.addons.nocheaters.command

import me.miki.shindo.management.addons.nocheaters.NoCheatersAddon
import me.miki.shindo.management.addons.nocheaters.data.WDR
import me.miki.shindo.management.addons.nocheaters.data.WdrData
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

class CommandWDR : ICommand {

    override fun getCommandName(): String = "wdr"

    override fun getCommandUsage(sender: ICommandSender?): String = "/wdr <playername> [cheat1] [cheat2] ..."

    override fun getCommandAliases(): List<String> = listOf("watchdogreport")

    private val mc = Minecraft.getMinecraft()

    private fun sendChatMessage(component: net.minecraft.util.IChatComponent) {
        mc.ingameGUI?.chatGUI?.addToSentMessages(component.toString())
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            sendChatMessage(ChatComponentText("${EnumChatFormatting.RED}Usage: ${getCommandUsage(sender)}"))
            return
        }

        val playername = args[0]
        val cheats = if (args.size == 1) {
            listOf("cheating")
        } else {
            args.drop(1).toList()
        }

        val command = "/wdr $playername ${cheats.joinToString(" ")}"
        mc.thePlayer?.sendChatMessage(command)

        addPlayerToReportList(playername, cheats)
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos?
    ): List<String> {
        if (args.size == 1) {

            val netHandler = mc.netHandler ?: return emptyList()
            return netHandler.playerInfoMap
                .map { it.gameProfile.name }
                .filter { it.startsWith(args[0], ignoreCase = true) }
                .sorted()
        }
        if (args.size > 1) {

            val validCheats = listOf(
                "killaura", "ka", "aimbot", "reach", "autoclicker", "ac",
                "fly", "speed", "nofall", "scaffold", "blink", "noslow",
                "antikb", "velocity", "bhop", "timer", "cheating"
            )
            return validCheats.filter { it.startsWith(args.last(), ignoreCase = true) }
        }
        return emptyList()
    }

    override fun isUsernameIndex(args: Array<String>, index: Int): Boolean = index == 0

    override fun compareTo(other: ICommand?): Int {
        return getCommandName().compareTo(other?.commandName ?: "")
    }

    private fun addPlayerToReportList(playername: String, cheats: List<String>) {
        val netHandler = mc.netHandler

        var foundOnline = false
        if (netHandler != null) {
            for (netInfo in netHandler.playerInfoMap) {
                if (netInfo.gameProfile.name.equals(playername, ignoreCase = true)) {
                    val uuid = netInfo.gameProfile.id
                    val team = netInfo.playerTeam
                    addPlayerToReportList(uuid, playername, team?.formatString(playername), cheats)
                    foundOnline = true
                    return
                }
            }
        }

        if (!foundOnline) {
            Multithreading.runAsync {
                try {
                    val nameUuidData = me.miki.shindo.libs.mojang.MojangApi.nameToUUID(playername)
                    TaskExecutor.runOnMainThread {
                        addPlayerToReportList(
                            nameUuidData.uuid,
                            nameUuidData.name,
                            null,
                            cheats
                        )
                    }
                } catch (e: me.miki.shindo.libs.hypixel.exceptions.MojangApiException) {

                    TaskExecutor.runOnMainThread {
                        addPlayerToReportList(null, playername, null, cheats)
                    }
                } catch (e: Exception) {
                    me.miki.shindo.logger.ShindoLogger.error(
                        "[NoCheaters] Failed to get UUID for $playername",
                        e
                    )
                    TaskExecutor.runOnMainThread {
                        addPlayerToReportList(null, playername, null, cheats)
                    }
                }
            }
        }
    }

    private fun addPlayerToReportList(
        uuid: UUID?,
        playername: String,
        formattedName: String?,
        cheats: List<String>
    ) {
        val wdr = WdrData.getWDR(uuid, playername)
        wdr == null

        if (wdr == null) {
            WdrData.put(uuid, playername, WDR(cheats))
        } else {
            wdr.addCheats(cheats)
        }

        NoCheatersAddon.instance.data.saveReportedPlayers()

        val isNicked = uuid == null || uuid.version() != 4
        val message = ChatComponentText(
            "${WarningMessages.getTagNoCheaters()}${EnumChatFormatting.GREEN}You reported " +
                    (if (isNicked) "${EnumChatFormatting.GREEN}the${EnumChatFormatting.DARK_PURPLE} nicked player " else "") +
                    "${EnumChatFormatting.RED}${formattedName ?: playername}${EnumChatFormatting.GREEN} and will receive warnings about this player in-game" +
                    (if (isNicked) " for the next 24 hours." else ".")
        )

        sendChatMessage(message)
    }
}
