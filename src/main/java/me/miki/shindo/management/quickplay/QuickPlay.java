package me.miki.shindo.management.quickplay;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class QuickPlay {

    private final String name;
    private final ResourceLocation icon;
    private ArrayList<QuickPlayCommand> commands = new ArrayList<QuickPlayCommand>();

    public QuickPlay(String name, ResourceLocation icon) {
        this.name = name;
        this.icon = icon;
        this.addCommands();
    }

    public void addCommands() {
    }

    public String getName() {
        return name;
    }

    public ArrayList<QuickPlayCommand> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<QuickPlayCommand> commands) {
        this.commands = commands;
    }

    public ResourceLocation getIcon() {
        return icon;
    }
}
