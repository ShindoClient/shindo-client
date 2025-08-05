package me.miki.shindo.management.quickplay.impl;

import me.miki.shindo.management.quickplay.QuickPlay;
import me.miki.shindo.management.quickplay.QuickPlayCommand;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class MurderMysteryQuickPlay extends QuickPlay {

    public MurderMysteryQuickPlay() {
        super("Murder", new ResourceLocation("shindo/icons/hypixel/MurderMystery.png"));
    }

    @Override
    public void addCommands() {
        ArrayList<QuickPlayCommand> commands = new ArrayList<QuickPlayCommand>();

        commands.add(new QuickPlayCommand("Lobby", "/l mm"));
        commands.add(new QuickPlayCommand("Classic", "/play murder_classic"));
        commands.add(new QuickPlayCommand("Double Up", "/play murder_double_up"));
        commands.add(new QuickPlayCommand("Assasins", "/play murder_assassins"));
        commands.add(new QuickPlayCommand("Infection", "/play murder_infection"));

        this.setCommands(commands);
    }
}