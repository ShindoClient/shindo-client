package com.shindoclient.shindo.management.quickplay.impl

import com.shindoclient.shindo.management.quickplay.QuickPlay
import com.shindoclient.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class MurderMysteryQuickPlay : QuickPlay("Murder", ResourceLocation("shindo/icons/hypixel/MurderMystery.png")) {
    override fun addCommands() {
        setCommands(
            arrayListOf(
                QuickPlayCommand("Lobby", "/l mm"),
                QuickPlayCommand("Classic", "/play murder_classic"),
                QuickPlayCommand("Double Up", "/play murder_double_up"),
                QuickPlayCommand("Assasins", "/play murder_assassins"),
                QuickPlayCommand("Infection", "/play murder_infection"),
            ),
        )
    }
}
