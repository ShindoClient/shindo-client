package com.shindoclient.shindo.management.quickplay.impl

import com.shindoclient.shindo.management.quickplay.QuickPlay
import com.shindoclient.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class SkywarsQuickPlay : QuickPlay("Skywars", ResourceLocation("shindo/icons/hypixel/Skywars.png")) {
    override fun addCommands() {
        setCommands(
            arrayListOf(
                QuickPlayCommand("Lobby", "/l s"),
                QuickPlayCommand("Solo Normal", "/play solo_normal"),
                QuickPlayCommand("Solo Insane", "/play solo_insane"),
                QuickPlayCommand("Teams Normal", "/play teams_normal"),
                QuickPlayCommand("Teams Insane", "/play teams_insane"),
                QuickPlayCommand("Mega", "/play mega_normal"),
            ),
        )
    }
}
