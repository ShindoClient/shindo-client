package me.miki.shindo.management.command

import me.miki.shindo.Shindo
import me.miki.shindo.management.command.impl.ScreenshotCommand
import me.miki.shindo.management.command.impl.TranslateCommand
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventSendChat

class CommandManager {

    private val commands = ArrayList<Command>().apply {
        add(ScreenshotCommand())
        add(TranslateCommand())
    }

    init {
        Shindo.getInstance().eventManager.register(this)
    }

    @EventTarget
    fun onSendChat(event: EventSendChat) {
        if (!event.message.startsWith(".scmd")) return
        event.setCancelled(true)
        val args = event.message.split(" ")
        if (args.size > 1) {
            val msg = event.message.replace(".scmd ", "").replace(args[1] + " ", "")
            commands.firstOrNull { args[1] == it.getPrefix() }?.onCommand(msg)
        }
    }

    fun getCommands(): ArrayList<Command> = commands
}
