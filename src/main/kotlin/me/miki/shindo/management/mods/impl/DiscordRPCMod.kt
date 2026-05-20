package me.miki.shindo.management.mods.impl

import me.miki.shindo.discord.DiscordRPC
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic

class DiscordRPCMod :
    Mod(
        TranslateText.DISCORD_RPC,
        TranslateText.DISCORD_RPC_DESCRIPTION,
        ModCategory.OTHER,
        Shinconic.MOD_DISCORD_RPC,
    ) {
    private val discord = DiscordRPC()

    override fun onEnable() {
        super.onEnable()
        discord.start()
    }

    override fun onDisable() {
        super.onDisable()
        if (discord.isStarted()) {
            discord.stop()
        }
    }
}
