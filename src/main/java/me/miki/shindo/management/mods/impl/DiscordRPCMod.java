package me.miki.shindo.management.mods.impl;

import me.miki.shindo.discord.DiscordRPC;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class DiscordRPCMod extends Mod {

    private final DiscordRPC discord = new DiscordRPC();

    public DiscordRPCMod() {
        super(TranslateText.DISCORD_RPC, TranslateText.DISCORD_RPC_DESCRIPTION, ModCategory.OTHER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        discord.start();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (discord.isStarted()) {
            discord.stop();
        }
    }
}
