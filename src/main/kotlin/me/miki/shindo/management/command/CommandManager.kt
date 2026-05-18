package me.miki.shindo.management.command

import me.miki.shindo.Shindo
import me.miki.shindo.management.command.impl.ScreenshotCommand
import me.miki.shindo.management.command.impl.TranslateCommand
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventSendChat

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
        if (!event.getMessage().startsWith(".scmd")) return
        event.setCancelled(true)
        val args = event.getMessage().split(" ")
        if (args.size > 1) {
            val msg = event.getMessage().replace(".scmd ", "").replace(args[1] + " ", "")
            commands.firstOrNull { args[1] == it.getPrefix() }?.onCommand(msg)
        }
    }

    fun getCommands(): ArrayList<Command> = commands
}
