package me.miki.shindo.management.quickplay.impl;

import me.miki.shindo.management.quickplay.QuickPlay;
import me.miki.shindo.management.quickplay.QuickPlayCommand;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class TNTQuickPlay extends QuickPlay {

    public TNTQuickPlay() {
        super("TNT", new ResourceLocation("shindo/icons/hypixel/TNT.png"));
    }

    @Override
    public void addCommands() {
        ArrayList<QuickPlayCommand> commands = new ArrayList<QuickPlayCommand>();

        commands.add(new QuickPlayCommand("Lobby", "/l tnt"));
        commands.add(new QuickPlayCommand("TNT Run", "/play tnt_tntrun"));
        commands.add(new QuickPlayCommand("PVP Run", "/play tnt_pvprun"));
        commands.add(new QuickPlayCommand("Bow Spleef", "/play tnt_bowspleef"));
        commands.add(new QuickPlayCommand("TNT Tag", "/play tnt_tntag"));
        commands.add(new QuickPlayCommand("TNT Wizards", "/play tnt_capture"));

        this.setCommands(commands);
    }
}