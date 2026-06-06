package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.discord.DiscordRPC
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic

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
