package me.miki.shindo.management.quickplay.impl;

import me.miki.shindo.management.quickplay.QuickPlay;
import me.miki.shindo.management.quickplay.QuickPlayCommand;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class UHCQuickPlay extends QuickPlay {

    public UHCQuickPlay() {
        super("UHC", new ResourceLocation("shindo/icons/hypixel/UHC.png"));
    }

    @Override
    public void addCommands() {
        ArrayList<QuickPlayCommand> commands = new ArrayList<QuickPlayCommand>();

        commands.add(new QuickPlayCommand("Lobby", "/l hc"));
        commands.add(new QuickPlayCommand("Solo", "/play uhc_solo"));
        commands.add(new QuickPlayCommand("Teams", "/play uhc_teams"));
        commands.add(new QuickPlayCommand("Events Mode", "/play /play uhc_events"));

        this.setCommands(commands);
    }
}