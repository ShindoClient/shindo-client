package me.miki.shindo.management.quickplay.impl

import me.miki.shindo.management.quickplay.QuickPlay
import me.miki.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class BedwarsQuickPlay : QuickPlay("Bedwars", ResourceLocation("shindo/icons/hypixel/Bedwars.png")) {

    override fun addCommands() {
        setCommands(arrayListOf(
            QuickPlayCommand("Lobby", "/l b"),
            QuickPlayCommand("Solo", "/play bedwars_eight_one"),
            QuickPlayCommand("Double", "/play bedwars_eight_two"),
            QuickPlayCommand("3v3v3v3", "/play bedwars_four_three"),
            QuickPlayCommand("4v4v4v4", "/play bedwars_four_four"),
            QuickPlayCommand("4v4", "/play bedwars_two_four"),
            QuickPlayCommand("Castle", "/play bedwars_castle")
        ))
    }
}
