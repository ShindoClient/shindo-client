package com.shindoclient.shindo.management.quickplay.impl

import com.shindoclient.shindo.management.quickplay.QuickPlay
import com.shindoclient.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class UHCQuickPlay : QuickPlay("UHC", ResourceLocation("shindo/icons/hypixel/UHC.png")) {
    override fun addCommands() {
        setCommands(
            arrayListOf(
                QuickPlayCommand("Lobby", "/l hc"),
                QuickPlayCommand("Solo", "/play uhc_solo"),
                QuickPlayCommand("Teams", "/play uhc_teams"),
                QuickPlayCommand("Events Mode", "/play uhc_events"),
            ),
        )
    }
}
