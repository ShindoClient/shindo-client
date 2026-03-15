package me.miki.shindo.management.quickplay.impl

import me.miki.shindo.management.quickplay.QuickPlay
import me.miki.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class UHCQuickPlay : QuickPlay("UHC", ResourceLocation("shindo/icons/hypixel/UHC.png")) {

    override fun addCommands() {
        setCommands(
            arrayListOf(
                QuickPlayCommand("Lobby", "/l hc"),
                QuickPlayCommand("Solo", "/play uhc_solo"),
                QuickPlayCommand("Teams", "/play uhc_teams"),
                QuickPlayCommand("Events Mode", "/play uhc_events")
            )
        )
    }
}
