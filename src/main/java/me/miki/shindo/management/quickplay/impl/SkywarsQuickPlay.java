package me.miki.shindo.management.quickplay.impl;

import me.miki.shindo.management.quickplay.QuickPlay;
import me.miki.shindo.management.quickplay.QuickPlayCommand;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class SkywarsQuickPlay extends QuickPlay {

    public SkywarsQuickPlay() {
        super("Skywars", new ResourceLocation("shindo/icons/hypixel/Skywars.png"));
    }

    @Override
    public void addCommands() {

        ArrayList<QuickPlayCommand> commands = new ArrayList<QuickPlayCommand>();

        commands.add(new QuickPlayCommand("Lobby", "/l s"));
        commands.add(new QuickPlayCommand("Solo Normal", "/play solo_normal"));
        commands.add(new QuickPlayCommand("Solo Insane", "/play solo_insane"));
        commands.add(new QuickPlayCommand("Teams Normal", "/play teams_normal"));
        commands.add(new QuickPlayCommand("Teams Insane", "/play teams_insane"));
        commands.add(new QuickPlayCommand("Mega", "/play mega_normal"));

        this.setCommands(commands);
    }
}
