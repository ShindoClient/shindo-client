package com.shindoclient.shindo.management.command

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.command.impl.ScreenshotCommand
import com.shindoclient.shindo.management.command.impl.TranslateCommand
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventSendChat

class CommandManager {
    private val commands =
        ArrayList<Command>().apply {
            add(ScreenshotCommand())
            add(TranslateCommand())
        }

    init {
        Shindo.getInstance().getEventManager().register(this)
    }

    @EventTarget
    fun onSendChat(event: EventSendChat) {
        if (!event.getMessage().startsWith("$")) return
        event.setCancelled(true)
        val raw = event.getMessage().removePrefix("$")
        val args = raw.split(" ")
        if (args.isNotEmpty() && args[0].isNotEmpty()) {
            val prefix = args[0]
            val cmdArgs = if (args.size > 1) args.drop(1).joinToString(" ") else ""
            commands.firstOrNull { it.getPrefix() == prefix }?.onCommand(cmdArgs)
        }
    }

    fun registerCommand(command: Command) {
        commands.add(command)
    }

    fun unregisterCommand(prefix: String) {
        commands.removeAll { it.getPrefix() == prefix }
    }

    fun getCommands(): ArrayList<Command> = commands

    /**
     * Returns all commands whose prefix starts with the given string.
     * Used by the autocomplete system to find matching commands as the user types.
     */
    fun getCommandsStartingWith(prefix: String): List<Command> =
        commands.filter { it.getPrefix().startsWith(prefix, ignoreCase = true) }
}
