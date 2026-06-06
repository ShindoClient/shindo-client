package com.shindoclient.shindo.management.quickplay.impl

import com.shindoclient.shindo.management.quickplay.QuickPlay
import com.shindoclient.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class MainLobbyQuickPlay : QuickPlay("MainLobby", ResourceLocation("shindo/icons/hypixel/MainLobby.png")) {
    override fun addCommands() {
        setCommands(arrayListOf(QuickPlayCommand("Lobby", "/lobby main")))
    }
}
