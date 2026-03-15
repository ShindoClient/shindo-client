package me.miki.shindo.management.quickplay.impl

import me.miki.shindo.management.quickplay.QuickPlay
import me.miki.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class TNTQuickPlay : QuickPlay("TNT", ResourceLocation("shindo/icons/hypixel/TNT.png")) {

    override fun addCommands() {
        setCommands(
            arrayListOf(
                QuickPlayCommand("Lobby", "/l tnt"),
                QuickPlayCommand("TNT Run", "/play tnt_tntrun"),
                QuickPlayCommand("PVP Run", "/play tnt_pvprun"),
                QuickPlayCommand("Bow Spleef", "/play tnt_bowspleef"),
                QuickPlayCommand("TNT Tag", "/play tnt_tntag"),
                QuickPlayCommand("TNT Wizards", "/play tnt_capture")
            )
        )
    }
}
