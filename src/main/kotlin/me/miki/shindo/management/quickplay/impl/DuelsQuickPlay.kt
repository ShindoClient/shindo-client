package me.miki.shindo.management.quickplay.impl

import me.miki.shindo.management.quickplay.QuickPlay
import me.miki.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class DuelsQuickPlay : QuickPlay("Duels", ResourceLocation("shindo/icons/hypixel/Duels.png")) {

    override fun addCommands() {
        setCommands(arrayListOf(
            QuickPlayCommand("Lobby", "/l 1v1"),
            QuickPlayCommand("Classic", "/play duels_classic_duel"),
            QuickPlayCommand("Solo SkyWars", "/play duels_sw_duel"),
            QuickPlayCommand("Doubles SkyWars", "/play duels_sw_doubles"),
            QuickPlayCommand("Solo Bow", "/play duels_bow_duel"),
            QuickPlayCommand("Solo UHC", "/play duels_uhc_duel"),
            QuickPlayCommand("Double UHC", "/play duels_uhc_doubles"),
            QuickPlayCommand("Teams UHC", "/play duels_uhc_four"),
            QuickPlayCommand("Deathmatch UHC", "/play duels_uhc_meetup"),
            QuickPlayCommand("Solo NoDebuff", "/play duels_potion_duel"),
            QuickPlayCommand("Solo Combo", "/play duels_combo_duel"),
            QuickPlayCommand("Solo Potion", "/play duels_potion_duel"),
            QuickPlayCommand("Solo OP", "/play duels_op_duel"),
            QuickPlayCommand("Doubles OP", "/play duels_op_doubles"),
            QuickPlayCommand("Solo Mega Walls", "/play duels_mw_duel"),
            QuickPlayCommand("Doubles Mega Walls", "/play duels_mw_doubles"),
            QuickPlayCommand("Sumo", "/play duels_sumo_duel"),
            QuickPlayCommand("Solo Blitz", "/play duels_blitz_duel"),
            QuickPlayCommand("Solo Bow Spleef", "/play duels_bowspleef_duel"),
            QuickPlayCommand("Bridge 1v1", "/play duels_bridge_duel"),
            QuickPlayCommand("Bridge 2v2", "/play duels_bridge_doubles"),
            QuickPlayCommand("Bridge 4v4", "/play duels_bridge_four"),
            QuickPlayCommand("Bridge 2v2v2v2", "/play duels_bridge_2v2v2v2"),
            QuickPlayCommand("Bridge 3v3v3v3", "/play duels_bridge_3v3v3v3")
        ))
    }
}
