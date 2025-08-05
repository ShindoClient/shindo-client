package me.miki.shindo.management.command;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.command.impl.ScreenshotCommand;
import me.miki.shindo.management.command.impl.TranslateCommand;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventSendChat;

import java.util.ArrayList;

public class CommandManager {

    private final ArrayList<Command> commands = new ArrayList<Command>();

    public CommandManager() {

        commands.add(new ScreenshotCommand());
        commands.add(new TranslateCommand());

        Shindo.getInstance().getEventManager().register(this);
    }

    @EventTarget
    public void onSendChat(EventSendChat event) {

        if (event.getMessage().startsWith(".scmd")) {

            event.setCancelled(true);

            String[] args = event.getMessage().split(" ");

            if (args.length > 1) {
                for (Command c : commands) {
                    if (args[1].equals(c.getPrefix())) {
                        c.onCommand(event.getMessage().replace(".scmd ", "").replace(args[1] + " ", ""));
                    }
                }
            }
        }
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }
}
