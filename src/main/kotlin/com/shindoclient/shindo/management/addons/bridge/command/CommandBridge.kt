package com.shindoclient.shindo.management.addons.bridge.command

import com.shindoclient.addon.api.command.AddonCommand
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.command.Command
import com.shindoclient.shindo.management.command.CommandManager

class CommandBridge(
    private val commandManager: CommandManager = Shindo.getInstance().getCommandManager(),
) {
    private val wrappedCommands = mutableListOf<Command>()

    fun registerCommand(addonCommand: AddonCommand) {
        val wrapped = wrapCommand(addonCommand)
        wrappedCommands.add(wrapped)
        commandManager.registerCommand(wrapped)
    }

    fun unregisterCommand(prefix: String) {
        wrappedCommands.removeAll { it.getPrefix() == prefix }
        commandManager.unregisterCommand(prefix)
    }

    fun unregisterAll() {
        for (cmd in wrappedCommands) {
            commandManager.unregisterCommand(cmd.getPrefix())
        }
        wrappedCommands.clear()
    }

    private fun wrapCommand(addonCommand: AddonCommand): Command {
        return object : Command(addonCommand.prefix) {
            override fun onCommand(message: String) {
                val args = message.split(" ").filter { it.isNotBlank() }
                try {
                    addonCommand.onCommand(args)
                } catch (e: Exception) {
                    val msg = "§c[Addon] Error executing command '${addonCommand.prefix}': ${e.message}"
                    mc.ingameGUI.chatGUI.printChatMessage(net.minecraft.util.ChatComponentText(msg))
                }
            }
        }
    }
}
