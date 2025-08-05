package me.miki.shindo.management.quickplay.impl;

import me.miki.shindo.management.quickplay.QuickPlay;
import me.miki.shindo.management.quickplay.QuickPlayCommand;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class MainLobbyQuickPlay extends QuickPlay {

    public MainLobbyQuickPlay() {
        super("MainLobby", new ResourceLocation("shindo/icons/hypixel/MainLobby.png"));
    }

    @Override
    public void addCommands() {
        ArrayList<QuickPlayCommand> commands = new ArrayList<QuickPlayCommand>();

        commands.add(new QuickPlayCommand("Lobby", "/lobby main"));

        this.setCommands(commands);
    }
}