package me.miki.shindo.management.command;

import net.minecraft.client.Minecraft;

public class Command {

    private final String prefix;
    public Minecraft mc = Minecraft.getMinecraft();

    public Command(String prefix) {
        this.prefix = prefix;
    }

    public void onCommand(String message) {
    }

    public String getPrefix() {
        return prefix;
    }
}
