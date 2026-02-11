package me.miki.shindo.management.quickplay.impl

import me.miki.shindo.management.quickplay.QuickPlay
import me.miki.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class MainLobbyQuickPlay : QuickPlay("MainLobby", ResourceLocation("shindo/icons/hypixel/MainLobby.png")) {

    override fun addCommands() {
        setCommands(arrayListOf(QuickPlayCommand("Lobby", "/lobby main")))
    }
}
