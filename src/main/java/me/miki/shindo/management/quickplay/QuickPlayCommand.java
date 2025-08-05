package me.miki.shindo.management.quickplay;

public class QuickPlayCommand {

    private final String name;
    private final String command;

    public QuickPlayCommand(String name, String command) {
        this.name = name;
        this.command = command;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }
}
