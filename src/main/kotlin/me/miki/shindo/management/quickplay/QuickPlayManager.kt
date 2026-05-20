package me.miki.shindo.management.quickplay

import me.miki.shindo.management.quickplay.impl.ArcadeQuickPlay
import me.miki.shindo.management.quickplay.impl.BedwarsQuickPlay
import me.miki.shindo.management.quickplay.impl.DuelsQuickPlay
import me.miki.shindo.management.quickplay.impl.MainLobbyQuickPlay
import me.miki.shindo.management.quickplay.impl.MurderMysteryQuickPlay
import me.miki.shindo.management.quickplay.impl.SkywarsQuickPlay
import me.miki.shindo.management.quickplay.impl.TNTQuickPlay
import me.miki.shindo.management.quickplay.impl.UHCQuickPlay

class QuickPlayManager {
    private val quickPlays =
        ArrayList<QuickPlay>().apply {
            add(ArcadeQuickPlay())
            add(BedwarsQuickPlay())
            add(DuelsQuickPlay())
            add(MainLobbyQuickPlay())
            add(MurderMysteryQuickPlay())
            add(SkywarsQuickPlay())
            add(TNTQuickPlay())
            add(UHCQuickPlay())
        }

    fun getQuickPlays(): ArrayList<QuickPlay> = quickPlays
}
